# Contributing to RWR-Prometheus

Thanks for helping improve RWR-Prometheus. Bug reports, documentation fixes, tests, and focused pull requests are all welcome.

## Ways to contribute

- Report reproducible bugs
- Suggest focused enhancements
- Improve docs (`README.md`, `CHANGELOG.md`, comments that clarify behavior)
- Add or strengthen automated tests
- Triage and clarify existing issues

Please do **not** use public issues for security vulnerabilities. See [Security vulnerabilities](#security-vulnerabilities).

## I have a question

1. Check [README.md](README.md) for install, scrape, and demo setup.
2. Search [existing issues](https://github.com/TamaWish/RWR-Prometheus/issues).

If the docs do not answer it, open a GitHub issue and use the `question` label when it fits. This repository does not currently use GitHub Discussions.

## Reporting bugs

Before filing:

- Confirm you are on the [latest release](https://github.com/TamaWish/RWR-Prometheus/releases)
- Rule out config mistakes (bind address, port conflicts, missing RWR runtime)
- Search for an existing issue

A useful report includes:

- Expected vs actual behavior
- Steps to reproduce
- Minecraft server software and version (CraftBukkit / Spigot / Paper / Purpur / Folia)
- ResourceWorldResetter runtime and version
- RWR-Prometheus version
- Relevant server logs (redact secrets and player data)

Open bugs at [GitHub Issues](https://github.com/TamaWish/RWR-Prometheus/issues).

## Security vulnerabilities

> [!WARNING]
> Do not report security issues in the public tracker.

Follow [SECURITY.md](SECURITY.md): use GitHub’s private vulnerability reporting on the repository Security tab, or contact the maintainer address listed there if private reporting is unavailable.

## Suggesting enhancements

Search existing issues first. Describe:

1. The problem or operator need
2. Your proposed approach
3. Alternatives you considered
4. Why it helps most users of this exporter

Large or breaking changes should be discussed in an issue before a pull request.

## Finding something to work on

Browse issues labeled [`good first issue`](https://github.com/TamaWish/RWR-Prometheus/labels/good%20first%20issue) or [`help wanted`](https://github.com/TamaWish/RWR-Prometheus/labels/help%20wanted) when those labels are in use.

## Development setup

### Prerequisites

- Java 21 or newer (the build targets bytecode 21 via `maven.compiler.release`; do not raise the class-file version)
- Maven 3.9 or newer

RWR-API 5.1.2 is published to Maven Central and resolves automatically.

### Quick start

```shell
git clone https://github.com/TamaWish/RWR-Prometheus.git
```

```shell
cd RWR-Prometheus
```

```shell
mvn clean verify
```

Successful builds produce:

- `target/RWR-Prometheus-1.0.0.jar`
- `target/RWR-Prometheus-1.0.0-dist.zip`

CI runs the same `mvn clean verify` on pushes and pull requests to `main`.

## Making changes

1. Fork the repository and branch from `main`.
2. Keep the change set focused on one problem.
3. Add or update tests when behavior changes.
4. Update [README.md](README.md) and [CHANGELOG.md](CHANGELOG.md) when the change is user-facing.
5. Format Java sources before opening a PR.

### Tests and formatting

```shell
mvn clean verify
```

```shell
mvn spotless:apply
```

```shell
mvn spotless:check
```

`mvn verify` runs the unit suite and Spotless check. Spotless uses Google Java Format.

### Metric label bounds

Keep Prometheus label values bounded. Never add player names, exception messages, paths, seeds, arbitrary commands, or other unbounded data as labels.

## Pull requests

- Target `main`
- Keep the PR small and focused
- Link the related issue when one exists
- Include tests for behavior changes
- Update docs and the changelog when operators will notice the change
- Ensure `mvn clean verify` passes locally

There is no separate PR template in this repository yet. Describe what changed, why, and how you verified it.

## Legal

By contributing, you agree that your contributions are licensed under the same [BSD 3-Clause License](LICENSE) as the project.

## Getting help

- Docs: [README.md](README.md)
- Bugs and features: [GitHub Issues](https://github.com/TamaWish/RWR-Prometheus/issues)
- Security: [SECURITY.md](SECURITY.md)
