# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [2.0.0-beta-3]

### Added
- `examples.wrong` is now applied to the negative `CaseErrorRequired{Field}` cases: when any `wrong` value is configured, the target required field is sent with an invalid value from `wrong`.

### Fixed
- `allOf`/`$ref` body fields that appear in more than one generated test case no longer collapse to an empty placeholder. Each generated request body now resolves its `$ref`s with a fresh resolver.
- `microcksHeaders`: a custom header named `X-Microcks-Response-Name` supplied by the user is now preserved in the `CaseErrorStatusCode{StatusCode}`/`CaseErrorRequired{Field}` test cases instead of being overwritten by the computed example name.

## [2.0.0-beta-2] - 2026-07-30

### Fixed
- String fields with `format` `date-time`, `email`, `uuid`, `password`, `byte`, `binary` now generate a valid `type: string` schema (previously a malformed `type: object`) and apply the configured `examples` value (e.g. `dateTime`). `date` unchanged.
- `allOf` nested inside another `allOf` is now flattened recursively, so member fields are no longer dropped.

## [2.0.0-beta-1] - 2026-07-20

### Changed
- **Upgraded to Java 21 and Spring Boot 3.5.16** (from Java 11 / Spring Boot 2.5.2). Migrates the Jakarta EE namespace (`javax.validation` → `jakarta.validation`) and replaces `springdoc-openapi-ui` 1.5.6 with `springdoc-openapi-starter-webmvc-ui` 2.8.17. The request/response contract and the generated SoapUI project output are unchanged.
- **Dependency refresh:** SoapUI `5.6.0` → `5.9.1` (now on Log4j2 + Groovy 3), swagger-parser `2.1.39` → `2.1.45`, Lombok → `1.18.46`. SoapUI 5.9.1's `DefaultSoapUICore` requires the Log4j2 `log4j-core` backend, so `log4j-to-slf4j` is excluded from the Spring Boot starters (the app itself still logs via SLF4J → Logback); the `guava` and `wsdl4j` SoapUI exclusions were removed because 5.9.1 needs them at runtime.
- **Slimmer artifact:** excluded SoapUI 5.9.1's bundled telemetry (SmartBear analytics → Mixpanel / Segment / OkHttp / Kotlin) and its GraphQL support, both unused by headless REST/OpenAPI generation (~7 MB, 23 jars). `org.json` is now a direct dependency (`20260522`); it was previously present only transitively via that telemetry. Other heavy SoapUI transitives (BouncyCastle, RSyntaxTextArea, Rhino, HtmlUnit, TestNG, Saxon/Xalan, Jersey) are loaded by SoapUI's eager core initialization and must remain.
- **Deployment:** WAR deployment now requires a Jakarta / Servlet 6 container — **Tomcat 10.1+** (previously Tomcat 9). The Docker base image is now `eclipse-temurin:21-jdk`.
- Trailing-slash URL matching is disabled by default in Spring 6, so `POST .../soap-ui-projects/` (with a trailing slash) no longer matches; use `.../soap-ui-projects`.

## [1.1.0-beta-1] - 2026-07-15

### Added
- Test Case generation follows a new naming convention: Test Suite `{resourcePath}_{apiName}_{apiVersion}-{METHOD}-Suite` (run type `SEQUENTIAL`, `abortOnError` `false`), and exactly 4 test cases per method: `{METHOD}_CaseOkAllProperties` (every body property/query parameter, required and optional), `{METHOD}_CaseOkRequiredProperties` (only required body properties/query parameters), one `{METHOD}_CaseErrorStatusCode{StatusCode}` per documented non-2xx response, and one `{METHOD}_CaseErrorRequired{Field}` per required body property and required query parameter.
- `readOnly`: when `true`, only generates GET and OPTIONS test cases.
- `serverPattern`: selects the OpenAPI server whose URL matches the substring (e.g. `%dev%`); defaults to the first server if none match, and to the first declared server (not every server) when omitted entirely.
- `testCaseNames`: for each name, generates an extra `{METHOD}_Case{name}` test case identical to `CaseOkAllProperties` — for additional named "happy path" variants beyond the 2 fixed Ok* cases. Empty/unset (default) generates none.
- `minimalEndpoints`: when `false` (default), generates `{METHOD}_CaseErrorRequired{Field}` for every required body property (recursing into nested required objects) and every required query parameter. When `true`, collapses this to at most one such test case (the first required body property found, or the first required query parameter if the operation has no JSON body).
- `microcksHeaders`: when `true`, adds an `X-Microcks-Response-Name` header with the matching response example name (or `default`). For the `CaseErrorStatusCode{StatusCode}`/`CaseErrorRequired{Field}` test cases, the header is resolved against that specific status's (or `default`'s) response, rather than the operation's first 2xx response.
- `generateOneOfAnyOf`: when `true`, resolves `oneOf`/`anyOf` to their first candidate when generating example bodies (`allOf` is always merged).
- `examples`: optional `{ successful }` object with custom `string`/`number`/`boolean`/`date`/`dateTime`/`array`/`object` values used for example bodies and query-param values. Unset fields keep the existing defaults.
- `validateSchema`: when `true` (default), every generated test case's status-code assertion is joined by a response-schema assertion. When explicitly `false`, only the status-code assertion is added — no schema assertion is generated for any test case.
- `isInline`: when `false` (default), JSON request-body example values are generated as SoapUI Project Properties and referenced from the body via a `${#Project#...}` token instead of being embedded literally. When `true`, literal values are embedded directly in the body. Query parameter values are always embedded literally, regardless of this flag.
- `schemaIsInline`: when `false` (default), the JSON Schema used by a test case's schema assertion is stored separately as a SoapUI Project Property and looked up automatically when the test runs, instead of being written out in full inside the assertion. When `true`, the full schema is written directly inside the assertion.
- `schemaPrettyPrint`: when `true` (default), the JSON Schema used by a test case's schema assertion is pretty-printed (indented). When `false`, it is serialized compactly with no extra whitespace. Has no effect on JSON request-body example formatting.
- `hasScopes`: when `true`, generates one additional `{METHOD}_CaseOkScope{ProfileName}` test case per configured `oAuth2Profiles` entry beyond the first, each wired to that profile's own OAuth 2.0 authentication — independent of the default request, which always uses only the first profile. No-op when `oAuth2Profiles` is empty, not provided, or has only one entry.
- `applicationToken`: only relevant when `hasScopes` is `true`. When also `true`, generates one additional `{METHOD}_CaseOkApplicationToken{ProfileName}` test case per configured `oAuth2Profiles` entry whose `grantType` is `CLIENT_CREDENTIALS` (including the first), separate from the `hasScopes` scope variant test cases.
- `numberOfScopes`: only relevant when `hasScopes` is `true`. Total number of test cases wired to a profile-based scope credential — counting the default request and any extra scope-variant test cases — using the first `numberOfScopes` configured `oAuth2Profiles` entries in configured order. Values below 1 (unset, `0`, or negative) are treated as `1`: no extra test case is generated, since the default request alone already covers the first profile.
- `customAuthorizationsFile`: prepends a dedicated Test Suite named `authorizations_{apiName}_{apiVersion}-Suite`, with one `{method}_Case{name}` test case per entry (using each entry's own `method`), before the per-endpoint test suites.
