# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.1.0] - 2026-06-30

### Added
- Added `readOnly` parameter: when set to `true`, only GET and OPTIONS test cases are generated; POST, PUT, PATCH and DELETE operations are excluded.
- Added `serverPattern` parameter: optional string (e.g. `%dev%`) that selects the OpenAPI server whose URL contains the given substring (after stripping `%`). If no server matches, the first server in the list is used as fallback. When omitted, all servers are added as endpoints (existing behavior preserved).
- Added `minimalEndpoints` parameter: when set to `false` (default), two additional test cases are generated per optional query parameter of each operation — one with a valid value asserting HTTP status 200, and one with an invalid value asserting HTTP status 400. When `true`, only the `testCaseNames`-based test cases are generated.
- Added `microcksHeaders` parameter: when set to `true`, adds an `X-Microcks-Response-Name` header to each request, in addition to any custom headers. Its value is the name of the response example defined in the OpenAPI spec (first 2xx response, falling back to the `default` response), or `default` if none is defined.
- Added `generateOneOfAnyOf` parameter: when set to `true`, `oneOf`/`anyOf` schemas are resolved to their first candidate schema when generating the example request body (default: `false`, left unresolved). `allOf` schemas are always merged into a single object, regardless of this flag.
