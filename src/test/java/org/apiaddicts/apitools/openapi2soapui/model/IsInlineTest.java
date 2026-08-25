package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;
import org.apiaddicts.apitools.openapi2soapui.request.ExampleValues;
import org.apiaddicts.apitools.openapi2soapui.request.ExamplesConfig;

class IsInlineTest {

	private static final String SPEC = String.join("\n",
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
			"          description: OK"
	);

	private OpenAPI parseSpec() {
		return SerializedDataUtils.parseOpenAPIContent(SPEC);
	}

	private ExamplesConfig examplesConfig() {
		ExampleValues successful = new ExampleValues();
		successful.setString("Ada");
		successful.setNumber(new BigDecimal(42));
		ExamplesConfig examples = new ExamplesConfig();
		examples.setSuccessful(successful);
		return examples;
	}

	private String decodeAndCompact(String xml) {
		String decoded = xml.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
		return decoded.replaceAll("\\s+", "");
	}

	@Test
	void isInlineTrue_embedsLiteralValues() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, true, true, examplesConfig());
		String compact = decodeAndCompact(soapUIProject.getFileContent());

		assertTrue(compact.contains("\"name\":\"Ada\""), "Body should contain the literal string value: " + compact);
		assertTrue(compact.contains("\"age\":42"), "Body should contain the literal number value: " + compact);
		assertFalse(compact.contains("${#Project#"), "Body should not contain Project Property tokens: " + compact);
	}

	@Test
	void isInlineFalse_usesProjectPropertyTokens() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, true, false, examplesConfig());
		String compact = decodeAndCompact(soapUIProject.getFileContent());

		assertTrue(compact.contains("\"name\":\"${#Project#body1_name}\""), "String field should be a quoted Project Property token: " + compact);
		assertTrue(compact.contains("\"age\":${#Project#body1_age}"), "Number field should be an unquoted Project Property token: " + compact);
		assertEquals("Ada", soapUIProject.getProject().getPropertyValue("body1_name"));
		assertEquals("42", soapUIProject.getProject().getPropertyValue("body1_age"));
	}

	private static final String NESTED_MULTI_OP_SPEC = String.join("\n",
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
			"                address:",
			"                  type: object",
			"                  properties:",
			"                    street:",
			"                      type: string",
			"                tags:",
			"                  type: array",
			"                  items:",
			"                    type: object",
			"                    properties:",
			"                      label:",
			"                        type: string",
			"      responses:",
			"        '200':",
			"          description: OK",
			"  /orders:",
			"    post:",
			"      operationId: createOrder",
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
			"          description: OK"
	);

	@Test
	void isInlineFalse_handlesNestedObjectsArraysAndMultipleOperationsWithoutKeyCollisions() throws Exception {
		OpenAPI openAPI = SerializedDataUtils.parseOpenAPIContent(NESTED_MULTI_OP_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, true, false, null);
		String compact = decodeAndCompact(soapUIProject.getFileContent());

		// /items body (generated first -> counter 1): top-level, nested object and array-of-object leaves
		assertTrue(compact.contains("\"name\":\"${#Project#body1_name}\""), "Top-level string field: " + compact);
		assertTrue(compact.contains("\"street\":\"${#Project#body1_address_street}\""), "Nested object field: " + compact);
		assertTrue(compact.contains("\"label\":\"${#Project#body1_tags_item_label}\""), "Array-of-object item field: " + compact);

		// /orders body (generated second -> counter 2): same field name "name" as /items must not collide
		assertTrue(compact.contains("\"name\":\"${#Project#body2_name}\""), "Second operation's field must use a distinct counter prefix: " + compact);

		assertEquals("", soapUIProject.getProject().getPropertyValue("body1_name"));
		assertEquals("", soapUIProject.getProject().getPropertyValue("body1_address_street"));
		assertEquals("", soapUIProject.getProject().getPropertyValue("body1_tags_item_label"));
		assertEquals("", soapUIProject.getProject().getPropertyValue("body2_name"));
	}

	private static final String DATE_TIME_BODY_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /events:",
			"    post:",
			"      operationId: createEvent",
			"      requestBody:",
			"        content:",
			"          application/json:",
			"            schema:",
			"              type: object",
			"              properties:",
			"                createdAt:",
			"                  type: string",
			"                  format: date-time",
			"      responses:",
			"        '200':",
			"          description: OK"
	);

	@Test
	void isInlineTrue_appliesConfiguredDateTimeExample_forDateTimeField() throws Exception {
		OpenAPI openAPI = SerializedDataUtils.parseOpenAPIContent(DATE_TIME_BODY_SPEC);

		ExampleValues successful = new ExampleValues();
		successful.setDateTime("2030-01-01T10:00:00");
		ExamplesConfig examples = new ExamplesConfig();
		examples.setSuccessful(successful);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, true, true, examples);
		String compact = decodeAndCompact(soapUIProject.getFileContent());

		assertTrue(compact.contains("\"createdAt\":\"2030-01-01T10:00:00\""),
				"date-time body field should use the configured examples.successful.dateTime value: " + compact);
	}
}
