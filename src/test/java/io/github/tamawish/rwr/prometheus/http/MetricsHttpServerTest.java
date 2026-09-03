package io.github.tamawish.rwr.prometheus.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tamawish.rwr.prometheus.config.ExporterConfig;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import org.junit.jupiter.api.Test;

class MetricsHttpServerTest {
    @Test
    void servesReadinessExpositionAndUnknownPathsThenReleasesSocket() throws Exception {
        int port = freePort();
        ExporterConfig config = new ExporterConfig(InetAddress.getLoopbackAddress(), port, "/metrics");
        CollectorRegistry registry = new CollectorRegistry();
        Gauge.build("rwr_test_value", "test").register(registry).set(7);

        try (MetricsHttpServer server = new MetricsHttpServer(config, registry)) {
            server.start();
            assertThat(request(port, "/metrics").status()).isEqualTo(503);
            server.ready();
            Response metrics = request(port, "/metrics");
            assertThat(metrics.status()).isEqualTo(200);
            assertThat(metrics.contentType()).startsWith("text/plain; version=0.0.4");
            assertThat(metrics.body()).contains("rwr_test_value 7.0");
            assertThat(request(port, "/other").status()).isEqualTo(404);
        }

        try (MetricsHttpServer rebound = new MetricsHttpServer(config, new CollectorRegistry())) {
            rebound.start();
        }
    }

    @Test
    void reportsPortConflict() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            ExporterConfig config = new ExporterConfig(
                    InetAddress.getLoopbackAddress(), occupied.getLocalPort(), "/metrics");
            assertThatThrownBy(() -> new MetricsHttpServer(config, new CollectorRegistry()))
                    .isInstanceOf(java.io.IOException.class);
        }
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    private static Response request(int port, String path) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);
        int status = connection.getResponseCode();
        var stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String body = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        return new Response(status, connection.getHeaderField("Content-Type"), body);
    }

    private record Response(int status, String contentType, String body) {}
}
