package io.github.tamawish.rwr.prometheus.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class ExporterConfigTest {
    @Test
    void loadsDefaults() {
        ExporterConfig config = ExporterConfig.load(new YamlConfiguration());
        assertThat(config.address().isLoopbackAddress()).isTrue();
        assertThat(config.port()).isEqualTo(9225);
        assertThat(config.metricsPath()).isEqualTo("/metrics");
    }

    @Test
    void rejectsInvalidAddressPortAndPath() {
        assertInvalid("server.address", " ", "address");
        assertInvalid("server.port", 0, "port");
        assertInvalid("server.port", 65536, "port");
        assertInvalid("server.metrics-path", "metrics", "path");
        assertInvalid("server.metrics-path", "/metrics/", "path");
        assertInvalid("server.metrics-path", "/metrics?q=1", "path");
    }

    private static void assertInvalid(String key, Object value, String message) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set(key, value);
        assertThatThrownBy(() -> ExporterConfig.load(yaml))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(message);
    }
}
