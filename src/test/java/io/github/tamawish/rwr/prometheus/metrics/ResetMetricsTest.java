package io.github.tamawish.rwr.prometheus.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.tamawish.rwr.api.event.ResourceWorldPostResetEvent;
import io.github.tamawish.rwr.api.event.ResourceWorldPreResetEvent;
import io.github.tamawish.rwr.api.event.ResourceWorldResetWarningEvent;
import io.github.tamawish.rwr.api.model.FailureSafety;
import io.github.tamawish.rwr.api.model.ResetFailureType;
import io.github.tamawish.rwr.api.model.ResetPhase;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResetMetricsTest {
    @Test
    void recordsOutcomesOverlapsRepeatsAndIsolatesWorlds() throws Exception {
        MutableClock clock = new MutableClock(Instant.ofEpochSecond(100));
        CollectorRegistry registry = new CollectorRegistry();
        ResetMetrics metrics = new ResetMetrics(registry, List.of("resource", "nether"), "1.0.0", clock);

        metrics.onStart(pre("one", "resource"));
        metrics.onStart(pre("one", "resource")); // duplicate lifecycle notification
        metrics.onStart(pre("two", "resource")); // overlapping attempt
        metrics.onStart(pre("ignored", "not-configured"));
        clock.advanceSeconds(5);
        metrics.onCompletion(post("one", "resource", ResetPhase.COMPLETE, null));
        metrics.onCompletion(post("two", "resource", ResetPhase.FAILED, ResetFailureType.EVENT_CANCELLED));
        metrics.onStart(pre("three", "nether"));
        clock.advanceSeconds(2);
        metrics.onCompletion(post("three", "nether", ResetPhase.FAILED, ResetFailureType.VERIFICATION_FAILED));
        metrics.onWarning(new ResourceWorldResetWarningEvent("nether", "world_nether", 5, Instant.ofEpochSecond(500)));

        String text = exposition(registry);
        assertThat(text).contains("rwr_reset_attempts_total{world=\"resource\",} 2.0")
                .contains("rwr_reset_attempts_total{world=\"nether\",} 1.0")
                .contains("rwr_reset_completions_total{world=\"resource\",result=\"success\",} 1.0")
                .contains("rwr_reset_completions_total{world=\"resource\",result=\"cancelled\",} 1.0")
                .contains("rwr_reset_completions_total{world=\"nether\",result=\"failure\",} 1.0")
                .contains("rwr_reset_in_progress{world=\"resource\",} 0.0")
                .contains("rwr_reset_duration_seconds_sum{world=\"resource\",} 10.0")
                .contains("rwr_reset_duration_seconds_sum{world=\"nether\",} 2.0")
                .contains("rwr_last_reset_timestamp_seconds{world=\"nether\",} 107.0")
                .contains("rwr_next_scheduled_reset_timestamp_seconds{world=\"nether\",} 500.0")
                .contains("rwr_exporter_build_info{version=\"1.0.0\",} 1.0")
                .doesNotContain("not-configured");
    }

    private static ResourceWorldPreResetEvent pre(String operation, String world) {
        return new ResourceWorldPreResetEvent(operation, world, "world_" + world);
    }

    private static ResourceWorldPostResetEvent post(
            String operation, String world, ResetPhase phase, ResetFailureType failure) {
        return new ResourceWorldPostResetEvent(operation, world, "world_" + world, phase, failure,
                failure == null ? FailureSafety.SAFE_TO_RETRY : FailureSafety.NOT_RETRYABLE, "done");
    }

    private static String exposition(CollectorRegistry registry) throws Exception {
        StringWriter writer = new StringWriter();
        TextFormat.write004(writer, registry.metricFamilySamples());
        return writer.toString();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        MutableClock(Instant instant) { this.instant = instant; }
        void advanceSeconds(long seconds) { instant = instant.plusSeconds(seconds); }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
