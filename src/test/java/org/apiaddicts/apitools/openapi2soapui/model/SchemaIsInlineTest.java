package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import groovy.lang.GroovyShell;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

class SchemaIsInlineTest {

	private static final String SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /users:",
			"    get:",
			"      operationId: getUsers",
			"      responses:",
			"        '200':",
			"          description: OK",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"                required: [id]",
			"                properties:",
			"                  id:",
			"                    type: integer",
			"                  name:",
			"                    type: string"
	);

	private OpenAPI parseSpec() {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(SPEC, null, null);
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());
		return result.getOpenAPI();
	}

	private String extractScript(String xml) {
		int start = xml.indexOf("def json = null");
		String marker = "errors.join(&apos;; &apos;)";
		int end = xml.indexOf(marker);
		if (end == -1) {
			marker = "errors.join('; ')";
			end = xml.indexOf(marker);
		}
		assertTrue(start >= 0 && end >= 0, "Should find the script bounds in the generated XML");
		return xml.substring(start, end + marker.length())
				.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
	}

	private Object fakeContext(String expandedValue) {
		return new Object() {
			@SuppressWarnings("unused")
			public String expand(String token) {
				return expandedValue;
			}
		};
	}

	@Test
	void schemaIsInlineFalse_referencesProjectPropertyAndValidatesAtRuntime() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, null);
		String xml = soapUIProject.getFileContent();
		String script = extractScript(xml);

		assertTrue(script.contains("context.expand('${#Project#schema1}')"), "Script should reference the schema via a Project Property token: " + script);
		assertFalse(script.contains("parseText('''"), "Script should not embed the schema literally: " + script);

		String storedSchema = soapUIProject.getProject().getPropertyValue("schema1");
		assertTrue(storedSchema != null && storedSchema.contains("\"id\""), "Schema JSON should be stored as a Project Property: " + storedSchema);

		Map<String, Object> validResponse = new HashMap<>();
		validResponse.put("responseContent", "{\"id\":1,\"name\":\"Al\"}");
		GroovyShell validShell = new GroovyShell();
		validShell.setVariable("messageExchange", validResponse);
		validShell.setVariable("context", fakeContext(storedSchema));
		validShell.evaluate(script);

		Map<String, Object> invalidResponse = new HashMap<>();
		invalidResponse.put("responseContent", "{\"name\":\"Al\"}");
		GroovyShell invalidShell = new GroovyShell();
		invalidShell.setVariable("messageExchange", invalidResponse);
		invalidShell.setVariable("context", fakeContext(storedSchema));
		AssertionError thrown = assertThrows(AssertionError.class, () -> invalidShell.evaluate(script));
		assertTrue(thrown.getMessage().contains("required property missing"), thrown.getMessage());
	}

	@Test
	void schemaIsInlineTrue_embedsSchemaLiterally() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, true, false, null);
		String xml = soapUIProject.getFileContent();
		String script = extractScript(xml);

		assertTrue(script.contains("parseText('''"), "Script should embed the schema literally: " + script);
		assertFalse(script.contains("context.expand("), "Script should not reference a Project Property: " + script);
		assertNull(soapUIProject.getProject().getPropertyValue("schema1"), "No schema Project Property should be registered");

		Map<String, Object> validResponse = new HashMap<>();
		validResponse.put("responseContent", "{\"id\":1,\"name\":\"Al\"}");
		GroovyShell validShell = new GroovyShell();
		validShell.setVariable("messageExchange", validResponse);
		validShell.evaluate(script);
	}
}
