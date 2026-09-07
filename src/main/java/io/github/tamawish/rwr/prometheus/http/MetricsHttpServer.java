package io.github.tamawish.rwr.prometheus.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.tamawish.rwr.prometheus.config.ExporterConfig;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Minimal HTTP server that serves Prometheus text exposition from a {@link CollectorRegistry} on a
 * dedicated daemon thread pool.
 */
public final class MetricsHttpServer implements AutoCloseable {
  private final HttpServer server;
  private final ExecutorService executor;
  private final CollectorRegistry registry;
  private final String metricsPath;
  private volatile boolean ready;

  /**
   * Binds the scrape endpoint using {@code config} but does not accept connections until {@link
   * #start()} runs.
   *
   * @param config listen address, port, and metrics path
   * @param registry collector registry whose samples are written on successful scrapes
   * @throws NullPointerException if {@code registry} is null
   * @throws IOException if the configured address and port cannot be bound
   */
  public MetricsHttpServer(ExporterConfig config, CollectorRegistry registry) throws IOException {
    this.registry = Objects.requireNonNull(registry, "registry");
    metricsPath = config.metricsPath();
    server = HttpServer.create(new InetSocketAddress(config.address(), config.port()), 0);
    ThreadFactory factory =
        runnable -> {
          Thread thread = new Thread(runnable, "rwr-prometheus-http");
          thread.setDaemon(true);
          return thread;
        };
    executor = Executors.newFixedThreadPool(2, factory);
    server.setExecutor(executor);
    server.createContext("/", this::handle);
  }

  /** Begins accepting HTTP connections on the configured bind address. */
  public void start() {
    server.start();
  }

  /**
   * Marks the exporter ready so the metrics path returns {@code 200} instead of {@code 503}. Call
   * after listeners and collectors are registered.
   */
  public void ready() {
    ready = true;
  }

  private void handle(HttpExchange exchange) throws IOException {
    if (!metricsPath.equals(exchange.getRequestURI().getPath())) {
      respond(exchange, 404, "text/plain; charset=utf-8", "Not Found\n");
      return;
    }
    if (!ready) {
      respond(exchange, 503, "text/plain; charset=utf-8", "Exporter not ready\n");
      return;
    }
    if (!"GET".equals(exchange.getRequestMethod())) {
      respond(exchange, 405, "text/plain; charset=utf-8", "Method Not Allowed\n");
      return;
    }
    StringWriter body = new StringWriter();
    TextFormat.write004(body, registry.metricFamilySamples());
    respond(exchange, 200, TextFormat.CONTENT_TYPE_004, body.toString());
  }

  private static void respond(HttpExchange exchange, int status, String contentType, String body)
      throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", contentType);
    exchange.sendResponseHeaders(status, bytes.length);
    try (var output = exchange.getResponseBody()) {
      output.write(bytes);
    }
  }

  /**
   * Stops accepting scrapes, shuts down the HTTP server, and interrupts its executor threads so the
   * listen port can be rebound.
   */
  @Override
  public void close() {
    ready = false;
    server.stop(0);
    executor.shutdownNow();
  }
}
