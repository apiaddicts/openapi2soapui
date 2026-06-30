# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.2.0] - 2026-06-30

### Added
- Added `serverPattern` parameter: optional string (e.g. `%dev%`) that selects the OpenAPI server whose URL contains the given substring (after stripping `%`). If no server matches, the first server in the list is used as fallback. When omitted, all servers are added as endpoints (existing behavior preserved).

## [1.1.0] - 2026-06-30

### Added
- Added `readOnly` parameter: when set to `true`, only GET and OPTIONS test cases are generated; POST, PUT, PATCH and DELETE operations are excluded.
