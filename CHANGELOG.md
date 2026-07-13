# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.1.0-beta-1] - 2026-07-02

### Added
- `readOnly`: when `true`, only generates GET and OPTIONS test cases.
- `serverPattern`: selects the OpenAPI server whose URL matches the substring (e.g. `%dev%`); defaults to the first server if none match, and to the first declared server (not every server) when omitted entirely.
- `minimalEndpoints`: targets the JSON request body rather than query parameters. When `false` (default), generates a 400 test case per missing required body property (recursing into nested required objects) plus a 400 test case per body property given an invalid value. When `true`, collapses this to at most one missing-required-property test case and none for invalid values.
- `microcksHeaders`: when `true`, adds an `X-Microcks-Response-Name` header with the matching response example name (or `default`). For the `minimalEndpoints` body-property variant test cases (which target HTTP 400), the header is resolved against the operation's 400 (or `default`) response specifically, rather than its first 2xx response.
- `generateOneOfAnyOf`: when `true`, resolves `oneOf`/`anyOf` to their first candidate when generating example bodies (`allOf` is always merged).
- `examples`: optional `{ successful, wrong }` object with custom `string`/`number`/`boolean`/`date`/`dateTime`/`array`/`object` values. `successful` feeds example bodies and valid query-param values; `wrong` feeds the invalid body-property and query-param values from `minimalEndpoints`. Unset fields keep the existing defaults.
- `validateSchema`: when `true`, adds an automatic check (an assertion) to each main test case's request test step that verifies the response body matches the JSON Schema of the operation's first 2xx JSON response. Covers type, required, properties, items, enum, oneOf/anyOf, allOf, nullable, pattern, format (email/uuid/date/date-time), length/size/range bounds and additionalProperties. Skipped for operations that declare a `$select`/`$exclude` query parameter, since a partial response would not match the full schema.
- `isInline`: when `false` (default), JSON request-body example values are generated as SoapUI Project Properties and referenced from the body via a `${#Project#...}` token instead of being embedded literally. When `true`, literal values are embedded directly in the body (previous behavior).
- `schemaIsInline`: only relevant when `validateSchema` is `true`. When `false` (default), the JSON Schema used by that check is stored separately as a SoapUI Project Property and looked up automatically when the test runs, instead of being written out in full inside the check. When `true`, the full schema is written directly inside the check (previous/only behavior).
- `schemaPrettyPrint`: only relevant when `validateSchema` is `true`. When `true` (default), the JSON Schema used by that check is pretty-printed (indented). When `false`, it is serialized compactly with no extra whitespace. Has no effect on JSON request-body example formatting.
- `hasScopes`: when `true`, generates one additional test case per configured `oAuth2Profiles` entry beyond the first, each wired to that profile's own OAuth 2.0 authentication — independent of the default test case, which always uses only the first profile and is never duplicated by an extra test case for that same profile. No-op when `oAuth2Profiles` is empty, not provided, or has only one entry.
- `applicationToken`: only relevant when `hasScopes` is `true`. When also `true`, generates one additional test case per configured `oAuth2Profiles` entry whose `grantType` is `CLIENT_CREDENTIALS` (including the first), separate from the `hasScopes` scope variant test cases.
- `numberOfScopes`: only relevant when `hasScopes` is `true`. Total number of test cases wired to a profile-based scope credential — counting the default test case and any extra scope-variant test cases — using the first `numberOfScopes` configured `oAuth2Profiles` entries in configured order. Values below 1 (unset, `0`, or negative) are treated as `1`: no extra test case is generated, since the default test case alone already covers the first profile.
