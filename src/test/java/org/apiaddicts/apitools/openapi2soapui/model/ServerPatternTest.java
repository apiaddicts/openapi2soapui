package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;

/**
 * When no pattern is given, serverPattern should default to only the first declared server, not every
 * declared server
 */
class ServerPatternTest {

	private static final String MULTI_SERVER_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"servers:",
			"  - url: https://dev.example.com/api",
			"  - url: https://prod.example.com/api",
			"paths:",
			"  /items:",
			"    get:",
			"      operationId: getItems",
			"      responses:",
			"        '200':",
			"          description: OK"
	);

	private OpenAPI parseSpec() {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(MULTI_SERVER_SPEC, null, null);
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());
		return result.getOpenAPI();
	}

	@Test
	void serverPatternOmitted_usesOnlyTheFirstDeclaredServer() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, null);

		String[] endpoints = soapUIProject.getRestService().getEndpoints();
		assertTrue(endpoints.length == 1 && "https://dev.example.com".equals(endpoints[0]),
				"With no serverPattern, only the first declared server should be used as an endpoint: " + String.join(",", endpoints));
	}

	@Test
	void serverPatternMatching_usesOnlyTheMatchedServer() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, "%prod%", true, false, false, false, false, false, null);

		String[] endpoints = soapUIProject.getRestService().getEndpoints();
		assertTrue(endpoints.length == 1 && "https://prod.example.com".equals(endpoints[0]), String.join(",", endpoints));
	}

	@Test
	void serverPatternWithNoMatch_fallsBackToTheFirstServer() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, "%staging%", true, false, false, false, false, false, null);

		String[] endpoints = soapUIProject.getRestService().getEndpoints();
		assertTrue(endpoints.length == 1 && "https://dev.example.com".equals(endpoints[0]), String.join(",", endpoints));
	}
}
