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
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"), authorizationCodeProfile("user"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, 2, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("GET_CaseOkApplicationTokenDev"), "Should contain an application token variant test case: " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeUser"), "Should still contain the hasScopes variant test case for the second (non-default) profile: " + xml);
		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already cover the first profile (dev); it must not be duplicated: " + xml);
		assertEquals(4, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases + 1 application token variant + 1 extra scope variant");

		int applicationTokenIndex = xml.indexOf("name=\"application_token dev\"");
		int scopeIndex = xml.indexOf("name=\"scope user\"");
		assertTrue(applicationTokenIndex >= 0 && scopeIndex >= 0, xml);
		assertTrue(applicationTokenIndex < scopeIndex, "application token variant should be generated before the scope variant: " + xml);
	}

	@Test
	void nonClientCredentialsProfile_hasScopesAndApplicationToken_generatesOnlyScopeVariant() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(authorizationCodeProfile("dev"), authorizationCodeProfile("user"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, 2, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), "AUTHORIZATION_CODE profiles must not generate an application token variant: " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeUser"), xml);
		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already cover the first profile (dev): " + xml);
		assertEquals(3, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases + 1 extra scope variant only");
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
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Only the 2 fixed Ok Test Cases should exist");
	}

	@Test
	void hasScopesTrue_applicationTokenFalse_generatesOnlyScopeVariant() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"), clientCredentialsProfile("admin"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), xml);
		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already cover the first profile (dev): " + xml);
		assertEquals(3, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases + 1 extra scope variant only");
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

		assertTrue(xml.contains("GET_CaseOkApplicationTokenApp"), xml);
		assertFalse(xml.contains("GET_CaseOkApplicationTokenUser"), xml);
		assertFalse(xml.contains("GET_CaseOkScopeApp"), "The 2 fixed Ok Test Cases already cover the first profile (app): " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeUser"), xml);
		assertEquals(4, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases + 1 application token variant + 1 extra scope variant");
	}

	@Test
	void noProfiles_hasScopesAndApplicationTokenTrue_generatesNoExtraTestCases() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), xml);
		assertFalse(xml.contains("scope "), xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Only the 2 fixed Ok Test Cases should exist");
	}

	@Test
	void legacyOverload_defaultsApplicationTokenToFalse() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), "Legacy overload must default applicationToken to false: " + xml);
		assertFalse(xml.contains("GET_CaseOkScopeDev"), "With a single profile the 2 fixed Ok Test Cases already cover it; no extra scope variant is generated: " + xml);
	}

	@Test
	void profileWithoutGrantType_hasScopesAndApplicationTokenTrue_noApplicationTokenVariantButScopeVariantRemains() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(accessTokenOnlyProfile("dev"), accessTokenOnlyProfile("admin"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, 2, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("application_token "), "A profile with no grantType must not generate an application token variant: " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
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

		assertEquals(2, countOccurrences(xml, "GET_CaseOkApplicationTokenDev"), "applicationToken generates one variant per CLIENT_CREDENTIALS profile, including the first");
		assertEquals(1, countOccurrences(xml, "GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already cover the first profile; only the second gets an extra scope variant");
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

		assertEquals(2 + 25 + 24, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases + 25 application token variants (one per profile, including the first) + 24 extra scope variants (all but the first, already covered by the fixed Ok Test Cases)");
		for (int i = 0; i < 25; i++) {
			assertTrue(xml.contains("GET_CaseOkApplicationTokenProfile" + i), "Missing application token variant for profile" + i + ": " + xml);
		}
	}

	@Test
	void multipleResources_eachGetsItsOwnApplicationTokenVariant() throws Exception {
		OpenAPI openAPI = parseSpec(TWO_RESOURCES_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "GET_CaseOkApplicationTokenDev"), "Both /users and /orders test suites should get their own application token variant");
	}

	@Test
	void applicationTokenVariantPreservesCustomHeadersFromDefaultRequest() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"), authorizationCodeProfile("user"));
		List<Header> headers = Arrays.asList(header("X-Custom", "abc123"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, headers, null,
				false, null, true, false, false, false, false, false, false, true, true, 2, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(countOccurrences(xml, "abc123") >= 5, "Custom header value should appear on the default request, both fixed Ok Test Cases, the application token clone and the scope clone: " + xml);
	}

	@Test
	void kitchenSink_allOtherFlagsEnabledSimultaneously_doesNotCrash() throws Exception {
		OpenAPI openAPI = parseSpec(SINGLE_QUERY_PARAM_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(clientCredentialsProfile("dev"), authorizationCodeProfile("user"));
		Set<String> testCaseNames = new LinkedHashSet<>(Arrays.asList("Success", "Alt"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, testCaseNames,
				false, null, false, true, true, true, true, true, true, true, true, 2, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("GET_CaseOkApplicationTokenDev"), xml);
		assertTrue(xml.contains("GET_CaseOkScopeUser"), xml);
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

		// weirdName has no underscores, so toCaseFieldName only forces its (already uppercase) first letter,
		// leaving the rest - including the XML special characters - unchanged
		String expectedTestCaseName = "GET_CaseOkApplicationToken" + weirdName;
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
