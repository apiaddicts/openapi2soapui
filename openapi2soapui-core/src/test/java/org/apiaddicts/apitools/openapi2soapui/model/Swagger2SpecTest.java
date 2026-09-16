package org.apiaddicts.apitools.openapi2soapui.model;

import io.swagger.v3.oas.models.OpenAPI;

import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Swagger2SpecTest {

	private static final String SWAGGER_2_SPEC = """
			swagger: "2.0"
			info:
			  title: Legacy Petstore
			  version: "1.0.0"
			host: petstore.swagger.io
			basePath: /v2
			schemes: [https]
			paths:
			  /pet/{petId}:
			    get:
			      operationId: getPetById
			      produces: [application/json]
			      parameters:
			        - name: petId
			          in: path
			          required: true
			          type: integer
			          format: int64
			      responses:
			        200:
			          description: ok
			          schema:
			            type: object
			            properties:
			              id: { type: integer, format: int64 }
			              name: { type: string }
			""";

	private static final String OPENAPI_3_SPEC = """
			openapi: 3.0.0
			info:
			  title: Legacy Petstore
			  version: "1.0.0"
			servers:
			  - url: https://petstore.swagger.io/v2
			paths:
			  /pet/{petId}:
			    get:
			      operationId: getPetById
			      parameters:
			        - name: petId
			          in: path
			          required: true
			          schema:
			            type: integer
			            format: int64
			      responses:
			        '200':
			          description: ok
			          content:
			            application/json:
			              schema:
			                type: object
			                properties:
			                  id: { type: integer, format: int64 }
			                  name: { type: string }
			""";

	@Test
	void readsSwagger2SpecAndConvertsItToOpenAPI3() {
		OpenAPI openAPI = SerializedDataUtils.parseOpenAPIContent(SWAGGER_2_SPEC);

		assertNotNull(openAPI, "a Swagger 2.0 spec must be readable");
		assertEquals("1.0.0", openAPI.getInfo().getVersion());
		assertEquals("https://petstore.swagger.io/v2", openAPI.getServers().get(0).getUrl());
		assertTrue(openAPI.getPaths().containsKey("/pet/{petId}"), "the declared path is missing");
	}

	@Test
	void swagger2AndOpenApi3SpecsOfTheSameApiGenerateTheSameProject() throws Exception {
		String fromV2 = generate(SWAGGER_2_SPEC);
		String fromV3 = generate(OPENAPI_3_SPEC);

		assertEquals(fromV3, fromV2, "the v2 spec must generate the same project as its v3 equivalent");
	}

	private String generate(String spec) throws Exception {
		OpenAPI openAPI = SerializedDataUtils.parseOpenAPIContent(spec);
		SoapUIProject project = new SoapUIProject("Legacy", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, null);
		try {
			return project.getFileContent().replaceAll(" id=\"[^\"]*\"", "");
		} finally {
			project.deleteTemporaryFile();
		}
	}
}
