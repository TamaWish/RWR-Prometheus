# Contributing

Bug reports and focused pull requests are welcome. For security issues, follow `SECURITY.md` rather
than opening a public issue.

## Development setup

RWR-API 5.1.2 is published to Maven Central and is resolved automatically. Clone this repository,
then run:

```shell
mvn clean verify
```

Use Java 21 and Maven 3.9 or newer. Keep metric label values bounded: never add player names,
exception messages, paths, seeds, arbitrary commands, or other unbounded data as labels. Add or
update tests for behavior changes and update `README.md` and `CHANGELOG.md` when user-facing behavior
changes.
