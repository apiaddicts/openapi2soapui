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

class HasScopesTest {

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

	@Test
	void singleProfile_generatesOneExtraTestCaseWiredToThatProfile() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid, secret"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("scope dev_TestCase"), "Should contain a scope-variant test case named after the profile: " + xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Should have the default test case plus one scope variant");
	}

	@Test
	void multipleProfiles_generateOneVariantEach() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("scope dev_TestCase"), xml);
		assertTrue(xml.contains("scope admin_TestCase"), xml);
		assertEquals(3, countOccurrences(xml, "<con:testCase"), "Default + 2 scope variants");
	}

	@Test
	void noProfiles_hasScopesTrue_generatesNoExtraTestCases() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("scope "), "No scope-variant test case should be generated without profiles: " + xml);
		assertEquals(1, countOccurrences(xml, "<con:testCase"), "Only the default test case should exist");
	}

	@Test
	void hasScopesFalseOrUnset_generatesNoScopeVariants() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject explicitFalse = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, false, null);
		assertFalse(explicitFalse.getFileContent().contains("scope "), "hasScopes=false must not generate variants");

		SoapUIProject legacyOverload = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, null);
		assertFalse(legacyOverload.getFileContent().contains("scope "), "Legacy overload must default hasScopes to false");
	}

	@Test
	void defaultTestCaseAuthProfileUnaffectedByScopeVariants() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		int defaultRequestStart = xml.indexOf("name=\"Request 1\"");
		int scopeDevRequestStart = xml.indexOf("name=\"scope dev\"");
		assertTrue(defaultRequestStart >= 0 && scopeDevRequestStart >= 0, xml);

		String defaultRequestBlock = xml.substring(defaultRequestStart, scopeDevRequestStart);
		assertTrue(defaultRequestBlock.contains("dev"), "Default request should still reference the first profile (dev): " + defaultRequestBlock);
		assertFalse(defaultRequestBlock.contains("admin"), "Default request must not reference the second profile: " + defaultRequestBlock);
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
