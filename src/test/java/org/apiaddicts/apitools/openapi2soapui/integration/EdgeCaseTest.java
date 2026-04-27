package org.apiaddicts.apitools.openapi2soapui.integration;

import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectOptions;
import com.eviware.soapui.support.SoapUIException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Edge Case and Error Handling Tests")
class EdgeCaseTest {

	private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

	@Nested
	@DisplayName("Minimal/Empty Specs")
	class MinimalSpecs {

		@Test
		@DisplayName("Should handle minimal valid spec with single endpoint")
		void testMinimalSpec() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Minimal\n" +
				"  version: 1.0\n" +
				"servers:\n" +
				"  - url: http://localhost\n" +
				"paths:\n" +
				"  /test:\n" +
				"    get:\n" +
				"      operationId: test\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			assertNotNull(openAPI, "Should parse minimal spec");

			SoapUIProject project = new SoapUIProject("Minimal", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("test"), "Should generate valid project");
		}
	}

	@Nested
	@DisplayName("Special Characters and Encoding")
	class SpecialCharacters {

		@Test
		@DisplayName("Should handle special characters in operation names")
		void testSpecialCharactersInNames() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Special Chars API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    post:\n" +
				"      operationId: createUser\n" +
				"      summary: Create a new user (with 'special' chars)\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: User created successfully\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("SpecialCharsAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("createUser"), "Should handle operation with special chars");
		}

		@Test
		@DisplayName("Should handle Unicode characters in descriptions")
		void testUnicodeCharacters() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Unicode API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    get:\n" +
				"      operationId: getUsers\n" +
				"      summary: Get users (获取用户)\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Success (成功)\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("UnicodeAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getUsers"), "Should handle Unicode characters");
		}
	}

	@Nested
	@DisplayName("Large and Complex Specs")
	class LargeAndComplexSpecs {

		@Test
		@DisplayName("Should handle spec with many endpoints")
		void testManyEndpoints() throws IOException, XmlException, SoapUIException {
			StringBuilder spec = new StringBuilder("openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Many Endpoints API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n");

			// Generate 20 endpoints
			for (int i = 1; i <= 20; i++) {
				spec.append("  /endpoint").append(i).append(":\n");
				spec.append("    get:\n");
				spec.append("      operationId: endpoint").append(i).append("\n");
				spec.append("      responses:\n");
				spec.append("        '200':\n");
				spec.append("          description: OK\n");
			}

			OpenAPI openAPI = parser.readContents(spec.toString()).getOpenAPI();
			SoapUIProject project = new SoapUIProject("ManyEndpointsAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 5000, "Should handle many endpoints");
			for (int i = 1; i <= 20; i++) {
				assertTrue(xml.contains("endpoint" + i), "Should contain endpoint" + i);
			}
		}

		@Test
		@DisplayName("Should handle deeply nested schemas")
		void testDeeplyNestedSchemas() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Deeply Nested API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    post:\n" +
				"      operationId: post\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                level1:\n" +
				"                  type: object\n" +
				"                  properties:\n" +
				"                    level2:\n" +
				"                      type: object\n" +
				"                      properties:\n" +
				"                        level3:\n" +
				"                          type: object\n" +
				"                          properties:\n" +
				"                            level4:\n" +
				"                              type: object\n" +
				"                              properties:\n" +
				"                                level5:\n" +
				"                                  type: object\n" +
				"                                  properties:\n" +
				"                                    value:\n" +
				"                                      type: string\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("DeeplyNestedAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 1000, "Should handle deeply nested structures");
		}
	}

	@Nested
	@DisplayName("Response Code Variations")
	class ResponseCodeVariations {

		@Test
		@DisplayName("Should handle various HTTP response codes")
		void testVariousResponseCodes() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Response Codes API\n" +
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
				"        '201':\n" +
				"          description: Created\n" +
				"        '204':\n" +
				"          description: No Content\n" +
				"        '301':\n" +
				"          description: Moved Permanently\n" +
				"        '304':\n" +
				"          description: Not Modified\n" +
				"        '400':\n" +
				"          description: Bad Request\n" +
				"        '401':\n" +
				"          description: Unauthorized\n" +
				"        '403':\n" +
				"          description: Forbidden\n" +
				"        '404':\n" +
				"          description: Not Found\n" +
				"        '500':\n" +
				"          description: Internal Server Error\n" +
				"        '503':\n" +
				"          description: Service Unavailable\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("ResponseCodesAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getData"), "Should contain operation");
		}
	}

	@Nested
	@DisplayName("Empty/Null Options Handling")
	class OptionsHandling {

		@Test
		@DisplayName("Should handle null options gracefully")
		void testNullOptions() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Test\n" +
				"  version: 1.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /test:\n" +
				"    get:\n" +
				"      operationId: test\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("test"), "Should handle null options");
		}

		@Test
		@DisplayName("Should handle empty options object")
		void testEmptyOptions() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Test\n" +
				"  version: 1.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /test:\n" +
				"    get:\n" +
				"      operationId: test\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProjectOptions options = new SoapUIProjectOptions();

			SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("test"), "Should handle empty options");
		}
	}

	@Nested
	@DisplayName("Path Variations")
	class PathVariations {

		@Test
		@DisplayName("Should handle root path")
		void testRootPath() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Root Path API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /:\n" +
				"    get:\n" +
				"      operationId: root\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("RootPathAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("root"), "Should handle root path");
		}

		@Test
		@DisplayName("Should handle paths with multiple path parameters")
		void testComplexPathStructure() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Complex Path API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /v1/orgs/{orgId}/teams/{teamId}/members/{memberId}/permissions:\n" +
				"    get:\n" +
				"      operationId: getPermissions\n" +
				"      parameters:\n" +
				"        - name: orgId\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: teamId\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: memberId\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: string\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Permissions\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("ComplexPathAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getPermissions"), "Should handle complex path");
		}
	}

	@Nested
	@DisplayName("Content Type Variations")
	class ContentTypeVariations {

		@Test
		@DisplayName("Should handle multiple response content types")
		void testMultipleResponseContentTypes() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Multi Response Content API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    get:\n" +
				"      operationId: getData\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Success\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: object\n" +
				"            application/xml:\n" +
				"              schema:\n" +
				"                type: object\n" +
				"            text/plain:\n" +
				"              schema:\n" +
				"                type: string\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("MultiResponseAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getData"), "Should handle multiple response content types");
		}
	}
}
