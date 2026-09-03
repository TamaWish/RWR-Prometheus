package io.github.tamawish.rwr.prometheus.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record ExporterConfig(InetAddress address, int port, String metricsPath) {
    public static final String DEFAULT_ADDRESS = "127.0.0.1";
    public static final int DEFAULT_PORT = 9225;
    public static final String DEFAULT_PATH = "/metrics";

    public ExporterConfig {
        Objects.requireNonNull(address, "address");
        validatePort(port);
        metricsPath = validatePath(metricsPath);
    }

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

    public String scrapeUrl() {
        String host = address.getHostAddress();
        if (host.contains(":")) host = "[" + host + "]";
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
