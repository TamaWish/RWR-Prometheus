# Release notes

This file highlights user-facing updates for operators installing or upgrading RWR-Prometheus. Full technical history lives in [CHANGELOG.md](CHANGELOG.md).

## Version 1.0.1 — 2026-09-13

This maintenance release hardens exporter startup and shutdown, configuration validation, HTTP readiness, and reset metric lifecycle handling. It keeps the RWR API 5.1.2 compatibility baseline and is verified for ResourceWorldResetter 5.2.0.

Replace the plugin jar and restart the server. Existing configuration remains compatible.

## Version 1.0.0 — 2026-09-03

**Headline:** First public release — scrape ResourceWorldResetter reset activity with Prometheus and optionally open a ready-made Grafana dashboard.

RWR-Prometheus is an optional companion plugin for ResourceWorldResetter. It listens to reset lifecycle events, exposes a small set of per-world metrics over HTTP, and ships a Docker demo so you can see real server data in Grafana without building scrape config from scratch.

### Highlights

- Watch reset attempts, outcomes (`success` / `failure` / `cancelled`), duration, in-progress state, and last/next-known reset times per configured RWR world.
- Scrape from a local HTTP endpoint that defaults to `127.0.0.1:9225/metrics` so it stays off the public network unless you deliberately rebind it.
- Run the bundled Prometheus + Grafana Compose stack and open a provisioned ResourceWorldResetter dashboard against live Minecraft metrics.
- Use either the Spigot or Paper/Folia ResourceWorldResetter runtime; the plugin checks for a compatible RWR-API at enable time.

### Upgrade notes

- Nothing to migrate — this is the initial release. Drop the JAR into `plugins/`, start once to generate `config.yml`, then scrape or start the demo.
- Requires Java 21+, a Bukkit-compatible server on 1.21.4+, and ResourceWorldResetter 5.2 with RWR-API 5.1+.
- Prefer the `RWR-Prometheus-1.0.0-dist.zip` download when you want the JAR plus the `demo/` folder together.

### Links

- [Changelog entry for 1.0.0](CHANGELOG.md#100---2026-09-03)
- [README](README.md)
- [GitHub Release](https://github.com/TamaWish/RWR-Prometheus/releases/tag/v1.0.0)
