package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import groovy.lang.GroovyShell;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

/**
 * Throwaway end-to-end verification for the validateSchema Groovy script: builds a real SoapUIProject,
 * extracts the generated Script Assertion text from the actual serialized XML, and executes it with
 * GroovyShell against valid and invalid sample response bodies
 */
class ValidateSchemaScriptTest {

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
			"                required: [id, name]",
			"                properties:",
			"                  id:",
			"                    type: integer",
			"                  name:",
			"                    type: string",
			"                    minLength: 2",
			"                  email:",
			"                    type: string",
			"                    format: email",
			"                    nullable: true",
			"                  tags:",
			"                    type: array",
			"                    items:",
			"                      type: string",
			"                    uniqueItems: true"
	);

	@Test
	void generatedScriptIsValidGroovyAndValidatesCorrectly() throws Exception {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(SPEC, null, null);
		OpenAPI openAPI = result.getOpenAPI();
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, true, false, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Script Assertion"), "XML should contain the Script Assertion");
		assertTrue(xml.contains("JsonSlurper"), "XML should contain the embedded validator script");

		int start = xml.indexOf("def json = null");
		String marker = "errors.join(&apos;; &apos;)";
		int end = xml.indexOf(marker);
		if (end == -1) {
			marker = "errors.join('; ')";
			end = xml.indexOf(marker);
		}
		assertTrue(start >= 0 && end >= 0, "Should find the script bounds in the generated XML");
		String script = xml.substring(start, end + marker.length())
				.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");

		Map<String, Object> validResponse = new HashMap<>();
		validResponse.put("responseContent", "{\"id\":1,\"name\":\"Al\",\"email\":null,\"tags\":[\"a\",\"b\"]}");
		GroovyShell validShell = new GroovyShell();
		validShell.setVariable("messageExchange", validResponse);
		validShell.evaluate(script);

		Map<String, Object> invalidResponse = new HashMap<>();
		invalidResponse.put("responseContent", "{\"id\":1,\"name\":\"A\",\"email\":\"not-an-email\",\"tags\":[\"a\",\"a\"]}");
		GroovyShell invalidShell = new GroovyShell();
		invalidShell.setVariable("messageExchange", invalidResponse);
		AssertionError thrown = assertThrows(AssertionError.class, () -> invalidShell.evaluate(script));
		assertTrue(thrown.getMessage().contains("minLength"), "Should report the minLength violation: " + thrown.getMessage());
		assertTrue(thrown.getMessage().contains("email"), "Should report the email format violation: " + thrown.getMessage());
		assertTrue(thrown.getMessage().contains("unique"), "Should report the uniqueItems violation: " + thrown.getMessage());

		Map<String, Object> missingRequiredResponse = new HashMap<>();
		missingRequiredResponse.put("responseContent", "{\"name\":\"Ale\"}");
		GroovyShell missingShell = new GroovyShell();
		missingShell.setVariable("messageExchange", missingRequiredResponse);
		AssertionError missingThrown = assertThrows(AssertionError.class, () -> missingShell.evaluate(script));
		assertTrue(missingThrown.getMessage().contains("required property missing"), missingThrown.getMessage());

		Map<String, Object> notJsonResponse = new HashMap<>();
		notJsonResponse.put("responseContent", "<html>not json</html>");
		GroovyShell notJsonShell = new GroovyShell();
		notJsonShell.setVariable("messageExchange", notJsonResponse);
		AssertionError notJsonThrown = assertThrows(AssertionError.class, () -> notJsonShell.evaluate(script));
		assertTrue(notJsonThrown.getMessage().contains("A JSON response was expected"), notJsonThrown.getMessage());

		assertFalse(script.trim().isEmpty());
	}
}
