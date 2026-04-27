# OpenAPI Version Support

This document outlines the OpenAPI specification versions supported by openapi2soapui.

## Supported Versions

### OpenAPI 3.0.x ✅
- **Status:** Fully supported
- **Versions:** 3.0.0, 3.0.1, 3.0.2, 3.0.3
- **Parser:** swagger-parser 2.1.19+
- **Features:**
  - Basic path/operation definitions
  - Request/response bodies
  - Schema references
  - Server selection
  - Parameter handling
  - All SoapUI project generation features

**Example:**
```yaml
openapi: 3.0.3
info:
  title: My API
  version: 1.0.0
servers:
  - url: https://api.example.com/v1
paths:
  /users:
    get:
      operationId: listUsers
      responses:
        '200':
          description: Success
```

### OpenAPI 3.1.x ✅
- **Status:** Fully supported
- **Versions:** 3.1.0+
- **Parser:** swagger-parser 2.1.19+ (required for 3.1 support)
- **Additional Features:**
  - JSON Schema 2020-12 support
  - Nullable types (`type: [string, null]`)
  - Enhanced examples handling
  - Webhook definitions
  - More flexible schema composition
  - Multiple content types per operation

**Key Differences from 3.0:**
- JSON Schema 2020-12 format for schemas
- Type can be an array: `type: [string, number, null]`
- Examples can be defined at component level
- Enhanced parameter definitions
- Better nullable type support

**Example:**
```yaml
openapi: 3.1.0
info:
  title: Modern API
  version: 2.0.0
servers:
  - url: https://api.example.com/v2
paths:
  /items:
    get:
      operationId: getItems
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                type: object
                properties:
                  id:
                    type: integer
                  name:
                    type: string
                  optional:
                    type: [string, null]  # Nullable in 3.1
```

### OpenAPI 3.2.x ⏳
- **Status:** Not yet released
- **ETA:** Future (as of April 2026)
- **Planned Support:** When OpenAPI 3.2 is officially released and swagger-parser is updated
- **Expected Changes:** Refinements to JSON Schema integration, possible webhooks improvements

## Version Detection

The OpenAPI version is automatically detected from the `openapi` field in your spec:

```yaml
openapi: 3.1.0  # Detected and handled appropriately
```

No configuration is needed — just submit your spec and the tool will parse it correctly.

## Feature Compatibility Across Versions

All openapi2soapui features work with both OpenAPI 3.0 and 3.1:

| Feature | 3.0.x | 3.1.x |
|---------|-------|-------|
| `readOnly` | ✅ | ✅ |
| `serverPattern` | ✅ | ✅ |
| `minimalEndpoints` | ✅ | ✅ |
| `microcksHeaders` | ✅ | ✅ |
| `generateOneOfAnyOf` | ✅ | ✅ |
| `examples` | ✅ | ✅ |
| `validateSchema` | ✅ | ✅ |

## Test Coverage

Comprehensive tests ensure compatibility across versions:

**File:** `src/test/java/org/apiaddicts/apitools/openapi2soapui/model/OpenAPIVersionSupportTest.java`

- **OpenAPI 3.0.x Tests:** 2 tests
  - Basic 3.0.0 parsing
  - 3.0.3 with parameters
  
- **OpenAPI 3.1.x Tests:** 5 tests
  - Basic 3.1.0 parsing
  - JSON Schema 2020-12 type arrays
  - Nullable type handling
  - Component-level examples
  - Multiple content types
  
- **Version-Agnostic Feature Tests:** 3 tests
  - readOnly with both 3.0 and 3.1
  - serverPattern with 3.1
  - Backward compatibility verification
  
- **Complex Schema Tests:** 3 tests
  - Deeply nested objects
  - Arrays of objects
  - Schema composition (allOf)
  
- **Forward Compatibility Tests:** 2 tests
  - 3.2 status documentation
  - Graceful handling of future versions

**Total:** 15 version support tests, all passing ✅

## Migration Guide

### From OpenAPI 3.0 to 3.1

If you're migrating from 3.0 to 3.1:

1. Update the version field:
   ```yaml
   # Before
   openapi: 3.0.3
   
   # After
   openapi: 3.1.0
   ```

2. Update schemas to use JSON Schema 2020-12:
   ```yaml
   # Before (3.0)
   schema:
     type: object
     properties:
       id:
         type: integer
       name:
         type: string

   # After (3.1) - can use nullable types
   schema:
     type: object
     properties:
       id:
         type: integer
       name:
         type: string
       optional:
         type: [string, null]  # New in 3.1
   ```

3. No changes needed for openapi2soapui — it handles both automatically

## Dependencies

**OpenAPI Parsing:**
```xml
<dependency>
    <groupId>io.swagger.parser.v3</groupId>
    <artifactId>swagger-parser</artifactId>
    <version>2.1.19</version>
</dependency>
```

- **Version 2.1.19+** supports OpenAPI 3.0.x and 3.1.x
- **Version 2.0.x** supports OpenAPI 3.0.x only (legacy)
- **Version 3.x** (future) may support OpenAPI 3.2.x

## Error Handling

If your OpenAPI spec has version-specific issues:

1. **Invalid YAML/JSON:** Parser will return error message
2. **Unsupported version:** Parser will attempt to read as closest compatible version
3. **Schema issues:** Validation errors during generation will be reported

## Best Practices

1. **Explicit Version:** Always specify the `openapi` version in your spec
2. **Consistent Schemas:** Keep schema definitions consistent within a version
3. **Test Your Specs:** Use the validation endpoint to test before generation
4. **Version in CI/CD:** Track OpenAPI version changes in your version control

## Troubleshooting

### "Parser returned null" error
- Verify your YAML/JSON is valid
- Ensure OpenAPI version is one of: 3.0.0, 3.0.1, 3.0.2, 3.0.3, 3.1.0+
- Check that required fields (info, paths) are present

### "Unknown schema property" errors
- For OpenAPI 3.0: Some JSON Schema 2020-12 features not supported
- For OpenAPI 3.1: Verify you're using 3.1-compatible schemas

### Performance with large specs
- Both 3.0 and 3.1 handle large specs efficiently
- No performance difference between versions

## References

- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [OpenAPI 3.1 Specification](https://spec.openapis.org/oas/v3.1.0)
- [swagger-parser Releases](https://github.com/swagger-api/swagger-parser/releases)
- [JSON Schema 2020-12](https://json-schema.org/draft/2020-12/json-schema-core.html)

---

**Last Updated:** 2026-04-27  
**Swagger Parser Version:** 2.1.19+  
**Test Coverage:** 15 tests (100% passing)
