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

@DisplayName("Parameter Handling Tests")
class ParameterHandlingTest {

	private final OpenAPIV3Parser parser = new OpenAPIV3Parser();

	@Nested
	@DisplayName("Path Parameters")
	class PathParameters {

		@Test
		@DisplayName("Should handle single path parameter")
		void testSinglePathParameter() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Path Param API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users/{userId}:\n" +
				"    get:\n" +
				"      operationId: getUser\n" +
				"      parameters:\n" +
				"        - name: userId\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: User\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("PathParamAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getUser"), "Should contain operation");
			assertTrue(xml.length() > 0, "Should generate valid XML");
		}

		@Test
		@DisplayName("Should handle multiple path parameters")
		void testMultiplePathParameters() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Multi Path Param API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /organizations/{orgId}/projects/{projectId}/members/{memberId}:\n" +
				"    get:\n" +
				"      operationId: getMember\n" +
				"      parameters:\n" +
				"        - name: orgId\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: projectId\n" +
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
				"          description: Member\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("MultiPathAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getMember"), "Should contain operation");
		}
	}

	@Nested
	@DisplayName("Query Parameters")
	class QueryParameters {

		@Test
		@DisplayName("Should handle single query parameter")
		void testSingleQueryParameter() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Query Param API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    get:\n" +
				"      operationId: listUsers\n" +
				"      parameters:\n" +
				"        - name: limit\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Users\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("QueryParamAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("listUsers"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle multiple query parameters")
		void testMultipleQueryParameters() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Multi Query API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /users:\n" +
				"    get:\n" +
				"      operationId: search\n" +
				"      parameters:\n" +
				"        - name: search\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: limit\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"        - name: offset\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"        - name: sortBy\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: string\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Users\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("MultiQueryAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("search"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle required and optional query parameters")
		void testRequiredAndOptionalParameters() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Required Param API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /search:\n" +
				"    get:\n" +
				"      operationId: search\n" +
				"      parameters:\n" +
				"        - name: q\n" +
				"          in: query\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: filter\n" +
				"          in: query\n" +
				"          required: false\n" +
				"          schema:\n" +
				"            type: string\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Results\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("RequiredParamAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("search"), "Should contain operation");
		}
	}

	@Nested
	@DisplayName("Header Parameters")
	class HeaderParameters {

		@Test
		@DisplayName("Should handle header parameters")
		void testHeaderParameters() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Header API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /data:\n" +
				"    get:\n" +
				"      operationId: getData\n" +
				"      parameters:\n" +
				"        - name: X-API-Key\n" +
				"          in: header\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: Accept-Language\n" +
				"          in: header\n" +
				"          schema:\n" +
				"            type: string\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Data\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("HeaderAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("getData"), "Should contain operation");
		}
	}

	@Nested
	@DisplayName("Mixed Parameter Types")
	class MixedParameters {

		@Test
		@DisplayName("Should handle path, query, and header parameters together")
		void testMixedParameters() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Mixed Param API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /orgs/{orgId}/repos:\n" +
				"    get:\n" +
				"      operationId: listRepos\n" +
				"      parameters:\n" +
				"        - name: orgId\n" +
				"          in: path\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: type\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: string\n" +
				"            enum: [all, owner, member]\n" +
				"        - name: sort\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: Authorization\n" +
				"          in: header\n" +
				"          required: true\n" +
				"          schema:\n" +
				"            type: string\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Repositories\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("MixedParamAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("listRepos"), "Should contain operation");
			assertTrue(xml.length() > 1000, "Should generate substantial XML");
		}
	}

	@Nested
	@DisplayName("Parameter Data Types")
	class ParameterDataTypes {

		@Test
		@DisplayName("Should handle various parameter data types")
		void testParameterDataTypes() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.1.0\n" +
				"info:\n" +
				"  title: Data Type API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    get:\n" +
				"      operationId: search\n" +
				"      parameters:\n" +
				"        - name: stringParam\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: string\n" +
				"        - name: intParam\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: integer\n" +
				"        - name: numberParam\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: number\n" +
				"        - name: boolParam\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: boolean\n" +
				"        - name: dateParam\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: string\n" +
				"            format: date\n" +
				"        - name: dateTimeParam\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: string\n" +
				"            format: date-time\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Results\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("DataTypeAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("search"), "Should contain operation");
		}

		@Test
		@DisplayName("Should handle array parameters")
		void testArrayParameters() throws IOException, XmlException, SoapUIException {
			String spec = "openapi: 3.0.0\n" +
				"info:\n" +
				"  title: Array Param API\n" +
				"  version: 1.0.0\n" +
				"servers:\n" +
				"  - url: http://api.example.com\n" +
				"paths:\n" +
				"  /items:\n" +
				"    get:\n" +
				"      operationId: filter\n" +
				"      parameters:\n" +
				"        - name: tags\n" +
				"          in: query\n" +
				"          schema:\n" +
				"            type: array\n" +
				"            items:\n" +
				"              type: string\n" +
				"      responses:\n" +
				"        '200':\n" +
				"          description: Items\n";

			OpenAPI openAPI = parser.readContents(spec).getOpenAPI();
			SoapUIProject project = new SoapUIProject("ArrayParamAPI", openAPI, null, null, null, null);
			String xml = project.getFileContent();
			project.deleteTemporaryFile();

			assertTrue(xml.contains("filter"), "Should contain operation");
		}
	}
}
