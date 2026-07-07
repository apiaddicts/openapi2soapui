package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apiaddicts.apitools.openapi2soapui.request.AccessTokenPosition;
import org.apiaddicts.apitools.openapi2soapui.request.GrantType;
import org.apiaddicts.apitools.openapi2soapui.request.Header;
import org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile;

class HasScopesEdgeCasesTest {

	private static final String SINGLE_QUERY_PARAM_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /users:",
			"    get:",
			"      operationId: getUsers",
			"      parameters:",
			"        - name: limit",
			"          in: query",
			"          required: false",
			"          schema:",
			"            type: integer",
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

	private static final String TWO_RESOURCES_SPEC = String.join("\n",
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
			"  /orders:",
			"    get:",
			"      operationId: getOrders",
			"      responses:",
			"        '200':",
			"          description: OK"
	);

	private static final String GET_AND_POST_SPEC = String.join("\n",
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
			"    post:",
			"      operationId: createUser",
			"      responses:",
			"        '201':",
			"          description: Created"
	);

	private static final String POST_ONLY_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /users:",
			"    post:",
			"      operationId: createUser",
			"      responses:",
			"        '201':",
			"          description: Created"
	);

	private static final String EMPTY_PATHS_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths: {}"
	);

	private static final String SIMPLE_SPEC = String.join("\n",
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
			"          description: OK"
	);

	private OpenAPI parseSpec(String yaml) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(yaml, null, null);
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());
		return result.getOpenAPI();
	}

	private OAuth2Profile grantTypeProfile(String profileName, String scope) {
		OAuth2Profile profile = new OAuth2Profile();
		profile.setProfileName(profileName);
		profile.setGrantType(GrantType.CLIENT_CREDENTIALS);
		profile.setClientId("clientId");
		profile.setClientSecret("clientSecret");
		profile.setAccessTokenURI("http://api.example.com/token");
		profile.setAccessTokenPosition(AccessTokenPosition.HEADER);
		profile.setScope(scope);
		return profile;
	}

	private OAuth2Profile accessTokenOnlyProfile(String profileName) {
		OAuth2Profile profile = new OAuth2Profile();
		profile.setProfileName(profileName);
		profile.setAccessToken("someToken");
		return profile;
	}

	private OAuth2Profile oAuth2Profile(String profileName, GrantType grantType, String scope) {
		OAuth2Profile profile = new OAuth2Profile();
		profile.setProfileName(profileName);
		profile.setGrantType(grantType);
		profile.setClientId("clientId");
		profile.setClientSecret("clientSecret");
		profile.setAccessTokenURI("http://api.example.com/token");
		profile.setAuthorizationURI("http://api.example.com/authorize");
		profile.setRedirectURI("http://api.example.com/callback");
		profile.setUsername("user");
		profile.setPassword("pass");
		profile.setAccessTokenPosition(AccessTokenPosition.HEADER);
		profile.setScope(scope);
		return profile;
	}

	private Header header(String key, String value) {
		Header header = new Header();
		header.setKey(key);
		header.setValue(value);
		return header;
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

	@Test
	void profileWithNullScope_stillGeneratesVariantWithoutError() throws Exception {
		OpenAPI openAPI = parseSpec(TWO_RESOURCES_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(accessTokenOnlyProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("scope dev_TestCase"), xml);
	}

	@Test
	void coexistsWithQueryParamVariants_scopeVariantKeepsDefaultQueryParamValue() throws Exception {
		OpenAPI openAPI = parseSpec(SINGLE_QUERY_PARAM_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, false, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		// Success (default) + queryString limit (valid) + queryString limit wrong (invalid) + scope dev = 4
		assertEquals(4, countOccurrences(xml, "<con:testCase"));
		assertTrue(xml.contains("scope dev_TestCase"), xml);
		assertTrue(xml.contains("queryString limit_TestCase"), xml);
		assertTrue(xml.contains("queryString limit wrong_TestCase"), xml);
	}

	@Test
	void coexistsWithValidateSchema_scopeVariantsDoNotGetSchemaAssertion() throws Exception {
		OpenAPI openAPI = parseSpec(SINGLE_QUERY_PARAM_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, true, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("scope dev_TestCase"), xml);
		assertEquals(1, countOccurrences(xml, "Script Assertion"), "Only the testCaseNames-based case should get the schema assertion");
	}

	@Test
	void multipleResources_eachGetsItsOwnScopeVariants() throws Exception {
		OpenAPI openAPI = parseSpec(TWO_RESOURCES_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "scope dev_TestCase"), "Both /users and /orders test suites should get their own scope variant");
	}

	@Test
	void readOnly_excludesScopeVariantsForNonReadMethods() throws Exception {
		OpenAPI openAPI = parseSpec(GET_AND_POST_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				true, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("_POST_TestSuite"), "No test suite should be generated for POST when readOnly is true: " + xml);
		assertEquals(1, countOccurrences(xml, "scope dev_TestCase"), "Only the GET suite should get a scope variant");
	}

	@Test
	void readOnly_specWithOnlyNonGetOperations_generatesNoSuitesOrVariants() throws Exception {
		OpenAPI openAPI = parseSpec(POST_ONLY_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				true, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("<con:testSuite"), "No test suite at all should be generated when readOnly excludes the spec's only operation: " + xml);
		assertFalse(xml.contains("scope "), xml);
	}

	@Test
	void emptyPathsSpec_hasScopesTrue_generatesNoSuitesWithoutError() throws Exception {
		OpenAPI openAPI = parseSpec(EMPTY_PATHS_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("<con:testSuite"), "A spec with no paths should not crash and should generate no test suites: " + xml);
	}

	@Test
	void emptyProfilesList_hasScopesTrue_isANoOpLikeNullProfiles() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		List<OAuth2Profile> profiles = Collections.emptyList();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("scope "), "An empty (but non-null) oAuth2Profiles list must also be a no-op: " + xml);
	}

	@Test
	void duplicateProfileNames_generateOneVariantPerEntryWithoutCrashing() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("dev", "write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "scope dev_TestCase"));
	}

	@Test
	void largeNumberOfProfiles_generatesOneVariantEachWithoutError() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		List<OAuth2Profile> profiles = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			profiles.add(grantTypeProfile("profile" + i, "openid"));
		}

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(1 + 25, countOccurrences(xml, "<con:testCase"), "Default test case plus one variant per profile");
		for (int i = 0; i < 25; i++) {
			assertTrue(xml.contains("scope profile" + i + "_TestCase"), "Missing variant for profile" + i + ": " + xml);
		}
	}

	@Test
	void kitchenSink_allOtherFlagsEnabledSimultaneously_doesNotCrash() throws Exception {
		OpenAPI openAPI = parseSpec(SINGLE_QUERY_PARAM_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));
		Set<String> testCaseNames = new LinkedHashSet<>(Arrays.asList("Success", "Alt"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, testCaseNames,
				false, null, false, true, true, true, true, true, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(6, countOccurrences(xml, "<con:testCase"));
		assertTrue(xml.contains("scope dev_TestCase"), xml);
		assertTrue(xml.contains("scope admin_TestCase"), xml);
	}

	@Test
	void multipleMethodsOnSamePath_eachGetsIndependentScopeVariants() throws Exception {
		OpenAPI openAPI = parseSpec(GET_AND_POST_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "scope dev_TestCase"), "Both the GET and POST test suites for /users should each get their own scope variant");
	}

	@Test
	void differentGrantTypes_allGenerateScopeVariantsWithoutError() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				oAuth2Profile("authCode", GrantType.AUTHORIZATION_CODE, "openid"),
				oAuth2Profile("implicit", GrantType.IMPLICIT, "openid"),
				oAuth2Profile("password", GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS, "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("scope authCode_TestCase"), xml);
		assertTrue(xml.contains("scope implicit_TestCase"), xml);
		assertTrue(xml.contains("scope password_TestCase"), xml);
	}

	@Test
	void scopeVariantPreservesCustomHeadersFromDefaultRequest() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));
		List<Header> headers = Arrays.asList(header("X-Custom", "abc123"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, headers, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(4, countOccurrences(xml, "abc123"), "Custom header value should appear on both the default request and the scope-variant clone: " + xml);
	}

	@Test
	void profileNameWithXmlSpecialCharacters_producesWellFormedXml() throws Exception {
		OpenAPI openAPI = parseSpec(SIMPLE_SPEC);
		String weirdName = "Admin & <QA> \"team\"";
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile(weirdName, "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

		String expectedTestCaseName = "scope " + weirdName + "_TestCase";
		NodeList testCases = document.getElementsByTagNameNS("*", "testCase");
		boolean found = false;
		for (int i = 0; i < testCases.getLength(); i++) {
			Element element = (Element) testCases.item(i);
			if (expectedTestCaseName.equals(element.getAttribute("name"))) {
				found = true;
				break;
			}
		}
		assertTrue(found, "Should find a well-formed testCase element correctly round-tripping the special-character profile name '" + expectedTestCaseName + "': " + xml);
	}
}
