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

@DisplayName("HTTP Method Handling Tests")
class HTTPMethodHandlingTest {

	private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

	@Nested
	@DisplayName("Standard HTTP Methods")
	class StandardMethods {

		@Test
		@DisplayName("Should handle GET requests")
		void testGETMethod() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: GET API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    get:\n" +
				"      operationId: getItems\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("GETAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getItems"), "Should contain GET operation");
		}

		@Test
		@DisplayName("Should handle POST requests with request body")
		void testPOSTMethod() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: POST API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    post:\n" +
				"      operationId: createItem\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                name:\n" +
				"                  type: string\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("POSTAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("createItem"), "Should contain POST operation");
		}

		@Test
		@DisplayName("Should handle PUT requests")
		void testPUTMethod() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: PUT API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items/{id}:\n" +
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
				"          description: Updated\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("PUTAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("updateItem"), "Should contain PUT operation");
		}

		@Test
		@DisplayName("Should handle PATCH requests")
		void testPATCHMethod() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: PATCH API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items/{id}:\n" +
				"    patch:\n" +
				"      operationId: patchItem\n" +
				"      parameters:\n" +
				"        - name: id\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Patched\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("PATCHAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("patchItem"), "Should contain PATCH operation");
		}

		@Test
		@DisplayName("Should handle DELETE requests")
		void testDELETEMethod() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: DELETE API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items/{id}:\n" +
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
			SoapUIProject project = new SoapUIProject("DELETEAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("deleteItem"), "Should contain DELETE operation");
		}

		@Test
		@DisplayName("Should handle HEAD requests")
		void testHEADMethod() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: HEAD API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    head:\n" +
				"      operationId: headItems\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("HEADAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should generate valid XML for HEAD request");
		}

		@Test
		@DisplayName("Should handle OPTIONS requests")
		void testOPTIONSMethod() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: OPTIONS API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    options:\n" +
				"      operationId: optionsItems\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("OPTIONSAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.length() > 0, "Should generate valid XML for OPTIONS request");
		}
	}

	@Nested
	@DisplayName("Multiple Methods on Same Path")
	class MultipleMethods {

		@Test
		@DisplayName("Should handle all CRUD methods on same endpoint")
		void testCRUDMethods() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: CRUD API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    get:\n" +
				"      operationId: list\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n" +
				"  /items/{id}:\n" +
				"    get:\n" +
				"      operationId: read\n" +
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
				"      operationId: update\n" +
				"      parameters:\n" +
				"        - name: id\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Updated\n" +
				"    delete:\n" +
				"      operationId: delete\n" +
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

			assertTrue(xml.contains("list"), "Should contain list");
			assertTrue(xml.contains("create"), "Should contain create");
			assertTrue(xml.contains("read"), "Should contain read");
			assertTrue(xml.contains("update"), "Should contain update");
			assertTrue(xml.contains("delete"), "Should contain delete");
		}
	}

	@Nested
	@DisplayName("Method Filtering with readOnly")
	class MethodFiltering {

		@Test
		@DisplayName("readOnly should exclude only write methods")
		void testReadOnlyFiltering() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Filter API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    get:\n" +
				"      operationId: read1\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    head:\n" +
				"      operationId: head1\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    options:\n" +
				"      operationId: options1\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    post:\n" +
				"      operationId: write1\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n" +
				"    put:\n" +
				"      operationId: write2\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    patch:\n" +
				"      operationId: write3\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n" +
				"    delete:\n" +
				"      operationId: write4\n" +
				"      responses:\n" +
				"        '204':\n" +
				"          description: Deleted\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProjectOptions options = new SoapUIProjectOptions();
			options.setReadOnly(true);

			SoapUIProject project = new SoapUIProject("FilterAPI", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("read1"), "Should include GET");
			assertFalse(xml.contains("write1"), "Should exclude POST");
			assertFalse(xml.contains("write2"), "Should exclude PUT");
			assertFalse(xml.contains("write3"), "Should exclude PATCH");
			assertFalse(xml.contains("write4"), "Should exclude DELETE");
		}

		@Test
		@DisplayName("readOnly should work with single read-only endpoint")
		void testReadOnlyWithSingleGET() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: ReadOnly API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /status:\n" +
				"    get:\n" +
				"      operationId: status\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: OK\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProjectOptions options = new SoapUIProjectOptions();
			options.setReadOnly(true);

			SoapUIProject project = new SoapUIProject("ReadOnlyAPI", openAPI, null, null, null, options);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("status"), "Should include GET operation");
		}
	}
}
