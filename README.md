# RWR-Prometheus

RWR-Prometheus is an optional Bukkit-compatible companion plugin for
[ResourceWorldResetter](https://github.com/TamaWish/ResourceWorldResetter). It turns RWR reset
lifecycle events into a small set of Prometheus metrics and serves them from an HTTP endpoint.

[![CI](https://github.com/TamaWish/RWR-Prometheus/actions/workflows/ci.yml/badge.svg)](https://github.com/TamaWish/RWR-Prometheus/actions/workflows/ci.yml)
[![License: BSD-3-Clause](https://img.shields.io/badge/License-BSD--3--Clause-blue.svg)](LICENSE)

The repository includes a ready-to-run `demo/` folder containing Prometheus, Grafana, an already
configured Prometheus data source, and an RWR dashboard. The demo uses real data from your running
Minecraft server; it does not contain simulated or sample metrics.

## What to download

The easiest download is `RWR-Prometheus-<version>-dist.zip`. It contains:

1. `RWR-Prometheus-<version>.jar` for the Minecraft server.
2. This README.
3. The complete `demo/` folder for the optional Docker setup.

The JAR may also be downloaded by itself when an existing Prometheus installation is already
available. The `demo/` folder is distributed inside the release ZIP, not inside the plugin JAR.
Existing Prometheus and Grafana installations do not need it; use `demo/prometheus.yml` and the
dashboard JSON as reference instead.

## Requirements

- Java 21+
- CraftBukkit, Spigot, Paper, Purpur, or Folia 1.21.4+
- The matching ResourceWorldResetter 5.2 runtime with RWR-API 5.1+
- Docker Desktop or Docker Engine with Compose, only for the bundled dashboard demo

Install the matching RWR runtime: `ResourceWorldResetter` on CraftBukkit/Spigot, or
`ResourceWorldResetter-Paper-Folia` on Paper/Purpur/Folia. Bukkit metadata cannot express “require
either of these plugin names,” so both are declared as load-order dependencies and RWR-Prometheus
performs the hard requirement check during enable. It disables itself with a clear message unless
one runtime has registered a compatible public RWR API.

## Quick start: plugin only

1. Copy `RWR-Prometheus-<version>.jar` into the Minecraft server's `plugins/` directory.
2. Start the server once so `plugins/RWR-Prometheus/config.yml` is generated.
3. Keep the default loopback configuration when Prometheus runs directly on the Minecraft host:

   ```yaml
   server:
     address: 127.0.0.1
     port: 9225
     metrics-path: /metrics
   ```

4. Restart the server and verify the exporter:

   ```shell
   curl http://127.0.0.1:9225/metrics
   ```

The server log prints the final scrape URL. Unknown paths return `404`, and the metrics path returns
`503` until plugin initialization finishes. An invalid address, port, path, or occupied port prevents
the exporter from enabling.

## Easiest complete setup: Docker, Prometheus, and Grafana

Use this when Docker and the Minecraft server run on the same computer.

### 1. Make the exporter reachable from Docker

Docker containers cannot normally reach a service bound only to the host's `127.0.0.1`. Change
`plugins/RWR-Prometheus/config.yml` to:

```yaml
server:
  address: 0.0.0.0
  port: 9225
  metrics-path: /metrics
```

Restart Minecraft and verify from the host:

```shell
curl http://127.0.0.1:9225/metrics
```

The endpoint has no authentication. Restrict inbound TCP port `9225` with the host firewall so it
is reachable only from the local machine, Docker network, or designated Prometheus host. Binding to
a specific Docker-facing private address is safer than `0.0.0.0` when that address is known.

### 2. Start the provided stack

Open a terminal in the repository or extracted release folder, then run:

```shell
docker compose -f demo/docker-compose.yml up -d
```

The Compose file starts:

- Prometheus at <http://localhost:9090>
- Grafana at <http://localhost:3000>

Prometheus scrapes `host.docker.internal:9225` every five seconds. The Compose file includes the
`host-gateway` mapping needed by Linux Docker hosts.

### 3. Confirm that Prometheus sees Minecraft

Open <http://localhost:9090/targets>. The `rwr` target should be `UP`.

Alternatively, open <http://localhost:9090/query> and run:

```promql
up{job="rwr"}
```

The result should be `1`. Also try:

```promql
rwr_exporter_build_info
```

If the target is `DOWN`, first confirm that `curl http://127.0.0.1:9225/metrics` works on the host,
then test from the container:

```shell
docker compose -f demo/docker-compose.yml exec prometheus \
  wget -qO- http://host.docker.internal:9225/metrics
```

On PowerShell, enter that command on one line rather than using the `\` continuation.

### 4. Open the Grafana dashboard

Open <http://localhost:3000> and navigate to:

`Dashboards` → `ResourceWorldResetter` → `ResourceWorldResetter`

The Prometheus data source and dashboard are provisioned automatically. Anonymous viewer access is
enabled for the local demo. Administrative credentials are `admin` / `admin`; change or remove these
defaults before using the stack anywhere beyond local development.

Trigger an RWR reset, wait for the next five-second scrape, and the dashboard will show the real
outcome, duration, active-reset state, and known last/next reset times.

### Port conflicts and Docker troubleshooting

If Compose reports that port `3000` is already allocated, another program is using Grafana's host
port. Edit `demo/docker-compose.yml` and change:

```yaml
ports:
  - "3000:3000"
```

to an unused host port, for example:

```yaml
ports:
  - "3001:3000"
```

Run `docker compose -f demo/docker-compose.yml up -d` again and open
<http://localhost:3001>. Keep the right-hand `3000`: that is Grafana's port inside the container.
For a Prometheus conflict, change `"9090:9090"` to, for example, `"9091:9090"`, then open port
9091 on the host.

Recent Docker Desktop versions may offer the Gordon AI assistant. Consumers can ask it to diagnose
the Compose error and propose or apply a fix on their computer when that feature is available and
they explicitly authorize the action. Review AI-generated commands and configuration changes before
approval: they run against the consumer's own Docker environment and are used at their own risk.
Gordon is optional; the manual port-mapping fix above works without it.

### 5. Stop or remove the demo

Stop it while retaining the containers:

```shell
docker compose -f demo/docker-compose.yml stop
```

Start it again:

```shell
docker compose -f demo/docker-compose.yml start
```

Remove the demo containers:

```shell
docker compose -f demo/docker-compose.yml down
```

This example stack is intentionally lightweight and does not configure persistent Prometheus or
Grafana data volumes. Removing/recreating its containers can discard collected history and local
Grafana changes. The provisioned dashboard returns when the stack starts again. Plugin metrics also
restart from zero whenever RWR-Prometheus or the Minecraft server restarts.

## Using an existing Prometheus installation

Add this scrape job and replace the target with the Minecraft host address:

```yaml
scrape_configs:
  - job_name: rwr
    scrape_interval: 5s
    static_configs:
      - targets: [minecraft.example.internal:9225]
```

Bind the exporter to a specific private interface address and allow TCP port `9225` only from the
Prometheus server. Do not expose this unauthenticated endpoint publicly.

Import `demo/grafana/dashboards/rwr.json` into an existing Grafana instance and select its Prometheus
data source if automatic provisioning is not being used.

## Metrics

All `world` labels are canonical world identifiers captured from RWR configuration when the plugin
enables. Events for unknown worlds are ignored. Player names, exception text, paths, seeds, and other
unbounded values are never labels.

| Metric | Meaning |
|---|---|
| `rwr_reset_attempts_total{world}` | Reset lifecycle attempts observed |
| `rwr_reset_completions_total{world,result}` | Outcomes with `success`, `failure`, or `cancelled` |
| `rwr_reset_duration_seconds{world}` | Reset duration histogram |
| `rwr_reset_in_progress{world}` | Number of active observed operations |
| `rwr_last_reset_timestamp_seconds{world}` | Unix timestamp of the latest terminal event |
| `rwr_next_scheduled_reset_timestamp_seconds{world}` | Next reset learned from a scheduled-warning event |
| `rwr_exporter_build_info{version}` | Exporter build identity; value is always `1` |

The next-scheduled metric exists only after RWR publishes scheduling information through a warning
event. RWR-API 5.1 does not expose a complete scheduler snapshot.

## Useful PromQL

```promql
# Exporter scrape health; 1 means reachable
up{job="rwr"}

# Attempts and outcomes by configured world
rwr_reset_attempts_total
rwr_reset_completions_total

# Outcome activity over the last 15 minutes
sum by (world, result) (rate(rwr_reset_completions_total[15m]))

# 95th-percentile duration per world
histogram_quantile(
  0.95,
  sum by (world, le) (rate(rwr_reset_duration_seconds_bucket[1h]))
)

# Currently active resets
rwr_reset_in_progress

# Seconds since the last terminal reset
time() - rwr_last_reset_timestamp_seconds

# Seconds until the next reset known from a warning event
rwr_next_scheduled_reset_timestamp_seconds - time()
```

## Contributor and development setup

RWR-API 5.1.2 is published to Maven Central and Maven resolves it automatically. Clone
RWR-Prometheus and run:

```shell
mvn clean verify
```

The build produces `target/RWR-Prometheus-1.0.0.jar` and the consumer-ready
`target/RWR-Prometheus-1.0.0-dist.zip`. Prometheus client dependencies are shaded and relocated into
the JAR; Bukkit/Spigot and RWR-API remain provided by the server.

`mvn verify` tests configuration validation, successful/failed/cancelled and overlapping/repeated
updates, world isolation, bounded labels, clock-controlled durations and timestamps, endpoint
readiness, exposition content type, unknown paths, port conflicts, shutdown, and socket reuse.

For a complete manual smoke test:

1. Start each supported server family with one configured RWR resource world.
2. Install the matching RWR runtime and RWR-Prometheus JARs.
3. Start the Docker demo and confirm `up{job="rwr"}` is `1`.
4. Trigger `/rwr reset <world-id>`.
5. Confirm the completion counter and duration histogram update in Prometheus and Grafana.

The Docker stack is a convenient local integration environment, not a production observability
deployment.

## Support, security, and license

- Use [GitHub Issues](https://github.com/TamaWish/RWR-Prometheus/issues) for reproducible bugs and
  focused feature requests.
- See [CONTRIBUTING.md](CONTRIBUTING.md) before submitting a pull request.
- Report vulnerabilities privately as described in [SECURITY.md](SECURITY.md).
- RWR-Prometheus is released under the [BSD 3-Clause License](LICENSE).
