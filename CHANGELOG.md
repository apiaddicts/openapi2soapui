# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.1.0-beta-1] - 2026-07-02

### Added
- `readOnly`: when `true`, only generates GET and OPTIONS test cases.
- `serverPattern`: selects the OpenAPI server whose URL matches the substring (e.g. `%dev%`); defaults to the first server if none match, or all servers if omitted.
- `minimalEndpoints`: when `false` (default), adds a valid (200) and an invalid (400) test case per optional query parameter. When `true`, only generates the `testCaseNames`-based cases.
- `microcksHeaders`: when `true`, adds an `X-Microcks-Response-Name` header with the matching response example name (or `default`).
- `generateOneOfAnyOf`: when `true`, resolves `oneOf`/`anyOf` to their first candidate when generating example bodies (`allOf` is always merged).
- `examples`: optional `{ successful, wrong }` object with custom `string`/`number`/`boolean`/`date`/`dateTime` values. `successful` feeds example bodies and valid query-param values; `wrong` feeds the invalid query-param values from `minimalEndpoints`. Unset fields keep the existing defaults.
- `validateSchema`: when `true`, adds an automatic check (an assertion) to each main test case's request test step that verifies the response body matches the JSON Schema of the operation's first 2xx JSON response. Covers type, required, properties, items, enum, oneOf/anyOf, allOf, nullable, pattern, format (email/uuid/date/date-time), length/size/range bounds and additionalProperties.
- `isInline`: when `false` (default), JSON request-body example values are generated as SoapUI Project Properties and referenced from the body via a `${#Project#...}` token instead of being embedded literally. When `true`, literal values are embedded directly in the body (previous behavior).
- `schemaIsInline`: only relevant when `validateSchema` is `true`. When `false` (default), the JSON Schema used by that check is stored separately as a SoapUI Project Property and looked up automatically when the test runs, instead of being written out in full inside the check. When `true`, the full schema is written directly inside the check (previous/only behavior).
