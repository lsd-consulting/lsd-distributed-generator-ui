[![semantic-release](https://img.shields.io/badge/semantic-release-e10079.svg?logo=semantic-release)](https://github.com/semantic-release/semantic-release)

# lsd-distributed-generator-ui
![GitHub](https://img.shields.io/github/license/lsd-consulting/lsd-distributed-generator-ui)
![Codecov](https://img.shields.io/codecov/c/github/lsd-consulting/lsd-distributed-generator-ui)

[![CI](https://github.com/lsd-consulting/lsd-distributed-generator-ui/actions/workflows/ci.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-generator-ui/actions/workflows/ci.yml)
[![Nightly Build](https://github.com/lsd-consulting/lsd-distributed-generator-ui/actions/workflows/nightly.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-generator-ui/actions/workflows/nightly.yml)
[![GitHub release](https://img.shields.io/github/release/lsd-consulting/lsd-distributed-generator-ui)](https://github.com/lsd-consulting/lsd-distributed-generator-ui/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lsd-consulting/lsd-distributed-generator-ui-api.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.lsd-consulting%22%20AND%20a:%22lsd-distributed-generator-ui-api%22)

This is the graphical interface providing the LSD generation functionality as well as various endpoints.

## Testing
This service provides WireMock stubs for testing purposes.

## Releasing

Pushing to main builds but does not release. Maven Central limits the
io.github.lsd-consulting namespace to 7 releases a month, so each release is
started on purpose. semantic-release still picks the version from the feat/fix
commits since the last release, and does nothing if there are none.

Release by hand:

```
gh workflow run release.yml -R lsd-consulting/lsd-distributed-generator-ui -f reason="why this release is needed"
```

To release weekly instead (Tuesday 07:00 UTC), set the repository variable
`WEEKLY_RELEASE` to `true`. It is off by default.
