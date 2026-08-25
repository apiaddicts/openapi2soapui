package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;
import org.junit.jupiter.api.Test;

class ServiceApiConventionTest {

	private static final String SPEC_WITH_REQUIRED_AND_OPTIONAL = String.join("\n",
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
			"              required: [name, address]",
			"              properties:",
			"                name:",
			"                  type: string",
			"                nickname:",
			"                  type: string",
			"                address:",
			"                  type: object",
			"                  required: [street]",
			"                  properties:",
			"                    street:",
			"                      type: string",
			"                    apartment:",
			"                      type: string",
			"      responses:",
			"        '201':",
			"          description: Created",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"                properties:",
			"                  id:",
			"                    type: string",
			"        '400':",
			"          description: Bad Request",
			"          content:",
			"            application/json:",
			"              schema:",
			"                type: object",
			"                properties:",
			"                  error:",
			"                    type: string",
			"        '404':",
			"          description: Not Found",
			"        '500':",
			"          description: Server Error",
			"        default:",
			"          description: Unexpected error"
	);

	private static final String SPEC_NO_ERRORS_NO_REQUIRED = String.join("\n",
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
		return SerializedDataUtils.parseOpenAPIContent(yaml);
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
		return xml.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
	}

	private SoapUIProject buildProject(OpenAPI openAPI) throws Exception {
		return new SoapUIProject(
				"TestApi",  // apiName
				openAPI,    // openAPI
				null,       // oAuth2Profiles
				null,       // headers
				null,       // testCaseNames
				false,      // readOnly
				null,       // serverPattern
				false,      // minimalEndpoints
				false,      // microcksHeaders
				false,      // generateOneOfAnyOf
				false,      // validateSchema
				false,      // schemaIsInline
				false,      // isInline
				true,       // schemaPrettyPrint
				false,      // hasScopes
				false,      // applicationToken
				null,       // numberOfScopes
				null,       // examples
				null        // customAuthorizationsFile
		);
	}

	@Test
	void generatesRsiSuiteAndCaseNames() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_REQUIRED_AND_OPTIONAL);
		String xml = buildProject(openAPI).getFileContent();

		assertTrue(xml.contains("/items_TestApi_1.0-POST-Suite"), xml);
		assertTrue(xml.contains("POST_CaseOkAllProperties"), xml);
		assertTrue(xml.contains("POST_CaseOkRequiredProperties"), xml);
		assertTrue(xml.contains("POST_CaseErrorStatusCode400"), xml);
		assertTrue(xml.contains("POST_CaseErrorStatusCode404"), xml);
		assertTrue(xml.contains("POST_CaseErrorStatusCode500"), xml);
		assertFalse(xml.contains("_TestSuite"), xml);
		assertFalse(xml.contains("_TestCase"), xml);
	}

	@Test
	void okAllProperties_populatesAllQueryParamsAndFullBody() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_REQUIRED_AND_OPTIONAL);
		String decoded = decode(buildProject(openAPI).getFileContent());

		int start = decoded.indexOf("name=\"OkAllProperties\"");
		assertTrue(start >= 0, decoded);
		int end = decoded.indexOf("name=\"OkRequiredProperties\"", start);
		String block = decoded.substring(start, end);

		assertTrue(block.contains("\"name\""), block);
		assertTrue(block.contains("\"nickname\""), "Optional body property must be present: " + block);
		assertTrue(block.contains("\"street\""), block);
		assertTrue(block.contains("\"apartment\""), "Optional nested property must be present: " + block);
		assertTrue(block.contains("key=\"category\" value=\"string\""), block);
		assertTrue(block.contains("key=\"sort\" value=\"string\""), "Optional query param must also get a value: " + block);
	}

	@Test
	void okRequiredProperties_omitsOptionalBodyAndQueryParams() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_REQUIRED_AND_OPTIONAL);
		String decoded = decode(buildProject(openAPI).getFileContent());

		int start = decoded.indexOf("name=\"OkRequiredProperties\"");
		assertTrue(start >= 0, decoded);
		int end = decoded.indexOf("name=\"ErrorStatusCode400\"", start);
		String block = decoded.substring(start, end);

		assertTrue(block.contains("\"name\""), block);
		assertTrue(block.contains("\"street\""), "Required nested property must be present: " + block);
		assertFalse(block.contains("\"nickname\""), "Optional body property must be omitted: " + block);
		assertFalse(block.contains("\"apartment\""), "Optional nested property must be omitted: " + block);
		assertTrue(block.contains("key=\"category\" value=\"string\""), "Required query param must have a value: " + block);
		assertFalse(block.contains("key=\"sort\""), "Optional query param must be omitted: " + block);
	}

	@Test
	void errorStatusCode_generatesOnePerDocumentedNonSuccessCodeWithOwnAssertions() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_REQUIRED_AND_OPTIONAL);
		String xml = buildProject(openAPI).getFileContent();

		assertEquals(1, countOccurrences(xml, "POST_CaseErrorStatusCode400"), xml);
		assertEquals(1, countOccurrences(xml, "POST_CaseErrorStatusCode404"), xml);
		assertEquals(1, countOccurrences(xml, "POST_CaseErrorStatusCode500"), xml);
		assertFalse(xml.contains("CaseErrorStatusCodedefault"), xml);
		assertTrue(xml.contains("<codes>400</codes>"), xml);
		assertTrue(xml.contains("<codes>404</codes>"), xml);
		assertTrue(xml.contains("<codes>500</codes>"), xml);
	}

	@Test
	void errorRequiredField_coversBodyAndQueryRequiredFieldsOnly() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_REQUIRED_AND_OPTIONAL);
		String xml = buildProject(openAPI).getFileContent();

		assertTrue(xml.contains("POST_CaseErrorRequiredName"), xml);
		assertTrue(xml.contains("POST_CaseErrorRequiredAddress"), xml);
		assertTrue(xml.contains("POST_CaseErrorRequiredStreet") || xml.contains("POST_CaseErrorRequiredAddressStreet"), xml);
		assertTrue(xml.contains("POST_CaseErrorRequiredCategory"), xml);
		assertFalse(xml.contains("CaseErrorRequiredNickname"), xml);
		assertFalse(xml.contains("CaseErrorRequiredSort"), xml);
		assertFalse(xml.contains("CaseErrorRequiredApartment"), xml);
	}

	@Test
	void noOperationRequirements_stillGeneratesTheTwoOkCasesOnly() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_NO_ERRORS_NO_REQUIRED);
		String xml = buildProject(openAPI).getFileContent();

		assertTrue(xml.contains("GET_CaseOkAllProperties"), xml);
		assertTrue(xml.contains("GET_CaseOkRequiredProperties"), xml);
		assertFalse(xml.contains("CaseErrorStatusCode"), xml);
		assertFalse(xml.contains("CaseErrorRequired"), xml);
	}
}
