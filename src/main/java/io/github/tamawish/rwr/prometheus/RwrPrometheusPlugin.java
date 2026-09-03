package io.github.tamawish.rwr.prometheus;

import io.github.tamawish.rwr.api.RwrApi;
import io.github.tamawish.rwr.prometheus.config.ExporterConfig;
import io.github.tamawish.rwr.prometheus.http.MetricsHttpServer;
import io.github.tamawish.rwr.prometheus.metrics.ResetMetrics;
import io.prometheus.client.CollectorRegistry;
import java.io.IOException;
import java.time.Clock;
import java.util.List;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class RwrPrometheusPlugin extends JavaPlugin {
    private MetricsHttpServer exporter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        RwrApi api;
        try {
            api = RwrApi.find(getServer()).orElseThrow(() -> new IllegalStateException(
                    "ResourceWorldResetter API 5.1+ is unavailable; install a compatible Spigot or Paper/Folia RWR build."));
        } catch (LinkageError error) {
            fail("ResourceWorldResetter API is incompatible: " + error.getClass().getSimpleName());
            return;
        } catch (IllegalStateException error) {
            fail(error.getMessage());
            return;
        }

        try {
            ExporterConfig config = ExporterConfig.load(getConfig());
            List<String> worlds = api.managedWorlds().stream().map(world -> world.id()).toList();
            CollectorRegistry registry = new CollectorRegistry();
            ResetMetrics metrics = new ResetMetrics(
                    registry, worlds, getDescription().getVersion(), Clock.systemUTC());
            getServer().getPluginManager().registerEvents(new ResetEventListener(metrics), this);
            exporter = new MetricsHttpServer(config, registry);
            exporter.start();
            exporter.ready();
            getLogger().info("Prometheus exporter ready at " + config.scrapeUrl()
                    + " for " + worlds.size() + " configured RWR world(s).");
        } catch (IllegalArgumentException | IOException error) {
            fail("Unable to start Prometheus exporter: " + error.getMessage());
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        if (exporter != null) {
            exporter.close();
            exporter = null;
        }
    }

    private void fail(String message) {
        getLogger().severe(message);
        getServer().getPluginManager().disablePlugin(this);
    }
}
