package org.apiaddicts.apitools.openapi2soapui.model;

import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectOptions;
import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;
import com.eviware.soapui.support.SoapUIException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAPI Version Support Tests")
class OpenAPIVersionSupportTest {

	private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

	@Nested
	@DisplayName("OpenAPI 3.0.x Support")
	class OpenAPI30Support {

		@Test
		@DisplayName("Should parse OpenAPI 3.0.0 spec")
		void testParseOpenAPI30() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Test API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com/v1\n" +
				"paths:\n" +
				"  /users:\n" +
				"    get:\n" +
				"      operationId: listUsers\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: object\n" +
				"                properties:\n" +
				"                  id:\n" +
				"                    type: integer\n" +
				"                  name:\n" +
				"                    type: string\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			assertNotNull(openAPI, "OpenAPI 3.0.0 spec should parse successfully");
			assertEquals("3.0.0", openAPI.getOpenapi(), "Should detect OpenAPI version 3.0.0");

			SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("listUsers"), "Generated XML should contain operation");
			assertTrue(xml.contains("TestAPI"), "Generated XML should contain API name");
		}

		@Test
		@DisplayName("Should parse OpenAPI 3.0.3 spec")
		void testParseOpenAPI303() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.3\n" +
				"info:\n" +
				"  title: Products API\n" +
				"  version: 2.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /products:\n" +
				"    get:\n" +
				"      operationId: getProducts\n" +
				"      parameters:\n" +
				"        - name: limit\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Success\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: array\n" +
				"                items:\n" +
				"                  type: object\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			assertNotNull(openAPI, "OpenAPI 3.0.3 spec should parse successfully");
			assertEquals("3.0.3", openAPI.getOpenapi(), "Should detect OpenAPI version 3.0.3");

			SoapUIProject project = new SoapUIProject("ProductsAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should generate valid XML");
			assertTrue(xml.contains("getProducts"), "Generated XML should contain operation");
		}
	}

	@Nested
	@DisplayName("OpenAPI 3.1.x Support")
	class OpenAPI31Support {

		@Test
		@DisplayName("Should parse OpenAPI 3.1.0 spec")
		void testParseOpenAPI310() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Modern API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com/v1\n" +
				"paths:\n" +
				"  /items:\n" +
				"    get:\n" +
				"      operationId: listItems\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Success\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: object\n" +
				"                properties:\n" +
				"                  id:\n" +
				"                    type: integer\n" +
				"                  name:\n" +
				"                    type: string\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			assertNotNull(openAPI, "OpenAPI 3.1.0 spec should parse successfully");
			assertEquals("3.1.0", openAPI.getOpenapi(), "Should detect OpenAPI version 3.1.0");

			SoapUIProject project = new SoapUIProject("ModernAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("listItems"), "Generated XML should contain operation");
			assertTrue(xml.length() > 0, "Should generate valid XML");
		}

		@Test
		@DisplayName("Should handle OpenAPI 3.1.0 with JSON Schema 2020-12")
		void testOpenAPI310WithJsonSchema202012() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: JSON Schema API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    post:\n" +
				"      operationId: postData\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                value:\n" +
				"                  type: [string, number]\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: object\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			assertNotNull(openAPI, "OpenAPI 3.1.0 with JSON Schema should parse successfully");

			SoapUIProject project = new SoapUIProject("JSONSchemaAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("postData"), "Generated XML should contain operation");
		}

		@Test
		@DisplayName("Should handle OpenAPI 3.1.0 with nullable types")
		void testOpenAPI310WithNullableTypes() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Nullable API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /nullable:\n" +
				"    get:\n" +
				"      operationId: getNullable\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: object\n" +
				"                properties:\n" +
				"                  optionalField:\n" +
				"                    type: [string, null]\n" +
				"                  requiredField:\n" +
				"                    type: string\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			assertNotNull(openAPI, "OpenAPI 3.1.0 with nullable types should parse");

			SoapUIProject project = new SoapUIProject("NullableAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should generate valid XML with nullable types");
		}

		@Test
		@DisplayName("Should handle OpenAPI 3.1.0 with examples at component level")
		void testOpenAPI310WithExamples() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Examples API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    get:\n" +
				"      operationId: getUsers\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Success\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                $ref: '#/components/schemas/User'\n" +
				"components:\n" +
				"  schemas:\n" +
				"    User:\n" +
				"      type: object\n" +
				"      properties:\n" +
				"        id:\n" +
				"          type: integer\n" +
				"          example: 123\n" +
				"        name:\n" +
				"          type: string\n" +
				"          example: John Doe\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			assertNotNull(openAPI, "OpenAPI 3.1.0 with examples should parse");

			SoapUIProject project = new SoapUIProject("ExamplesAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getUsers"), "Should contain operation");
		}

		@Test
		@DisplayName("Should parse OpenAPI 3.1.0 with multiple content types")
		void testOpenAPI310WithMultipleContentTypes() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Multi Content API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    post:\n" +
				"      operationId: postData\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"          application/xml:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: object\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			assertNotNull(openAPI, "Should parse multiple content types");

			SoapUIProject project = new SoapUIProject("MultiAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should generate valid XML");
		}
	}

	@Nested
	@DisplayName("Version-Agnostic Feature Support")
	class VersionAgnosticFeatures {

		@Test
		@DisplayName("readOnly option should work with OpenAPI 3.0.0")
		void testReadOnlyWith300() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Test API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    get:\n" +
				"      operationId: getData\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    post:\n" +
				"      operationId: postData\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProjectOptions options = new SoapUIProjectOptions();
			options.setReadOnly(true);

			SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getData"), "Should contain GET operation");
			assertFalse(xml.contains("postData"), "Should exclude POST operation with readOnly");
		}

		@Test
		@DisplayName("readOnly option should work with OpenAPI 3.1.0")
		void testReadOnlyWith310() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Test API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    get:\n" +
				"      operationId: getData\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    put:\n" +
				"      operationId: updateData\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Updated\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProjectOptions options = new SoapUIProjectOptions();
			options.setReadOnly(true);

			SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getData"), "Should contain GET operation");
			assertFalse(xml.contains("updateData"), "Should exclude PUT operation with readOnly");
		}

		@Test
		@DisplayName("serverPattern should work with OpenAPI 3.1.0")
		void testServerPatternWith310() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Multi-Server API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://prod.example.com\n" +
				"  - url: http://staging.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    get:\n" +
				"      operationId: getItems\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProjectOptions options = new SoapUIProjectOptions();
			options.setServerPattern("staging");

			SoapUIProject project = new SoapUIProject("API", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("staging.example.com"), "Should use staging server");
			assertFalse(xml.contains("prod.example.com"), "Should not use production server");
		}
	}

	@Nested
	@DisplayName("OpenAPI 3.2.x Support")
	class OpenAPI32Support {

		@Test
		@DisplayName("Should parse OpenAPI 3.2.0 spec")
		void testParseOpenAPI320() throws IOException, XmlException, SoapUIException {
			String spec = """
				openapi: 3.2.0
				info:
				  title: OpenAPI 32 API
				  version: 1.0.0
				servers:
				  - url: http://api.example.com/v1
				paths:
				  /status:
				    get:
				      operationId: getStatus
				      responses:
				        '200':
				          description: OK
				          content:
				            application/json:
				              schema:
				                type: object
				""";

			OpenAPI openAPI = SerializedDataUtils.parseOpenAPIContent(spec);
			assertNotNull(openAPI, "OpenAPI 3.2.0 spec should parse successfully");
			assertEquals("3.1.0", openAPI.getOpenapi(), "3.2 spec should be normalized to parser-compatible version");

			SoapUIProject project = new SoapUIProject("OpenAPI32API", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getStatus"), "Generated XML should contain operation");
			assertFalse(xml.isEmpty(), "Should generate valid XML");
		}

		@Test
		@DisplayName("Should handle OpenAPI 3.2 querystring parameter")
		void testOpenAPI32QuerystringParameterSupport() throws IOException, XmlException, SoapUIException {
			String spec = """
				openapi: 3.2.0
				info:
				  title: Querystring API
				  version: 1.0.0
				servers:
				  - url: http://api.example.com
				paths:
				  /test:
				    get:
				      operationId: testQuerystring
				      parameters:
				        - name: rawQuery
				          in: querystring
				          required: false
				          content:
				            application/x-www-form-urlencoded:
				              schema:
				                type: string
				      responses:
				        '200':
				          description: OK
				""";

			OpenAPI openAPI = SerializedDataUtils.parseOpenAPIContent(spec);
			assertNotNull(openAPI, "OpenAPI 3.2 with querystring parameter should parse successfully");

			SoapUIProject project = new SoapUIProject("QuerystringAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("testQuerystring"), "Generated XML should contain operation");
			assertFalse(xml.isEmpty(), "Should generate valid XML");
		}
	}

	@Nested
	@DisplayName("Complex Schema Support Across Versions")
	class ComplexSchemaSupport {

		@Test
		@DisplayName("Should handle deeply nested objects in OpenAPI 3.1.0")
		void testDeeplyNestedObjects() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Nested API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /nested:\n" +
				"    get:\n" +
				"      operationId: getNested\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: object\n" +
				"                properties:\n" +
				"                  level1:\n" +
				"                    type: object\n" +
				"                    properties:\n" +
				"                      level2:\n" +
				"                        type: object\n" +
				"                        properties:\n" +
				"                          level3:\n" +
				"                            type: string\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("NestedAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should handle deeply nested structures");
		}

		@Test
		@DisplayName("Should handle arrays of objects in OpenAPI 3.1.0")
		void testArrayOfObjects() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Array API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    get:\n" +
				"      operationId: getItems\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: array\n" +
				"                items:\n" +
				"                  type: object\n" +
				"                  properties:\n" +
				"                    id:\n" +
				"                      type: integer\n" +
				"                    name:\n" +
				"                      type: string\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("ArrayAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should handle arrays of objects");
		}

		@Test
		@DisplayName("Should handle allOf composition in OpenAPI 3.1.0")
		void testAllOfComposition() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Composition API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    get:\n" +
				"      operationId: getUsers\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                allOf:\n" +
				"                  - type: object\n" +
				"                    properties:\n" +
				"                      id:\n" +
				"                        type: integer\n" +
				"                  - type: object\n" +
				"                    properties:\n" +
				"                      name:\n" +
				"                        type: string\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("CompositionAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should handle allOf composition");
		}
	}
}
