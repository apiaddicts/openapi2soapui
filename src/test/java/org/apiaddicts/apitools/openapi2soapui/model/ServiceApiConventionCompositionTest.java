package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apiaddicts.apitools.openapi2soapui.request.AccessTokenPosition;
import org.apiaddicts.apitools.openapi2soapui.request.CustomAuthorizationRequest;
import org.apiaddicts.apitools.openapi2soapui.request.GrantType;
import org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile;
import org.junit.jupiter.api.Test;

class ServiceApiConventionCompositionTest {

	private static final String SIMPLE_SPEC = String.join("\n",
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

	private static final String SPEC_WITH_MICROCKS_EXAMPLES = String.join("\n",
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

	private static final String SPEC_WITH_SCHEMA = String.join("\n",
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
			"              required: [name]",
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
			"                properties:",
			"                  id:",
			"                    type: string"
	);

	private static final String SPEC_WITH_TWO_REQUIRED_BODY_FIELDS = String.join("\n",
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
			"              required: [name, age]",
			"              properties:",
			"                name:",
			"                  type: string",
			"                age:",
			"                  type: integer",
			"      responses:",
			"        '200':",
			"          description: OK",
			"        '400':",
			"          description: Bad Request"
	);

	private static final String SPEC_WITH_NESTED_ALLOF = String.join("\n",
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
			"                metadata:",
			"                  allOf:",
			"                    - $ref: '#/components/schemas/Base'",
			"                    - type: object",
			"                      properties:",
			"                        version:",
			"                          type: integer",
			"      responses:",
			"        '200':",
			"          description: OK",
			"components:",
			"  schemas:",
			"    Base:",
			"      allOf:",
			"        - type: object",
			"          properties:",
			"            source:",
			"              type: string",
			"        - type: object",
			"          properties:",
			"            region:",
			"              type: string"
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

	private int countOccurrences(String haystack, String needle) {
		int count = 0;
		int index = 0;
		while ((index = haystack.indexOf(needle, index)) != -1) {
			count++;
			index += needle.length();
		}
		return count;
	}

	private String requestBlock(String decoded, String requestName) {
		int start = decoded.indexOf("name=\"" + requestName + "\"");
		assertTrue(start >= 0, "Request not found: " + requestName + "\n" + decoded);
		int end = decoded.indexOf("<con:request name=\"", start + 1);
		if (end < 0) end = decoded.indexOf("<con:testSuite", start);
		assertTrue(end > start, decoded);
		return decoded.substring(start, end);
	}

	private OAuth2Profile clientCredentialsProfile(String name) {
		OAuth2Profile profile = new OAuth2Profile();
		profile.setProfileName(name);
		profile.setGrantType(GrantType.CLIENT_CREDENTIALS);
		profile.setClientId("clientId");
		profile.setClientSecret("clientSecret");
		profile.setAccessTokenURI("http://api.example.com/token");
		profile.setAccessTokenPosition(AccessTokenPosition.HEADER);
		profile.setScope("openid");
		return profile;
	}

	@Test
	void hasScopesAndApplicationToken_generateVariantsWithTheNewCaseNaming() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"), clientCredentialsProfile("admin"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, false, false, false, false, false, false, true, true, true, 2, null, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("/items_TestApi_1.0-GET-Suite"), xml);
		assertTrue(xml.contains("GET_CaseOkAllProperties"), xml);
		assertTrue(xml.contains("GET_CaseOkRequiredProperties"), xml);
		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), "hasScopes variant must use the new naming: " + xml);
		assertTrue(xml.contains("GET_CaseOkApplicationTokenDev") || xml.contains("GET_CaseOkApplicationTokenAdmin"),
				"applicationToken variant must use the new naming: " + xml);
		assertFalse(xml.contains("_TestCase"), xml);
	}

	@Test
	void customAuthorizationsFile_authorizationsSuiteUsesTheNewNaming() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		CustomAuthorizationRequest customRequest = new CustomAuthorizationRequest();
		customRequest.setName("login");
		customRequest.setMethod("POST");
		customRequest.setEndpoint("http://auth.example.com/login");
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(customRequest);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, false, false, false, null, null, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("authorizations_TestApi_1.0-Suite"), xml);
		assertTrue(xml.contains("POST_CaseLogin"), xml);
		assertTrue(xml.contains("/items_TestApi_1.0-GET-Suite"), "The per-endpoint suite still gets its own naming: " + xml);
		assertFalse(xml.contains("_TestSuite") || xml.contains("_TestCase"), xml);
	}

	@Test
	void microcksHeadersTrue_appliesTheMatchingStatusExampleToEachCaseType() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_MICROCKS_EXAMPLES);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, true, false, false, false, false, false, false, false, null, null, null);
		String xml = decode(soapUIProject.getFileContent());

		String okAllProperties = requestBlock(xml, "OkAllProperties");
		assertTrue(okAllProperties.contains("successExample"), okAllProperties);
		assertFalse(okAllProperties.contains("badExample"), okAllProperties);

		String errorStatusCode400 = requestBlock(xml, "ErrorStatusCode400");
		assertTrue(errorStatusCode400.contains("badExample"), "The 400 case must use the 400 response's own named example, not the success one: " + errorStatusCode400);
		assertFalse(errorStatusCode400.contains("successExample"), errorStatusCode400);
	}

	@Test
	void validateSchemaOmitted_defaultsToTrueAndAddsSchemaAssertion() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_SCHEMA);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, null, false, false, false, false, false, null, null, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Script Assertion"), "The schema assertion must be added when validateSchema is omitted (defaults to true): " + xml);
		assertTrue(xml.contains("<codes>200</codes>"), xml);
	}

	@Test
	void validateSchemaFalse_omitsSchemaAssertionButKeepsStatusCodeAssertion() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_SCHEMA);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, false, false, false, null, null, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("Script Assertion"), "validateSchema=false must omit the schema assertion: " + xml);
		assertTrue(xml.contains("<codes>200</codes>"), "The status-code assertion must remain regardless of validateSchema: " + xml);
	}

	@Test
	void isInlineTrue_bodyValuesAreLiteral_queryParamsAreAlwaysLiteralRegardlessOfIsInline() throws Exception {
		String specWithQueryParam = String.join("\n",
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
				"      requestBody:",
				"        content:",
				"          application/json:",
				"            schema:",
				"              type: object",
				"              required: [name]",
				"              properties:",
				"                name:",
				"                  type: string",
				"      responses:",
				"        '200':",
				"          description: OK"
		);
		OpenAPI openAPI = parseSpec(specWithQueryParam);

		SoapUIProject inlineTrue = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, true, false, false, false, null, null, null);
		String xmlInline = inlineTrue.getFileContent();
		assertTrue(xmlInline.contains("\"name\": \"\""), "isInline=true must embed the literal body value (empty string is the default example for an unformatted string property): " + xmlInline);
		assertFalse(xmlInline.contains("${#Project#"), xmlInline);
		assertTrue(xmlInline.contains("key=\"category\" value=\"string\""), "Query param values are always written literally into the request config, regardless of isInline: " + xmlInline);

		SoapUIProject inlineFalse = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, false, false, false, null, null, null);
		String xmlNotInline = inlineFalse.getFileContent();
		assertTrue(xmlNotInline.contains("${#Project#"), "isInline=false must tokenize the body value as a Project Property: " + xmlNotInline);
		assertTrue(xmlNotInline.contains("key=\"category\" value=\"string\""), "Query param values remain literal even when isInline=false, same as before: " + xmlNotInline);
	}

	@Test
	void testCaseNames_generatesExtraNamedCopyOfOkAllProperties() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		Set<String> testCaseNames = Set.of("Custom");

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, testCaseNames,
				false, null, false, false, false, false, false, false, false, false, false, null, null, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("GET_CaseCustom"), "testCaseNames must generate an extra named copy of CaseOkAllProperties: " + xml);
		assertTrue(xml.contains("GET_CaseOkAllProperties"), xml);
		assertTrue(xml.contains("GET_CaseOkRequiredProperties"), xml);
	}

	@Test
	void allOf_flattensMembersNestedInsideAnotherAllOf() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_NESTED_ALLOF);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, true, false, false, false, null, null, null);
		String xml = decode(soapUIProject.getFileContent());

		assertTrue(xml.contains("\"version\""), "Direct allOf member field must be present: " + xml);
		assertTrue(xml.contains("\"source\""), "Field from an allOf nested inside an allOf member must be merged: " + xml);
		assertTrue(xml.contains("\"region\""), "All fields from an allOf nested inside an allOf member must be merged: " + xml);
	}

	@Test
	void minimalEndpoints_capsErrorRequiredFieldToOne() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_TWO_REQUIRED_BODY_FIELDS);

		SoapUIProject minimalTrue = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, false, false, false, null, null, null);
		String xmlMinimal = minimalTrue.getFileContent();
		assertEquals(1, countOccurrences(xmlMinimal, "POST_CaseErrorRequired"), "minimalEndpoints=true must collapse to at most one CaseErrorRequired{Field}: " + xmlMinimal);

		SoapUIProject minimalFalse = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, false, false, false, null, null, null);
		String xmlNotMinimal = minimalFalse.getFileContent();
		assertEquals(2, countOccurrences(xmlNotMinimal, "POST_CaseErrorRequired"), "minimalEndpoints=false (default) must generate one CaseErrorRequired{Field} per required property: " + xmlNotMinimal);
	}
}
