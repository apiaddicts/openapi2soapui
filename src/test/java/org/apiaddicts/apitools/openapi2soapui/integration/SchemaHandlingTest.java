package org.apiaddicts.apitools.openapi2soapui.integration;

import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import com.eviware.soapui.support.SoapUIException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Schema Handling Tests")
class SchemaHandlingTest {

	private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

	@Nested
	@DisplayName("Basic Data Types")
	class BasicDataTypes {

		@Test
		@DisplayName("Should handle string schema")
		void testStringSchema() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: String Schema API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: string\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("StringAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle numeric schemas")
		void testNumericSchemas() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Numeric Schema API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                count:\n" +
				"                  type: integer\n" +
				"                price:\n" +
				"                  type: number\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("NumericAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle boolean schema")
		void testBooleanSchema() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Boolean Schema API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /toggle:\n" +
				"    post:\n" +
				"      operationId: toggle\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                enabled:\n" +
				"                  type: boolean\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Updated\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("BooleanAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("toggle"), "Should contain operation");
		}
	}

	@Nested
	@DisplayName("Complex Schemas")
	class ComplexSchemas {

		@Test
		@DisplayName("Should handle nested objects")
		void testNestedObjects() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Nested API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                name:\n" +
				"                  type: string\n" +
				"                profile:\n" +
				"                  type: object\n" +
				"                  properties:\n" +
				"                    bio:\n" +
				"                      type: string\n" +
				"                    avatar:\n" +
				"                      type: object\n" +
				"                      properties:\n" +
				"                        url:\n" +
				"                          type: string\n" +
				"                        size:\n" +
				"                          type: integer\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("NestedAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
			assertTrue(xml.length() > 2000, "Should generate substantial XML");
		}

		@Test
		@DisplayName("Should handle array schemas")
		void testArraySchemas() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Array API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: array\n" +
				"              items:\n" +
				"                type: object\n" +
				"                properties:\n" +
				"                  id:\n" +
				"                    type: integer\n" +
				"                  name:\n" +
				"                    type: string\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("ArrayAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle schema composition with allOf")
		void testAllOfComposition() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: AllOf API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              allOf:\n" +
				"                - type: object\n" +
				"                  properties:\n" +
				"                    id:\n" +
				"                      type: integer\n" +
				"                - type: object\n" +
				"                  properties:\n" +
				"                    name:\n" +
				"                      type: string\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("AllOfAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}
	}

	@Nested
	@DisplayName("Schema Constraints")
	class SchemaConstraints {

		@Test
		@DisplayName("Should handle enum values")
		void testEnumValues() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Enum API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /orders:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                status:\n" +
				"                  type: string\n" +
				"                  enum: [pending, confirmed, shipped, delivered]\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("EnumAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle min/max constraints")
		void testMinMaxConstraints() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Constraint API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                quantity:\n" +
				"                  type: integer\n" +
				"                  minimum: 1\n" +
				"                  maximum: 100\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("ConstraintAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}
	}

	@Nested
	@DisplayName("Format Handling")
	class FormatHandling {

		@Test
		@DisplayName("Should handle date format")
		void testDateFormat() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Date Format API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /events:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                eventDate:\n" +
				"                  type: string\n" +
				"                  format: date\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("DateFormatAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle date-time format")
		void testDateTimeFormat() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: DateTime Format API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /events:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                timestamp:\n" +
				"                  type: string\n" +
				"                  format: date-time\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("DateTimeAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle email format")
		void testEmailFormat() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Email Format API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    post:\n" +
				"      operationId: create\n" +
				"      requestBody:\n" +
				"        content:\n" +
				"          application/json:\n" +
				"            schema:\n" +
				"              type: object\n" +
				"              properties:\n" +
				"                email:\n" +
				"                  type: string\n" +
				"                  format: email\n" +
				"      responses:\n" +
				"        '201':\n" +
				"          description: Created\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("EmailFormatAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("create"), "Should contain operation");
		}
	}
}
