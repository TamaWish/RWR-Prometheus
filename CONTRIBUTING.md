# Contributing

Bug reports and focused pull requests are welcome. For security issues, follow `SECURITY.md` rather
than opening a public issue.

## Development setup

RWR-API is not yet consumed from Maven Central. Clone `RWR-API` next to this repository, then run:

```shell
cd ../RWR-API
mvn clean install
cd ../RWR-Prometheus
mvn clean verify
```

Use Java 21 and Maven 3.9 or newer. Keep metric label values bounded: never add player names,
exception messages, paths, seeds, arbitrary commands, or other unbounded data as labels. Add or
update tests for behavior changes and update `README.md` and `CHANGELOG.md` when user-facing behavior
changes.
