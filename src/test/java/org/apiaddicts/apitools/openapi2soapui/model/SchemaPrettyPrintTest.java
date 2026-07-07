package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import groovy.lang.GroovyShell;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apiaddicts.apitools.openapi2soapui.request.ExampleValues;
import org.apiaddicts.apitools.openapi2soapui.request.ExamplesConfig;

class SchemaPrettyPrintTest {

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

	private static final String SPEC_WITH_BODY = String.join("\n",
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
			"              properties:",
			"                name:",
			"                  type: string",
			"                age:",
			"                  type: integer",
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

	private OpenAPI parseSpec(String spec) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(spec, null, null);
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());
		return result.getOpenAPI();
	}

	private ExamplesConfig examplesConfig() {
		ExampleValues successful = new ExampleValues();
		successful.setString("Ada");
		successful.setNumber(new BigDecimal(42));
		ExamplesConfig examples = new ExamplesConfig();
		examples.setSuccessful(successful);
		return examples;
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

	private void assertSchemaValidatesResponses(String script, String storedSchema) {
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
	void schemaPrettyPrintTrue_producesIndentedSchemaJson() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, true, null);
		String storedSchema = soapUIProject.getProject().getPropertyValue("schema1");

		assertTrue(storedSchema.contains("\n"), "Pretty-printed schema should span multiple lines: " + storedSchema);
		assertTrue(storedSchema.contains("\"type\" : \"object\""), "Pretty-printed schema should have spaced colons: " + storedSchema);

		assertSchemaValidatesResponses(extractScript(soapUIProject.getFileContent()), storedSchema);
	}

	@Test
	void schemaPrettyPrintFalse_producesCompactSchemaJson() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, false, null);
		String storedSchema = soapUIProject.getProject().getPropertyValue("schema1");

		assertFalse(storedSchema.contains("\n"), "Compact schema should be a single line: " + storedSchema);
		assertTrue(storedSchema.contains("\"type\":\"object\""), "Compact schema should have no whitespace around colons: " + storedSchema);

		assertSchemaValidatesResponses(extractScript(soapUIProject.getFileContent()), storedSchema);
	}

	@Test
	void schemaPrettyPrintUnset_defaultsToTrueViaLegacyConstructor() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, null);
		String storedSchema = soapUIProject.getProject().getPropertyValue("schema1");

		assertTrue(storedSchema.contains("\n"), "Default (unset) schemaPrettyPrint should keep the schema pretty-printed: " + storedSchema);
	}

	@Test
	void schemaPrettyPrintFalse_doesNotAffectRequestBodyExampleFormatting() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_BODY);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, true, false, examplesConfig());

		String storedSchema = soapUIProject.getProject().getPropertyValue("schema1");
		assertFalse(storedSchema.contains("\n"), "Schema should still be compact: " + storedSchema);

		String decoded = soapUIProject.getFileContent()
				.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
		int bodyStart = decoded.indexOf("\"age\": 42");
		assertTrue(bodyStart >= 0, "Request body example should still be embedded and pretty-printed: " + decoded);
		int bodyEnd = decoded.indexOf("\"Ada\"", bodyStart);
		assertTrue(bodyEnd >= 0, "Request body example should still be embedded and pretty-printed: " + decoded);
		String bodySnippet = decoded.substring(bodyStart, bodyEnd);
		assertTrue(bodySnippet.contains("\n"), "Request body example should remain pretty-printed even when schemaPrettyPrint is false: " + bodySnippet);
	}
}
