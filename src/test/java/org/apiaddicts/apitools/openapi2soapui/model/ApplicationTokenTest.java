package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apiaddicts.apitools.openapi2soapui.request.AccessTokenPosition;
import org.apiaddicts.apitools.openapi2soapui.request.GrantType;
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

	private OpenAPI parseSpec() {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(SPEC, null, null);
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
				false, null, true, false, false, false, false, false, false, true, true, null);
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

	private int countOccurrences(String haystack, String needle) {
		int count = 0;
		int index = 0;
		while ((index = haystack.indexOf(needle, index)) != -1) {
			count++;
			index += needle.length();
		}
		return count;
	}
}
