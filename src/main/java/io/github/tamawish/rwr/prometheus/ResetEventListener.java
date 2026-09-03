package io.github.tamawish.rwr.prometheus;

import io.github.tamawish.rwr.api.event.ResourceWorldPostResetEvent;
import io.github.tamawish.rwr.api.event.ResourceWorldPreResetEvent;
import io.github.tamawish.rwr.api.event.ResourceWorldResetWarningEvent;
import io.github.tamawish.rwr.prometheus.metrics.ResetMetrics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

final class ResetEventListener implements Listener {
    private final ResetMetrics metrics;
    ResetEventListener(ResetMetrics metrics) { this.metrics = metrics; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onStart(ResourceWorldPreResetEvent event) { metrics.onStart(event); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCompletion(ResourceWorldPostResetEvent event) { metrics.onCompletion(event); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWarning(ResourceWorldResetWarningEvent event) { metrics.onWarning(event); }
}
