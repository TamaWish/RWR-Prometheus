package io.github.tamawish.rwr.prometheus.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Validated bind address, TCP port, and scrape path for the Prometheus HTTP exporter.
 *
 * @param address interface or host the exporter listens on
 * @param port TCP port in the inclusive range 1–65535
 * @param metricsPath absolute URL path that serves exposition text (for example {@code /metrics})
 */
public record ExporterConfig(InetAddress address, int port, String metricsPath) {
  public static final String DEFAULT_ADDRESS = "127.0.0.1";
  public static final int DEFAULT_PORT = 9225;
  public static final String DEFAULT_PATH = "/metrics";

  /**
   * Validates bind settings and normalizes {@code metricsPath}.
   *
   * @param address interface or host the exporter listens on
   * @param port TCP port in the inclusive range 1–65535
   * @param metricsPath absolute URL path that serves exposition text
   * @throws NullPointerException if {@code address} is null
   * @throws IllegalArgumentException if {@code port} or {@code metricsPath} is invalid
   */
  public ExporterConfig {
    Objects.requireNonNull(address, "address");
    validatePort(port);
    metricsPath = validatePath(metricsPath);
  }

  /**
   * Reads {@code server.address}, {@code server.port}, and {@code server.metrics-path} from plugin
   * config, applying defaults when keys are absent.
   *
   * @param root plugin configuration root (typically the plugin's {@code config.yml})
   * @return a validated exporter bind configuration
   * @throws NullPointerException if {@code root} is null
   * @throws IllegalArgumentException if an address, port, or path value fails validation
   */
  public static ExporterConfig load(ConfigurationSection root) {
    Objects.requireNonNull(root, "root");
    String address = root.getString("server.address", DEFAULT_ADDRESS);
    int port = root.getInt("server.port", DEFAULT_PORT);
    String path = root.getString("server.metrics-path", DEFAULT_PATH);
    if (address == null || address.isBlank()) {
      throw new IllegalArgumentException("server.address must not be blank");
    }
    try {
      return new ExporterConfig(InetAddress.getByName(address), port, path);
    } catch (UnknownHostException ex) {
      throw new IllegalArgumentException("server.address is not resolvable: " + address, ex);
    }
  }

  /**
   * Builds the HTTP scrape URL for this bind configuration, bracketing IPv6 hosts.
   *
   * @return a {@code http://host:port/path} URL suitable for logs and scrape configs
   */
  public String scrapeUrl() {
    String host = address.getHostAddress();
    if (host.contains(":")) {
      host = "[" + host + "]";
    }
    return "http://" + host + ":" + port + metricsPath;
  }

  private static void validatePort(int port) {
    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException("server.port must be between 1 and 65535");
    }
  }

  private static String validatePath(String path) {
    if (path == null || path.isBlank() || !path.startsWith("/")) {
      throw new IllegalArgumentException("server.metrics-path must start with /");
    }
    if (path.length() > 1 && path.endsWith("/")) {
      throw new IllegalArgumentException("server.metrics-path must not end with /");
    }
    if (path.contains("?") || path.contains("#") || path.contains(" ")) {
      throw new IllegalArgumentException("server.metrics-path must be a plain URL path");
    }
    return path;
  }
}
