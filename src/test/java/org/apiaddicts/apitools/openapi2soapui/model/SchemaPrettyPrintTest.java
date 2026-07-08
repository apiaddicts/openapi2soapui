package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final String SIMPLE_ITEMS_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    get:",
			"      operationId: getItems",
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
			"                    type: integer"
	);

	private static final String ENUM_WITH_BACKSLASH_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    get:",
			"      operationId: getItems",
			"      responses:",
			"        '200':",
			"          description: OK",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"                required: [id, path]",
			"                properties:",
			"                  id:",
			"                    type: integer",
			"                  path:",
			"                    type: string",
			"                    enum: ['C:\\Users\\a', 'C:\\Users\\b']"
	);

	private static final String TWO_OPERATIONS_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    get:",
			"      operationId: getItems",
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
			"  /orders:",
			"    get:",
			"      operationId: getOrders",
			"      responses:",
			"        '200':",
			"          description: OK",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"                required: [total]",
			"                properties:",
			"                  total:",
			"                    type: number"
	);

	private static final String COMPLEX_NESTED_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /orders:",
			"    get:",
			"      operationId: getOrders",
			"      responses:",
			"        '200':",
			"          description: OK",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"                required: [id, items]",
			"                properties:",
			"                  id:",
			"                    type: integer",
			"                  items:",
			"                    type: array",
			"                    items:",
			"                      type: object",
			"                      required: [sku]",
			"                      properties:",
			"                        sku:",
			"                          type: string",
			"                        payment:",
			"                          oneOf:",
			"                            - type: object",
			"                              required: [cardNumber]",
			"                              properties:",
			"                                cardNumber:",
			"                                  type: string",
			"                            - type: object",
			"                              required: [accountId]",
			"                              properties:",
			"                                accountId:",
			"                                  type: string"
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

	private String decode(String s) {
		return s.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
	}

	private List<String> extractAllScripts(String xml) {
		List<String> scripts = new ArrayList<>();
		int searchFrom = 0;
		while (true) {
			int start = xml.indexOf("def json = null", searchFrom);
			if (start < 0) break;
			String marker = "errors.join(&apos;; &apos;)";
			int end = xml.indexOf(marker, start);
			if (end == -1) {
				marker = "errors.join('; ')";
				end = xml.indexOf(marker, start);
			}
			assertTrue(end >= 0, "Should find the script end marker");
			scripts.add(decode(xml.substring(start, end + marker.length())));
			searchFrom = end + marker.length();
		}
		return scripts;
	}

	private String extractPropertyKey(String script) {
		Matcher matcher = Pattern.compile("\\$\\{#Project#(schema\\d+)\\}").matcher(script);
		assertTrue(matcher.find(), "Script should reference a schema Project Property: " + script);
		return matcher.group(1);
	}

	private Object fakeContext(String expandedValue) {
		return new Object() {
			@SuppressWarnings("unused")
			public String expand(String token) {
				return expandedValue;
			}
		};
	}

	private boolean evaluatesSuccessfully(String script, String storedSchema, String responseBody) {
		Map<String, Object> response = new HashMap<>();
		response.put("responseContent", responseBody);
		GroovyShell shell = new GroovyShell();
		shell.setVariable("messageExchange", response);
		shell.setVariable("context", fakeContext(storedSchema));
		try {
			shell.evaluate(script);
			return true;
		} catch (AssertionError e) {
			return false;
		}
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

	@Test
	void schemaPrettyPrintFalse_withSchemaIsInline_embedsCompactJsonLiterallyAndValidates() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_ITEMS_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, true, false, false, null);
		String script = extractAllScripts(soapUIProject.getFileContent()).get(0);

		assertTrue(script.contains("parseText('''"), "Schema should be embedded literally: " + script);
		int schemaStart = script.indexOf("parseText('''") + "parseText('''".length();
		int schemaEnd = script.indexOf("''')", schemaStart);
		String embeddedSchema = script.substring(schemaStart, schemaEnd);
		assertFalse(embeddedSchema.contains("\n"), "Embedded schema should be compact (single line): " + embeddedSchema);

		assertTrue(evaluatesSuccessfully(script, null, "{\"id\":1}"), "Valid body should pass");
		assertFalse(evaluatesSuccessfully(script, null, "{}"), "Missing required id should fail");
	}

	@Test
	void schemaPrettyPrintTrue_withSchemaIsInline_embedsMultilineJsonLiterallyAndValidates() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_ITEMS_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, true, false, true, null);
		String script = extractAllScripts(soapUIProject.getFileContent()).get(0);

		assertTrue(script.contains("parseText('''"), "Schema should be embedded literally: " + script);
		int schemaStart = script.indexOf("parseText('''") + "parseText('''".length();
		int schemaEnd = script.indexOf("''')", schemaStart);
		String embeddedSchema = script.substring(schemaStart, schemaEnd);
		assertTrue(embeddedSchema.contains("\n"), "Embedded schema should be pretty-printed (multi-line) even inside the Groovy triple-quoted string: " + embeddedSchema);

		assertTrue(evaluatesSuccessfully(script, null, "{\"id\":1}"), "Valid body should pass");
		assertFalse(evaluatesSuccessfully(script, null, "{}"), "Missing required id should fail");
	}

	@Test
	void schemaWithBackslashInEnum_validatesCorrectlyEmbeddedLiterally_whenCompact() throws Exception {
		OpenAPI openAPI = parseSpec(ENUM_WITH_BACKSLASH_SPEC);
		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, true, false, false, null);
		String script = extractAllScripts(soapUIProject.getFileContent()).get(0);

		assertTrue(evaluatesSuccessfully(script, null, "{\"id\":1,\"path\":\"C:\\\\Users\\\\a\"}"), "Enum value with backslashes should validate: " + script);
		assertFalse(evaluatesSuccessfully(script, null, "{\"id\":1,\"path\":\"C:\\\\Users\\\\c\"}"), "Value outside the enum should fail: " + script);
	}

	@Test
	void schemaWithBackslashInEnum_validatesCorrectlyViaProjectProperty_whenCompact() throws Exception {
		OpenAPI openAPI = parseSpec(ENUM_WITH_BACKSLASH_SPEC);
		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, false, null);
		String script = extractAllScripts(soapUIProject.getFileContent()).get(0);
		String stored = soapUIProject.getProject().getPropertyValue(extractPropertyKey(script));

		assertFalse(stored.contains("\n"), "Compact schema should be a single line: " + stored);
		assertTrue(stored.contains("C:\\\\Users\\\\a"), "Stored property should contain the JSON-escaped backslash path: " + stored);
		assertTrue(evaluatesSuccessfully(script, stored, "{\"id\":1,\"path\":\"C:\\\\Users\\\\a\"}"));
		assertFalse(evaluatesSuccessfully(script, stored, "{\"id\":1,\"path\":\"C:\\\\Users\\\\c\"}"));
	}

	@Test
	void multipleOperations_eachSchemaRespectsCompactFlagIndependently() throws Exception {
		OpenAPI openAPI = parseSpec(TWO_OPERATIONS_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, false, null);
		List<String> scripts = extractAllScripts(soapUIProject.getFileContent());
		assertEquals(2, scripts.size());

		String schema1 = soapUIProject.getProject().getPropertyValue("schema1");
		String schema2 = soapUIProject.getProject().getPropertyValue("schema2");
		assertFalse(schema1.contains("\n"), "First operation's schema should be compact: " + schema1);
		assertFalse(schema2.contains("\n"), "Second operation's schema should be compact too, not just the first one: " + schema2);

		String itemsBody = "{\"id\":1}";
		String ordersBody = "{\"total\":5}";
		int passesForItems = 0;
		int passesForOrders = 0;
		for (String script : scripts) {
			String stored = soapUIProject.getProject().getPropertyValue(extractPropertyKey(script));
			boolean itemsOk = evaluatesSuccessfully(script, stored, itemsBody);
			boolean ordersOk = evaluatesSuccessfully(script, stored, ordersBody);
			assertTrue(itemsOk ^ ordersOk, "Each assertion must validate exactly one of the two operation shapes: " + script);
			if (itemsOk) passesForItems++; else passesForOrders++;
		}
		assertEquals(1, passesForItems);
		assertEquals(1, passesForOrders);
	}

	@Test
	void schemaPrettyPrintFalse_hasNoEffectWhenValidateSchemaIsFalse() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_ITEMS_SPEC);

		for (boolean schemaIsInline : new boolean[]{false, true}) {
			SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
					false, null, true, false, false, false, schemaIsInline, false, false, null);
			String xml = soapUIProject.getFileContent();
			assertFalse(xml.contains("Script Assertion"), "No assertion should be added when validateSchema is false (schemaIsInline=" + schemaIsInline + ")");
		}
	}

	@Test
	void complexNestedSchemaWithArrayAndOneOf_compactStillValidatesCorrectly() throws Exception {
		OpenAPI openAPI = parseSpec(COMPLEX_NESTED_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, false, null);
		String script = extractAllScripts(soapUIProject.getFileContent()).get(0);
		String stored = soapUIProject.getProject().getPropertyValue(extractPropertyKey(script));

		assertFalse(stored.contains("\n"), "Complex nested schema should still be compact: " + stored);
		assertTrue(evaluatesSuccessfully(script, stored, "{\"id\":1,\"items\":[{\"sku\":\"A1\",\"payment\":{\"cardNumber\":\"4111\"}}]}"), "Body matching the first oneOf branch should pass");
		assertTrue(evaluatesSuccessfully(script, stored, "{\"id\":1,\"items\":[{\"sku\":\"A1\",\"payment\":{\"accountId\":\"AC1\"}}]}"), "Body matching the second oneOf branch should pass");
		assertFalse(evaluatesSuccessfully(script, stored, "{\"id\":1,\"items\":[{\"sku\":\"A1\",\"payment\":{\"other\":\"x\"}}]}"), "Body matching neither oneOf branch should fail");
	}

	@Test
	void complexNestedSchemaWithArrayAndOneOf_prettyStillValidatesCorrectly() throws Exception {
		OpenAPI openAPI = parseSpec(COMPLEX_NESTED_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, true, null);
		String script = extractAllScripts(soapUIProject.getFileContent()).get(0);
		String stored = soapUIProject.getProject().getPropertyValue(extractPropertyKey(script));

		assertTrue(stored.contains("\n"), "Complex nested schema should be pretty-printed: " + stored);
		assertTrue(evaluatesSuccessfully(script, stored, "{\"id\":1,\"items\":[{\"sku\":\"A1\",\"payment\":{\"cardNumber\":\"4111\"}}]}"), "Body matching the first oneOf branch should pass");
		assertFalse(evaluatesSuccessfully(script, stored, "{\"id\":1,\"items\":[{\"sku\":\"A1\",\"payment\":{\"other\":\"x\"}}]}"), "Body matching neither oneOf branch should fail");
	}
}
