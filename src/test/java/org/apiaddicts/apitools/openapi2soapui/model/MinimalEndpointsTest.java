package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;

class MinimalEndpointsTest {

	private static final String FLAT_REQUIRED_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    post:",
			"      operationId: createItem",
			"      parameters:",
			"        - name: category",
			"          in: query",
			"          required: true",
			"          schema:",
			"            type: string",
			"        - name: sort",
			"          in: query",
			"          required: false",
			"          schema:",
			"            type: string",
			"      requestBody:",
			"        content:",
			"          application/json:",
			"            schema:",
			"              type: object",
			"              required: [name, age]",
			"              properties:",
			"                name:",
			"                  type: string",
			"                age:",
			"                  type: integer",
			"                nickname:",
			"                  type: string",
			"      responses:",
			"        '200':",
			"          description: OK",
			"        '400':",
			"          description: Bad Request"
	);

	private static final String NESTED_REQUIRED_SPEC = String.join("\n",
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
			"              required: [address]",
			"              properties:",
			"                address:",
			"                  type: object",
			"                  required: [street]",
			"                  properties:",
			"                    street:",
			"                      type: string",
			"      responses:",
			"        '200':",
			"          description: OK"
	);

	private static final String NO_REQUIRED_FIELDS_SPEC = String.join("\n",
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
			"          description: OK"
	);

	private OpenAPI parseSpec(String yaml) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(yaml, null, null);
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());
		return result.getOpenAPI();
	}

	private int countOccurrences(String haystack, String needle) {
		int count = 0;
		int index = 0;
		while ((index = haystack.indexOf(needle, index)) != -1) {
			count++;
			index += needle.length();
		}
		return count;
	}

	private String decode(String xml) {
		String d = xml.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
		// Collapse the pretty-printed JSON body's newlines+indentation only; leaves genuine inline
		// spaces between XML attributes untouched
		return d.replaceAll("\\r?\\n\\s*", "");
	}

	private SoapUIProject buildProject(OpenAPI openAPI, boolean minimalEndpoints) throws Exception {
		return new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, minimalEndpoints, false, false, false, false, false, null);
	}

	@Test
	void minimalEndpointsFalse_generatesOneErrorRequiredPerRequiredBodyPropertyAndQueryParam() throws Exception {
		OpenAPI openAPI = parseSpec(FLAT_REQUIRED_SPEC);
		String xml = buildProject(openAPI, false).getFileContent();

		assertTrue(xml.contains("POST_CaseErrorRequiredName"), xml);
		assertTrue(xml.contains("POST_CaseErrorRequiredAge"), xml);
		assertTrue(xml.contains("POST_CaseErrorRequiredCategory"), xml);
		assertFalse(xml.contains("CaseErrorRequiredNickname"), "nickname is not required, no variant should be generated: " + xml);
		assertFalse(xml.contains("CaseErrorRequiredSort"), "sort is not required, no variant should be generated: " + xml);
		assertEquals(3, countOccurrences(xml, "CaseErrorRequired"), xml);
	}

	@Test
	void minimalEndpointsTrue_collapsesToAtMostOneErrorRequiredField_prioritizingBodyOverQuery() throws Exception {
		OpenAPI openAPI = parseSpec(FLAT_REQUIRED_SPEC);
		String xml = buildProject(openAPI, true).getFileContent();

		assertEquals(1, countOccurrences(xml, "CaseErrorRequired"), "Exactly one CaseErrorRequired{Field} must be generated: " + xml);
		boolean errorRequiredName = xml.contains("CaseErrorRequiredName");
		boolean errorRequiredAge = xml.contains("CaseErrorRequiredAge");
		assertTrue(errorRequiredName ^ errorRequiredAge, "The single variant must target a required BODY property, not the query parameter: " + xml);
		assertFalse(xml.contains("CaseErrorRequiredCategory"), "Body required properties take priority over query parameters: " + xml);
	}

	@Test
	void noRequiredFieldsAtAll_generatesNoErrorRequiredCasesRegardlessOfMinimalEndpoints() throws Exception {
		OpenAPI openAPI = parseSpec(NO_REQUIRED_FIELDS_SPEC);

		assertFalse(buildProject(openAPI, false).getFileContent().contains("CaseErrorRequired"));
		assertFalse(buildProject(openAPI, true).getFileContent().contains("CaseErrorRequired"));
	}

	@Test
	void nestedRequiredObject_generatesErrorRequiredForBothLevels() throws Exception {
		OpenAPI openAPI = parseSpec(NESTED_REQUIRED_SPEC);
		String xml = buildProject(openAPI, false).getFileContent();

		assertTrue(xml.contains("POST_CaseErrorRequiredAddress"), xml);
		assertTrue(xml.contains("POST_CaseErrorRequiredStreet") || xml.contains("POST_CaseErrorRequiredAddressStreet"),
				"A required property nested inside a required object must also get its own variant: " + xml);
	}

	@Test
	void errorRequiredVariant_omitsOnlyTheTargetedPropertyFromTheBody() throws Exception {
		OpenAPI openAPI = parseSpec(FLAT_REQUIRED_SPEC);
		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, true, true, null);
		String decoded = decode(soapUIProject.getFileContent());

		// CaseOkAllProperties' body has all three properties
		assertTrue(decoded.contains("\"name\": \"\""), decoded);
		assertTrue(decoded.contains("\"age\": 0"), decoded);

		int errorRequiredAgeStart = decoded.indexOf("name=\"ErrorRequiredage\"");
		assertTrue(errorRequiredAgeStart >= 0, decoded);
		// The inner <con:request>{json}</con:request> tag holds the body; stop at its closing tag so the
		// block doesn't spill into the next sibling <con:request name="...">...</con:request> element
		int bodyCloseTag = decoded.indexOf("</con:request>", errorRequiredAgeStart);
		assertTrue(bodyCloseTag > errorRequiredAgeStart, decoded);
		String errorRequiredAgeBlock = decoded.substring(errorRequiredAgeStart, bodyCloseTag);
		assertFalse(errorRequiredAgeBlock.contains("\"age\""), "The ErrorRequired variant targeting age must omit the age property entirely: " + errorRequiredAgeBlock);
		assertTrue(errorRequiredAgeBlock.contains("\"name\""), "Every other property should keep its normal value: " + errorRequiredAgeBlock);
	}
}
