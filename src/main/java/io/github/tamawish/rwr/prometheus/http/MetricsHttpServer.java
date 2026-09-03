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

public final class MetricsHttpServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor;
    private final CollectorRegistry registry;
    private final String metricsPath;
    private volatile boolean ready;

    public MetricsHttpServer(ExporterConfig config, CollectorRegistry registry) throws IOException {
        this.registry = Objects.requireNonNull(registry, "registry");
        metricsPath = config.metricsPath();
        server = HttpServer.create(new InetSocketAddress(config.address(), config.port()), 0);
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "rwr-prometheus-http");
            thread.setDaemon(true);
            return thread;
        };
        executor = Executors.newFixedThreadPool(2, factory);
        server.setExecutor(executor);
        server.createContext("/", this::handle);
    }

    public void start() { server.start(); }
    public void ready() { ready = true; }

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

    private static void respond(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
    }

    @Override
    public void close() {
        ready = false;
        server.stop(0);
        executor.shutdownNow();
    }
}
