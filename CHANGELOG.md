# Changelog

Public **v1** history for RWR-Prometheus. All 1.x.x versions stay in this file. When v2 begins,
start `CHANGELOG_v2.md`.

## 1.0.0 - 2026-09-03

- Added bounded, per-world Prometheus metrics for reset attempts, outcomes, duration, active resets,
  last-reset timestamps, next-known scheduled resets, and exporter build identity.
- Added a configurable embedded HTTP endpoint with loopback-only defaults and strict path handling.
- Added support for the Spigot and Paper/Folia ResourceWorldResetter runtimes through RWR-API 5.1.2.
- Added a ready-to-run Prometheus and Grafana Docker demo with a provisioned dashboard.
- Added automated tests for configuration, lifecycle metrics, label bounds, endpoint behavior, port
  conflicts, shutdown, and socket reuse.
