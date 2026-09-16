package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;
import org.junit.jupiter.api.Test;

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
		return SerializedDataUtils.parseOpenAPIContent(MULTI_SERVER_SPEC);
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
