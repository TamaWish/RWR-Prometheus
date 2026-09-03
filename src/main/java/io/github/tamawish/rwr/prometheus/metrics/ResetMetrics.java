package io.github.tamawish.rwr.prometheus.metrics;

import io.github.tamawish.rwr.api.event.ResourceWorldPostResetEvent;
import io.github.tamawish.rwr.api.event.ResourceWorldPreResetEvent;
import io.github.tamawish.rwr.api.event.ResourceWorldResetWarningEvent;
import io.github.tamawish.rwr.api.model.ResetFailureType;
import io.github.tamawish.rwr.api.model.ResetPhase;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ResetMetrics {
    private final Clock clock;
    private final Map<String, String> configuredWorlds;
    private final Map<String, Attempt> attempts = new HashMap<>();
    private final Counter attemptsTotal;
    private final Counter completionsTotal;
    private final Histogram durations;
    private final Gauge inProgress;
    private final Gauge lastReset;
    private final Gauge nextReset;

    public ResetMetrics(CollectorRegistry registry, Collection<String> worldIds, String version, Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        configuredWorlds = new HashMap<>();
        for (String world : worldIds) configuredWorlds.put(normalize(world), world);
        attemptsTotal = Counter.build("rwr_reset_attempts_total", "RWR reset attempts.")
                .labelNames("world").register(registry);
        completionsTotal = Counter.build("rwr_reset_completions_total", "Terminal RWR reset outcomes.")
                .labelNames("world", "result").register(registry);
        durations = Histogram.build("rwr_reset_duration_seconds", "RWR reset duration in seconds.")
                .labelNames("world").register(registry);
        inProgress = Gauge.build("rwr_reset_in_progress", "Number of active RWR reset operations.")
                .labelNames("world").register(registry);
        lastReset = Gauge.build("rwr_last_reset_timestamp_seconds", "Unix timestamp of the last terminal reset.")
                .labelNames("world").register(registry);
        nextReset = Gauge.build("rwr_next_scheduled_reset_timestamp_seconds", "Unix timestamp of the next known scheduled reset.")
                .labelNames("world").register(registry);
        Gauge.build("rwr_exporter_build_info", "RWR-Prometheus build information.")
                .labelNames("version").register(registry).labels(version).set(1);
        for (String world : configuredWorlds.values()) {
            attemptsTotal.labels(world);
            inProgress.labels(world).set(0);
        }
    }

    public synchronized void onStart(ResourceWorldPreResetEvent event) {
        String world = accepted(event.getWorldId());
        if (world == null || attempts.containsKey(event.getOperationId())) return;
        attempts.put(event.getOperationId(), new Attempt(world, clock.instant()));
        attemptsTotal.labels(world).inc();
        inProgress.labels(world).inc();
    }

    public synchronized void onCompletion(ResourceWorldPostResetEvent event) {
        String world = accepted(event.getWorldId());
        if (world == null) return;
        Attempt attempt = attempts.remove(event.getOperationId());
        String result = result(event);
        completionsTotal.labels(world, result).inc();
        Instant now = clock.instant();
        lastReset.labels(world).set(now.getEpochSecond());
        if (attempt != null) {
            inProgress.labels(attempt.world()).dec();
            double seconds = Math.max(0, (now.toEpochMilli() - attempt.startedAt().toEpochMilli()) / 1000.0);
            durations.labels(attempt.world()).observe(seconds);
        }
    }

    public synchronized void onWarning(ResourceWorldResetWarningEvent event) {
        String world = accepted(event.getWorldId());
        if (world != null) nextReset.labels(world).set(event.getScheduledResetAt().getEpochSecond());
    }

    private String accepted(String world) {
        String normalized = normalize(world);
        return configuredWorlds.get(normalized);
    }

    private static String normalize(String world) {
        Objects.requireNonNull(world, "world");
        return world.toLowerCase(Locale.ROOT);
    }

    private static String result(ResourceWorldPostResetEvent event) {
        if (event.getPhase() == ResetPhase.COMPLETE) return "success";
        if (event.getFailure().orElse(null) == ResetFailureType.EVENT_CANCELLED) return "cancelled";
        return "failure";
    }

    private record Attempt(String world, Instant startedAt) {}
}
