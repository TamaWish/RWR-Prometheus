# RWR local observability demo

This folder is the ready-to-run Prometheus and Grafana example for RWR-Prometheus. It consumes live
metrics from a Minecraft server running on the same Docker host.

Before starting it, configure the plugin to listen on an address reachable from Docker—typically
`0.0.0.0:9225` for a local demo—and restart Minecraft. Confirm
`http://127.0.0.1:9225/metrics` works from the host.

From the repository root:

```shell
docker compose -f demo/docker-compose.yml up -d
```

Then open:

- Prometheus targets: <http://localhost:9090/targets>
- Grafana: <http://localhost:3000>

The Prometheus target named `rwr` should be `UP`. Grafana's provisioned dashboard is under
`Dashboards` → `ResourceWorldResetter` → `ResourceWorldResetter`.

If host port 3000 is occupied, change Grafana's Compose mapping from `"3000:3000"` to
`"3001:3000"`, start the stack again, and open <http://localhost:3001>. Docker Desktop users may
also ask the optional Gordon AI assistant to diagnose the error, but should review every proposed
command or file change before authorizing it; changes run on their computer at their own risk.

See the repository's main README for security guidance, troubleshooting, PromQL, existing
Prometheus installations, and contributor setup.
