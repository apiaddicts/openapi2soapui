package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;

/**
 * The X-Microcks-Response-Name header should be computed per generated request against that request's
 * own target status, rather than always using the operation's first 2xx (or default) response
 */
class MicrocksHeadersStatusTest {

	private static final String SPEC_WITH_400_EXAMPLE = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    post:",
			"      operationId: createItem",
			"      requestBody:",
			"        content:",
			"          application/json:",
			"            schema:",
			"              type: object",
			"              required: [id]",
			"              properties:",
			"                id:",
			"                  type: integer",
			"      responses:",
			"        '200':",
			"          description: OK",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"              examples:",
			"                successExample:",
			"                  value: {}",
			"        '400':",
			"          description: Bad Request",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"              examples:",
			"                badExample:",
			"                  value: {}"
	);

	private static final String SPEC_WITHOUT_400_EXAMPLE = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    post:",
			"      operationId: createItem",
			"      requestBody:",
			"        content:",
			"          application/json:",
			"            schema:",
			"              type: object",
			"              required: [id]",
			"              properties:",
			"                id:",
			"                  type: integer",
			"      responses:",
			"        '200':",
			"          description: OK",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"              examples:",
			"                successExample:",
			"                  value: {}"
	);

	private OpenAPI parseSpec(String yaml) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(yaml, null, null);
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());
		return result.getOpenAPI();
	}

	private String decode(String xml) {
		return xml.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
	}

	@Test
	void bodyPropertyVariants_useThe400ResponseExampleInsteadOfTheOperationsFirst2xx() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_400_EXAMPLE);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, true, false, false, false, false, null);
		String xml = decode(soapUIProject.getFileContent());

		int defaultRequestStart = xml.indexOf("name=\"Request 1\"");
		int missingIdRequestStart = xml.indexOf("name=\"missing id\"");
		assertTrue(defaultRequestStart >= 0 && missingIdRequestStart >= 0, xml);

		String defaultRequestBlock = xml.substring(defaultRequestStart, missingIdRequestStart);
		assertTrue(defaultRequestBlock.contains("X-Microcks-Response-Name") && defaultRequestBlock.contains("successExample"),
				"The default (success-path) request should use the first 2xx response's named example: " + defaultRequestBlock);
		assertFalse(defaultRequestBlock.contains("badExample"), defaultRequestBlock);

		String missingIdBlock = xml.substring(missingIdRequestStart, xml.indexOf("</con:request>", missingIdRequestStart));
		assertTrue(missingIdBlock.contains("badExample"),
				"The 400-targeting body-property variant should use the 400 response's own named example, not the operation's first 2xx: " + missingIdBlock);
		assertFalse(missingIdBlock.contains("successExample"), missingIdBlock);
	}

	@Test
	void bodyPropertyVariants_fallBackToLiteralDefaultWhenNo400ResponseIsDeclared() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITHOUT_400_EXAMPLE);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, true, false, false, false, false, null);
		String xml = decode(soapUIProject.getFileContent());

		int missingIdRequestStart = xml.indexOf("name=\"missing id\"");
		assertTrue(missingIdRequestStart >= 0, xml);
		String missingIdBlock = xml.substring(missingIdRequestStart, xml.indexOf("</con:request>", missingIdRequestStart));
		assertTrue(missingIdBlock.contains("value=\"default\""),
				"With no 400 (or default) response declared, the header should fall back to the literal string 'default': " + missingIdBlock);
	}
}
