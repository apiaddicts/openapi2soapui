package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;
import org.apiaddicts.apitools.openapi2soapui.request.Header;
import org.junit.jupiter.api.Test;

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
		return SerializedDataUtils.parseOpenAPIContent(yaml);
	}

	private String decode(String xml) {
		return xml.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
	}

	private String requestBlock(String decoded, String requestName) {
		int start = decoded.indexOf("name=\"" + requestName + "\"");
		assertTrue(start >= 0, "Request not found: " + requestName + "\n" + decoded);
		int end = decoded.indexOf("<con:request name=\"", start + 1);
		if (end < 0) end = decoded.indexOf("<con:testSuite", start);
		assertTrue(end > start, decoded);
		return decoded.substring(start, end);
	}

	@Test
	void errorRequiredField_usesThe400ResponseExampleInsteadOfTheOperationsFirst2xx() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_400_EXAMPLE);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, true, false, false, false, false, null);
		String xml = decode(soapUIProject.getFileContent());

		String defaultRequestBlock = requestBlock(xml, "Request 1");
		assertTrue(defaultRequestBlock.contains("X-Microcks-Response-Name") && defaultRequestBlock.contains("successExample"),
				"The default (success-path) request should use the first 2xx response's named example: " + defaultRequestBlock);
		assertFalse(defaultRequestBlock.contains("badExample"), defaultRequestBlock);

		String errorRequiredIdBlock = requestBlock(xml, "ErrorRequiredid");
		assertTrue(errorRequiredIdBlock.contains("badExample"),
				"The 400-targeting CaseErrorRequired{Field} variant should use the 400 response's own named example, not the operation's first 2xx: " + errorRequiredIdBlock);
		assertFalse(errorRequiredIdBlock.contains("successExample"), errorRequiredIdBlock);
	}

	@Test
	void customResponseNameHeader_isPreservedInErrorTestCasesNotOnlyOkOnes() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_400_EXAMPLE);

		Header customHeader = new Header();
		customHeader.setKey("X-Microcks-Response-Name");
		customHeader.setValue("custom");

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, List.of(customHeader), null,
				false, null, false, true, false, false, false, false, null);
		String xml = decode(soapUIProject.getFileContent());

		String defaultRequestBlock = requestBlock(xml, "Request 1");
		assertTrue(defaultRequestBlock.contains("X-Microcks-Response-Name") && defaultRequestBlock.contains("value=\"custom\""),
				"The OK request should keep the user-supplied custom X-Microcks-Response-Name value: " + defaultRequestBlock);
		assertFalse(defaultRequestBlock.contains("successExample"), defaultRequestBlock);

		String errorRequiredIdBlock = requestBlock(xml, "ErrorRequiredid");
		assertTrue(errorRequiredIdBlock.contains("value=\"custom\""),
				"The 400-targeting CaseErrorRequired{Field} variant should keep the user-supplied custom header value, not overwrite it with the computed status example: " + errorRequiredIdBlock);
		assertFalse(errorRequiredIdBlock.contains("badExample"), errorRequiredIdBlock);
	}

	@Test
	void errorRequiredField_fallsBackToLiteralDefaultWhenNo400ResponseIsDeclared() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITHOUT_400_EXAMPLE);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, true, false, false, false, false, null);
		String xml = decode(soapUIProject.getFileContent());

		String errorRequiredIdBlock = requestBlock(xml, "ErrorRequiredid");
		assertTrue(errorRequiredIdBlock.contains("value=\"default\""),
				"With no 400 (or default) response declared, the header should fall back to the literal string 'default': " + errorRequiredIdBlock);
	}
}
