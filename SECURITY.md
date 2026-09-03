# Security policy

## Supported versions

Security fixes are provided for the latest published release.

## Reporting a vulnerability

Please do not open a public issue for a suspected vulnerability. Use GitHub's **Report a
vulnerability** option in the repository Security tab. If private vulnerability reporting is not
available, contact TamaWish at `zaineloz55@gmail.com` with a concise reproduction and impact
assessment.

The metrics endpoint has no authentication or TLS. It defaults to `127.0.0.1`; operators who bind it
to another interface must restrict access with a firewall or private network and should terminate
TLS at a trusted reverse proxy when traffic leaves the host.
