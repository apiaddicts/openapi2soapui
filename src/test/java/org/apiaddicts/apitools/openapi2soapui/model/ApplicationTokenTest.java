package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
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

class ApplicationTokenTest {

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
			"          description: OK"
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

	private OpenAPI parseSpec() {
		return parseSpec(SPEC);
	}

	private OpenAPI parseSpec(String yaml) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(yaml, null, null);
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());
		return result.getOpenAPI();
	}

	private OAuth2Profile clientCredentialsProfile(String profileName) {
		OAuth2Profile profile = new OAuth2Profile();
		profile.setProfileName(profileName);
		profile.setGrantType(GrantType.CLIENT_CREDENTIALS);
		profile.setClientId("clientId");
		profile.setClientSecret("clientSecret");
		profile.setAccessTokenURI("http://api.example.com/token");
		profile.setAccessTokenPosition(AccessTokenPosition.HEADER);
		return profile;
	}

	private OAuth2Profile authorizationCodeProfile(String profileName) {
		OAuth2Profile profile = new OAuth2Profile();
		profile.setProfileName(profileName);
		profile.setGrantType(GrantType.AUTHORIZATION_CODE);
		profile.setClientId("clientId");
		profile.setClientSecret("clientSecret");
		profile.setAccessTokenURI("http://api.example.com/token");
		profile.setAuthorizationURI("http://api.example.com/authorize");
		profile.setRedirectURI("http://localhost/callback");
		profile.setAccessTokenPosition(AccessTokenPosition.HEADER);
		return profile;
	}

	private OAuth2Profile accessTokenOnlyProfile(String profileName) {
		OAuth2Profile profile = new OAuth2Profile();
		profile.setProfileName(profileName);
		profile.setAccessToken("someToken");
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
	void clientCredentialsProfile_hasScopesAndApplicationToken_generatesBothVariantsInOrder() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("application_token dev_TestCase"), "Should contain an application_token variant test case: " + xml);
		assertTrue(xml.contains("scope dev_TestCase"), "Should still contain the hasScopes variant test case: " + xml);
		assertEquals(3, countOccurrences(xml, "<con:testCase"), "Default + application_token variant + scope variant");

		int applicationTokenIndex = xml.indexOf("name=\"application_token dev\"");
		int scopeIndex = xml.indexOf("name=\"scope dev\"");
		assertTrue(applicationTokenIndex >= 0 && scopeIndex >= 0, xml);
		assertTrue(applicationTokenIndex < scopeIndex, "application_token variant should be generated before the scope variant: " + xml);
	}

	@Test
	void nonClientCredentialsProfile_hasScopesAndApplicationToken_generatesOnlyScopeVariant() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(authorizationCodeProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token dev_TestCase"), "AUTHORIZATION_CODE profiles must not generate an application_token variant: " + xml);
		assertTrue(xml.contains("scope dev_TestCase"), xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Default + scope variant only");
	}

	@Test
	void hasScopesFalse_applicationTokenTrue_isANoOp() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), "applicationToken must be a no-op when hasScopes is false: " + xml);
		assertFalse(xml.contains("scope "), xml);
		assertEquals(1, countOccurrences(xml, "<con:testCase"), "Only the default test case should exist");
	}

	@Test
	void hasScopesTrue_applicationTokenFalse_generatesOnlyScopeVariant() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), xml);
		assertTrue(xml.contains("scope dev_TestCase"), xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Default + scope variant only");
	}

	@Test
	void mixedGrantTypes_onlyClientCredentialsProfilesGenerateApplicationTokenVariant() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				clientCredentialsProfile("app"),
				authorizationCodeProfile("user"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, 2, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("application_token app_TestCase"), xml);
		assertFalse(xml.contains("application_token user_TestCase"), xml);
		assertTrue(xml.contains("scope app_TestCase"), xml);
		assertTrue(xml.contains("scope user_TestCase"), xml);
		assertEquals(4, countOccurrences(xml, "<con:testCase"), "Default + 1 application_token variant + 2 scope variants");
	}

	@Test
	void noProfiles_hasScopesAndApplicationTokenTrue_generatesNoExtraTestCases() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), xml);
		assertFalse(xml.contains("scope "), xml);
		assertEquals(1, countOccurrences(xml, "<con:testCase"), "Only the default test case should exist");
	}

	@Test
	void legacyOverload_defaultsApplicationTokenToFalse() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), "Legacy overload must default applicationToken to false: " + xml);
		assertTrue(xml.contains("scope dev_TestCase"), xml);
	}

	@Test
	void profileWithoutGrantType_hasScopesAndApplicationTokenTrue_noApplicationTokenVariantButScopeVariantRemains() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(accessTokenOnlyProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), "A profile with no grantType must not generate an application_token variant: " + xml);
		assertTrue(xml.contains("scope dev_TestCase"), xml);
	}

	@Test
	void duplicateClientCredentialsProfileNames_generateOneApplicationTokenVariantPerEntry() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				clientCredentialsProfile("dev"),
				clientCredentialsProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, 2, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "application_token dev_TestCase"));
		assertEquals(2, countOccurrences(xml, "scope dev_TestCase"));
	}

	@Test
	void largeNumberOfClientCredentialsProfiles_generatesOneApplicationTokenVariantEach() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			profiles.add(clientCredentialsProfile("profile" + i));
		}

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, 25, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(1 + 25 + 25, countOccurrences(xml, "<con:testCase"), "Default + 25 application_token variants + 25 scope variants");
		for (int i = 0; i < 25; i++) {
			assertTrue(xml.contains("application_token profile" + i + "_TestCase"), "Missing application_token variant for profile" + i + ": " + xml);
		}
	}

	@Test
	void multipleResources_eachGetsItsOwnApplicationTokenVariant() throws Exception {
		OpenAPI openAPI = parseSpec(TWO_RESOURCES_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "application_token dev_TestCase"), "Both /users and /orders test suites should get their own application_token variant");
	}

	@Test
	void applicationTokenVariantPreservesCustomHeadersFromDefaultRequest() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));
		List<Header> headers = Arrays.asList(header("X-Custom", "abc123"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, headers, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(6, countOccurrences(xml, "abc123"), "Custom header value should appear on the default request, the application_token clone and the scope clone: " + xml);
	}

	@Test
	void kitchenSink_allOtherFlagsEnabledSimultaneously_doesNotCrash() throws Exception {
		OpenAPI openAPI = parseSpec(SINGLE_QUERY_PARAM_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));
		Set<String> testCaseNames = new LinkedHashSet<>(Arrays.asList("Success", "Alt"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, testCaseNames,
				false, null, false, true, true, true, true, true, true, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("application_token dev_TestCase"), xml);
		assertTrue(xml.contains("scope dev_TestCase"), xml);
	}

	@Test
	void profileNameWithXmlSpecialCharacters_producesWellFormedApplicationTokenTestCase() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		String weirdName = "Admin & <QA> \"team\"";
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile(weirdName));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

		String expectedTestCaseName = "application_token " + weirdName + "_TestCase";
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
