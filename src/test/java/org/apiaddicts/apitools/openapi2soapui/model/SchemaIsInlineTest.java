package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final String DATE_TIME_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /events:",
			"    get:",
			"      operationId: getEvents",
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
			"                  createdAt:",
			"                    type: string",
			"                    format: date-time"
	);

	private OpenAPI parseSpec() {
		return parseSpec(SPEC);
	}

	private OpenAPI parseSpec(String yaml) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(yaml, null, null);
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

	@Test
	void validateSchemaFalse_omitsSchemaAssertionButKeepsStatusCodeAssertion() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_ITEMS_SPEC);

		SoapUIProject withSchemaValidation = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, null);
		String xmlWithSchema = withSchemaValidation.getFileContent();
		assertTrue(xmlWithSchema.contains("Script Assertion"), "validateSchema=true (or omitted, the default) must add the schema assertion");
		assertNotNull(withSchemaValidation.getProject().getPropertyValue("schema1"), "schemaIsInline=false must still register the schema Project Property");

		SoapUIProject withoutSchemaValidation = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, null);
		String xmlWithoutSchema = withoutSchemaValidation.getFileContent();
		assertFalse(xmlWithoutSchema.contains("Script Assertion"), "validateSchema=false must omit the schema assertion: " + xmlWithoutSchema);
		assertTrue(xmlWithoutSchema.contains("Valid HTTP Status Codes"), "The status-code assertion must remain regardless of validateSchema: " + xmlWithoutSchema);
		assertNull(withoutSchemaValidation.getProject().getPropertyValue("schema1"), "No schema property should be registered when validateSchema is false");
	}

	@Test
	void operationWithNo2xxJsonResponse_skipsAssertionWithoutCrashing() throws Exception {
		OpenAPI openAPI = parseSpec(String.join("\n",
				"openapi: 3.0.0",
				"info:",
				"  title: Test",
				"  version: '1.0'",
				"paths:",
				"  /nothing:",
				"    get:",
				"      operationId: getNothing",
				"      responses:",
				"        '204':",
				"          description: No Content"
		));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("Script Assertion"), "No assertion should be added for a 204 response: " + xml);
		assertNull(soapUIProject.getProject().getPropertyValue("schema1"));
	}

	@Test
	void schemaIsInlineNull_defaultsToExternalReference() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_ITEMS_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, null, false, null);
		String xml = soapUIProject.getFileContent();
		List<String> scripts = extractAllScripts(xml);

		assertEquals(2, scripts.size(), "Both fixed Ok* cases get their own schema assertion");
		scripts.forEach(script -> assertTrue(script.contains("context.expand("), "Omitted schemaIsInline should default to the external/false behavior: " + script));
		assertNotNull(soapUIProject.getProject().getPropertyValue("schema1"));
		assertNotNull(soapUIProject.getProject().getPropertyValue("schema2"));
	}

	@Test
	void testCaseNames_generateAdditionalIndependentlyValidatingProperties() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_ITEMS_SPEC);
		Set<String> testCaseNames = new LinkedHashSet<>(Arrays.asList("Success", "Alt"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, testCaseNames,
				false, null, true, false, false, true, false, false, null);
		String xml = soapUIProject.getFileContent();
		List<String> scripts = extractAllScripts(xml);

		assertEquals(4, scripts.size(), "2 fixed Ok* cases + 2 custom testCaseNames, each with its own schema assertion");

		for (String script : scripts) {
			String stored = soapUIProject.getProject().getPropertyValue(extractPropertyKey(script));
			assertTrue(evaluatesSuccessfully(script, stored, "{\"id\":1}"), "Valid body should pass: " + script);
			assertFalse(evaluatesSuccessfully(script, stored, "{}"), "Missing required id should fail: " + script);
		}
	}

	@Test
	void multipleOperations_eachAssertionValidatesOnlyItsOwnSchema() throws Exception {
		OpenAPI openAPI = parseSpec(String.join("\n",
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
		));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, null);
		String xml = soapUIProject.getFileContent();
		List<String> scripts = extractAllScripts(xml);
		assertEquals(4, scripts.size(), "2 fixed Ok* cases per operation, for 2 operations");

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
		assertEquals(2, passesForItems, "Both fixed Ok* cases for /items should validate the items schema");
		assertEquals(2, passesForOrders, "Both fixed Ok* cases for /orders should validate the orders schema");
	}

	@Test
	void isInlineAndSchemaIsInlineBothFalse_coexistWithoutKeyCollision() throws Exception {
		OpenAPI openAPI = parseSpec(String.join("\n",
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
		));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("${#Project#body1_name}"), "Body token should be present: " + xml);
		List<String> scripts = extractAllScripts(xml);
		assertEquals(2, scripts.size(), "Both fixed Ok* cases get their own schema assertion");
		assertEquals("", soapUIProject.getProject().getPropertyValue("body1_name"));

		for (String script : scripts) {
			String key = extractPropertyKey(script);
			assertTrue(key.equals("schema1") || key.equals("schema2"), "Schema property key must not collide with body property keys: " + key);
			String storedSchema = soapUIProject.getProject().getPropertyValue(key);
			assertTrue(storedSchema.contains("\"id\""));
			assertTrue(evaluatesSuccessfully(script, storedSchema, "{\"id\":7}"));
			assertFalse(evaluatesSuccessfully(script, storedSchema, "{}"));
		}
	}

	@Test
	void schemaWithBackslashInEnum_validatesCorrectlyEmbeddedLiterally() throws Exception {
		OpenAPI openAPI = parseSpec(ENUM_WITH_BACKSLASH_SPEC);
		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, true, false, null);
		String script = extractAllScripts(soapUIProject.getFileContent()).get(0);

		assertTrue(evaluatesSuccessfully(script, null, "{\"id\":1,\"path\":\"C:\\\\Users\\\\a\"}"));
		assertFalse(evaluatesSuccessfully(script, null, "{\"id\":1,\"path\":\"C:\\\\Users\\\\c\"}"));
	}

	@Test
	void dateTimeField_generatesStringTypeSchema_notMalformedObject() throws Exception {
		OpenAPI openAPI = parseSpec(DATE_TIME_SPEC);
		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, true, false, null);
		String script = extractScript(soapUIProject.getFileContent());

		int start = script.indexOf("parseText('''");
		assertTrue(start >= 0, "schemaIsInline=true should embed the schema literally: " + script);
		start += "parseText('''".length();
		int end = script.indexOf("''')", start);
		assertTrue(end > start, "Should find the embedded schema JSON bounds: " + script);
		org.json.JSONObject schema = new org.json.JSONObject(script.substring(start, end));

		org.json.JSONObject createdAt = schema.getJSONObject("properties").getJSONObject("createdAt");

		assertEquals("string", createdAt.getString("type"), "date-time must be typed as string: " + createdAt);
		assertEquals("date-time", createdAt.getString("format"), "date-time format must be preserved: " + createdAt);
		assertFalse(createdAt.has("properties"), "date-time must not be treated as an object with properties: " + createdAt);
	}

	@Test
	void schemaWithBackslashInEnum_validatesCorrectlyViaProjectProperty() throws Exception {
		OpenAPI openAPI = parseSpec(ENUM_WITH_BACKSLASH_SPEC);
		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, true, false, false, null);
		String script = extractAllScripts(soapUIProject.getFileContent()).get(0);
		String stored = soapUIProject.getProject().getPropertyValue(extractPropertyKey(script));

		assertTrue(stored.contains("C:\\\\Users\\\\a"), "Stored property should contain the JSON-escaped backslash path: " + stored);
		assertTrue(evaluatesSuccessfully(script, stored, "{\"id\":1,\"path\":\"C:\\\\Users\\\\a\"}"));
		assertFalse(evaluatesSuccessfully(script, stored, "{\"id\":1,\"path\":\"C:\\\\Users\\\\c\"}"));
	}
}
