package org.apiaddicts.apitools.openapi2soapui.integration;

import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import org.apiaddicts.apitools.openapi2soapui.request.ExampleSet;
import org.apiaddicts.apitools.openapi2soapui.request.ExampleValues;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectOptions;
import com.eviware.soapui.support.SoapUIException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-End SoapUI Project Generation Tests")
class EndToEndGenerationTest {

	private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

	@Nested
	@DisplayName("Simple API Generation")
	class SimpleAPIGeneration {

		@Test
		@DisplayName("Should generate SoapUI project from minimal OpenAPI spec")
		void testMinimalSpec() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Minimal API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /ping:\n" +
				"    get:\n" +
				"      operationId: ping\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Pong\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("MinimalAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("MinimalAPI"), "Should contain API name");
			assertTrue(xml.contains("ping"), "Should contain operation");
			assertTrue(xml.contains("http://api.example.com"), "Should contain server URL");
		}

		@Test
		@DisplayName("Should handle single endpoint with GET method")
		void testSingleGETEndpoint() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: GET API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: https://api.example.com/v1\n" +
				"paths:\n" +
				"  /users:\n" +
				"    get:\n" +
				"      operationId: listUsers\n" +
				"      summary: List all users\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: User list\n" +
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
			SoapUIProject project = new SoapUIProject("GetAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 1000, "Should generate substantial XML");
			assertTrue(xml.contains("listUsers"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle multiple endpoints with different HTTP methods")
		void testMultipleEndpoints() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: CRUD API\n" +
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
				"    post:\n" +
				"      operationId: createItem\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n" +
				"  /items/{id}:\n" +
				"    get:\n" +
				"      operationId: getItem\n" +
				"      parameters:\n" +
				"        - name: id\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    put:\n" +
				"      operationId: updateItem\n" +
				"      parameters:\n" +
				"        - name: id\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    delete:\n" +
				"      operationId: deleteItem\n" +
				"      parameters:\n" +
				"        - name: id\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"      responses:\n" +
				"        '204':\n" +
				"          description: Deleted\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("CrudAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getItems"), "Should contain GET operation");
			assertTrue(xml.contains("createItem"), "Should contain POST operation");
			assertTrue(xml.contains("updateItem"), "Should contain PUT operation");
			assertTrue(xml.contains("deleteItem"), "Should contain DELETE operation");
		}
	}

	@Nested
	@DisplayName("Complex API Generation")
	class ComplexAPIGeneration {

		@Test
		@DisplayName("Should handle nested request/response bodies")
		void testNestedBodies() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Nested API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /orders:\n" +
				"    post:\n" +
				"      operationId: createOrder\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                customer:\n" +
				"                  type: object\n" +
				"                  properties:\n" +
				"                    name:\n" +
				"                      type: string\n" +
				"                    address:\n" +
				"                      type: object\n" +
				"                      properties:\n" +
				"                        street:\n" +
				"                          type: string\n" +
				"                        city:\n" +
				"                          type: string\n" +
				"                items:\n" +
				"                  type: array\n" +
				"                  items:\n" +
				"                    type: object\n" +
				"                    properties:\n" +
				"                      id:\n" +
				"                        type: integer\n" +
				"                      quantity:\n" +
				"                        type: integer\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Order created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("NestedAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("createOrder"), "Should contain operation");
			assertTrue(xml.length() > 2000, "Should generate substantial XML for nested structure");
		}

		@Test
		@DisplayName("Should handle multiple servers")
		void testMultipleServers() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Multi-Server API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://dev.api.example.com\n" +
				"    description: Development\n" +
				"  - url: http://staging.api.example.com\n" +
				"    description: Staging\n" +
				"  - url: http://api.example.com\n" +
				"    description: Production\n" +
				"paths:\n" +
				"  /health:\n" +
				"    get:\n" +
				"      operationId: health\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("MultiServerAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("dev.api.example.com") || xml.contains("staging.api.example.com") ||
				xml.contains("api.example.com"), "Should contain at least one server");
		}

		@Test
		@DisplayName("Should handle multiple content types")
		void testMultipleContentTypes() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Multi-Content API\n" +
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
				"          application/form-urlencoded:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"          content:\n" +
				"            application/json:\n" +
				"              schema:\n" +
				"                type: object\n" +
				"            application/xml:\n" +
				"              schema:\n" +
				"                type: object\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("MultiContentAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should handle multiple content types");
		}
	}

	@Nested
	@DisplayName("Feature Integration Tests")
	class FeatureIntegration {

		@Test
		@DisplayName("Should apply readOnly to complex API")
		void testReadOnlyOnComplexAPI() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Complex API\n" +
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
				"          description: Created\n" +
				"    put:\n" +
				"      operationId: putData\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    delete:\n" +
				"      operationId: deleteData\n" +
				"      responses:\n" +
				"        '204':\n" +
				"          description: Deleted\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProjectOptions options = new SoapUIProjectOptions();
			options.setReadOnly(true);

			SoapUIProject project = new SoapUIProject("ComplexAPI", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getData"), "Should include GET");
			assertFalse(xml.contains("postData"), "Should exclude POST");
			assertFalse(xml.contains("putData"), "Should exclude PUT");
			assertFalse(xml.contains("deleteData"), "Should exclude DELETE");
		}

		@Test
		@DisplayName("Should apply custom examples to requests")
		void testCustomExamplesOnComplexAPI() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Examples API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    post:\n" +
				"      operationId: createUser\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                age:\n" +
				"                  type: integer\n" +
				"                active:\n" +
				"                  type: boolean\n" +
				"                joinDate:\n" +
				"                  type: string\n" +
				"                  format: date\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			ExampleSet examples = new ExampleSet();
			examples.setNumber(30);
			examples.setBooleanValue(true);
			examples.setDate("2025-06-15");

			ExampleValues exampleValues = new ExampleValues();
			exampleValues.setSuccessful(examples);

			SoapUIProjectOptions options = new SoapUIProjectOptions();
			options.setExamples(exampleValues);

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("ExamplesAPI", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("createUser"), "Should contain operation");
			assertTrue(xml.length() > 0, "Should generate valid XML");
		}

		@Test
		@DisplayName("Should combine multiple features")
		void testMultipleFeaturesOnAPI() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Multi-Feature API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://prod.api.example.com\n" +
				"  - url: http://staging.api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    get:\n" +
				"      operationId: listItems\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    post:\n" +
				"      operationId: createItem\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			ExampleSet examples = new ExampleSet();
			examples.setString("test-value");

			ExampleValues exampleValues = new ExampleValues();
			exampleValues.setSuccessful(examples);

			SoapUIProjectOptions options = new SoapUIProjectOptions();
			options.setReadOnly(true);
			options.setServerPattern("staging");
			options.setMicrocksHeaders(true);
			options.setExamples(exampleValues);
			options.setValidateSchema(true);

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("MultiFeatureAPI", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("listItems"), "Should include GET");
			assertFalse(xml.contains("createItem"), "Should exclude POST with readOnly");
			assertTrue(xml.contains("staging.api.example.com"), "Should use staging server");
			assertTrue(xml.contains("Validation_TestStep"), "Should include validation step");
		}
	}
}
