# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.1] - 2026-09-13

### Fixed

- Hardened exporter startup, shutdown, configuration validation, HTTP readiness, and reset metric lifecycle handling.

### Changed

- Resolved the public RWR API 5.1.2 contract from Maven Central and verified compatibility with ResourceWorldResetter 5.2.0.

## [1.0.0] - 2026-09-03

### Added

- Added bounded, per-world Prometheus metrics for reset attempts, outcomes, duration, active resets, last-reset timestamps, next-known scheduled resets, and exporter build identity.
- Added a configurable embedded HTTP metrics endpoint with loopback-only defaults, strict path handling, `404` for unknown paths, and `503` until the exporter is ready.
- Added support for the Spigot and Paper/Folia ResourceWorldResetter runtimes through RWR-API 5.1.2, with a hard enable-time API check.
- Added a ready-to-run Prometheus and Grafana Docker demo with a provisioned ResourceWorldResetter dashboard.
- Added a consumer distribution ZIP that packages the plugin JAR, README, and `demo/` folder.

[Unreleased]: https://github.com/TamaWish/RWR-Prometheus/compare/v1.0.1...HEAD
[1.0.1]: https://github.com/TamaWish/RWR-Prometheus/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/TamaWish/RWR-Prometheus/releases/tag/v1.0.0
