package org.apiaddicts.apitools.openapi2soapui.model;

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
	void hasScopesAndApplicationToken_stillGenerateVariants_butKeepTheOldCaseNamingInsideTheNewSuite() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"), clientCredentialsProfile("admin"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, false, false, false, false, false, false, true, true, true, 2, null, null, true);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("/items_TestApi_1.0-GET-Suite"), "New Suite naming must still apply: " + xml);
		assertTrue(xml.contains("GET_CaseOkAllProperties"), xml);
		assertTrue(xml.contains("GET_CaseOkRequiredProperties"), xml);
		assertTrue(xml.contains("scope admin_TestCase"), "hasScopes variants are unaffected and keep the OLD _TestCase suffix, mixed into the same, newly-named Suite: " + xml);
		assertTrue(xml.contains("application_token dev_TestCase") || xml.contains("application_token admin_TestCase"),
				"applicationToken variants also keep the OLD naming: " + xml);
	}

	@Test
	void customAuthorizationsFile_authorizationsSuiteIsUnaffectedAndKeepsOldNaming() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		CustomAuthorizationRequest customRequest = new CustomAuthorizationRequest();
		customRequest.setName("login");
		customRequest.setMethod("POST");
		customRequest.setEndpoint("http://auth.example.com/login");
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(customRequest);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, false, false, false, null, null, customAuthorizationsFile, true);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("authorizations_TestSuite"), "The authorizations Test Suite is built before setTestCases() runs and is untouched by serviceApiConvention: " + xml);
		assertTrue(xml.contains("login_TestCase"), xml);
		assertTrue(xml.contains("/items_TestApi_1.0-GET-Suite"), "The per-endpoint suite still gets the new naming: " + xml);
	}

	@Test
	void microcksHeadersTrue_appliesTheMatchingStatusExampleToEachCaseType() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_MICROCKS_EXAMPLES);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, true, false, false, false, false, false, false, false, null, null, null, true);
		String xml = decode(soapUIProject.getFileContent());

		String okAllProperties = requestBlock(xml, "OkAllProperties");
		assertTrue(okAllProperties.contains("successExample"), okAllProperties);
		assertFalse(okAllProperties.contains("badExample"), okAllProperties);

		String errorStatusCode400 = requestBlock(xml, "ErrorStatusCode400");
		assertTrue(errorStatusCode400.contains("badExample"), "The 400 case must use the 400 response's own named example, not the success one: " + errorStatusCode400);
		assertFalse(errorStatusCode400.contains("successExample"), errorStatusCode400);
	}

	@Test
	void validateSchemaFalse_schemaAssertionIsStillAddedUnderTheNewConvention() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC_WITH_SCHEMA);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, false, false, false, null, null, null, true);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Script Assertion"), "serviceApiConvention's schema assertion must be added even when validateSchema is false/unset: " + xml);
		assertTrue(xml.contains("<codes>200</codes>"), xml);
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
				false, null, false, false, false, false, false, true, false, false, false, null, null, null, true);
		String xmlInline = inlineTrue.getFileContent();
		assertTrue(xmlInline.contains("\"name\": \"\""), "isInline=true must embed the literal body value (empty string is the default example for an unformatted string property): " + xmlInline);
		assertFalse(xmlInline.contains("${#Project#"), xmlInline);
		assertTrue(xmlInline.contains("key=\"category\" value=\"string\""), "Query param values are always written literally into the request config, regardless of isInline: " + xmlInline);

		SoapUIProject inlineFalse = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, false, false, false, null, null, null, true);
		String xmlNotInline = inlineFalse.getFileContent();
		assertTrue(xmlNotInline.contains("${#Project#"), "isInline=false must tokenize the body value as a Project Property: " + xmlNotInline);
		assertTrue(xmlNotInline.contains("key=\"category\" value=\"string\""), "Query param values remain literal even when isInline=false, same as before: " + xmlNotInline);
	}

	@Test
	void testCaseNamesAndMinimalEndpoints_areSilentlyIgnoredWithoutErrorUnderTheNewConvention() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		Set<String> testCaseNames = Set.of("Custom");

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, testCaseNames,
				false, null, false, false, false, false, false, false, false, false, false, null, null, null, true);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("Custom_TestCase"), "testCaseNames must have no effect under serviceApiConvention: " + xml);
		assertFalse(xml.contains("missing "), "minimalEndpoints' variant generation must not run under serviceApiConvention: " + xml);
		assertFalse(xml.contains("wrong "), xml);
		assertTrue(xml.contains("GET_CaseOkAllProperties"), xml);
	}
}
