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
import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;
import org.apiaddicts.apitools.openapi2soapui.request.AccessTokenPosition;
import org.apiaddicts.apitools.openapi2soapui.request.GrantType;
import org.apiaddicts.apitools.openapi2soapui.request.Header;
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

	private OpenAPI parseSpec() {
		return parseSpec(SPEC);
	}

	private OpenAPI parseSpec(String yaml) {
		return SerializedDataUtils.parseOpenAPIContent(yaml);
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
	void singleProfile_hasScopesTrue_generatesNoExtraTestCase() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid, secret"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already use the only configured profile, so no extra scope-variant Test Case should be generated: " + xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Only the 2 fixed Ok Test Cases (CaseOkAllProperties + CaseOkRequiredProperties) should exist");
	}

	@Test
	void multipleProfiles_hasScopesDefault_generatesNoExtraVariant() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already use the first profile (dev): " + xml);
		assertFalse(xml.contains("GET_CaseOkScopeAdmin"), "Without hasScopes, no extra scope variant is generated by default: " + xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Only the 2 fixed Ok Test Cases should exist (hasScopes defaults to false, no extra variant)");
	}

	@Test
	void multipleProfiles_numberOfScopesTwo_generatesVariantForProfilesBeyondTheFirst() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already use the first profile (dev), so it must not be duplicated: " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
		assertEquals(3, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases (dev) + 1 extra scope variant (admin) for numberOfScopes=2");
	}

	@Test
	void noProfiles_hasScopesTrue_generatesNoExtraTestCases() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("scope "), "No scope-variant test case should be generated without profiles: " + xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Only the 2 fixed Ok Test Cases should exist");
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
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		int defaultRequestStart = xml.indexOf("name=\"Request 1\"");
		int scopeAdminRequestStart = xml.indexOf("name=\"scope admin\"");
		assertTrue(defaultRequestStart >= 0 && scopeAdminRequestStart >= 0, xml);

		String defaultRequestBlock = xml.substring(defaultRequestStart, scopeAdminRequestStart);
		assertTrue(defaultRequestBlock.contains("dev"), "The fixed Ok Test Cases should still reference the first profile (dev): " + defaultRequestBlock);
		assertFalse(defaultRequestBlock.contains("admin"), "The fixed Ok Test Cases must not reference the second profile: " + defaultRequestBlock);
	}

	@Test
	void numberOfScopesZero_isTreatedAsFloorOfOne() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 0, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("GET_CaseOkScopeDev"), xml);
		assertFalse(xml.contains("GET_CaseOkScopeAdmin"), xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "numberOfScopes=0 must behave like unset: floor of 1 (no extra variant)");
	}

	@Test
	void numberOfScopesLessThanProfileCount_capsToFirstN() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"),
				grantTypeProfile("qa", "openid, read"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already use the first profile (dev): " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
		assertFalse(xml.contains("GET_CaseOkScopeQa"), "numberOfScopes=2 must only use the first 2 configured profiles: " + xml);
		assertEquals(3, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases + 1 extra capped scope variant (admin)");
	}

	@Test
	void numberOfScopesGreaterThanOrEqualToProfileCount_isANoOp() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 5, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already use the first profile (dev): " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
		assertEquals(3, countOccurrences(xml, "<con:testCase"), "numberOfScopes >= profile count must not cap beyond the configured profiles");
	}

	@Test
	void numberOfScopesNegative_isTreatedAsFloorOfOne() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, -1, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Negative numberOfScopes must floor to 1 (no extra variant)");
	}

	@Test
	void numberOfScopesSet_hasScopesFalse_silentlyIgnored() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, false, false, 1, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("scope "), "hasScopes=false must ignore numberOfScopes entirely");
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Only the 2 fixed Ok Test Cases should exist");
	}

	@Test
	void legacyOverload_defaultsNumberOfScopesToFloorOfOne() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Legacy overload without numberOfScopes must default to floor of 1 (no extra variant)");
	}

	@Test
	void profileWithNullScope_stillGeneratesVariantWithoutError() throws Exception {
		OpenAPI openAPI = parseSpec(TWO_RESOURCES_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("primary", "openid"), accessTokenOnlyProfile("nullscope"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("GET_CaseOkScopeNullscope"), xml);
	}

	@Test
	void coexistsWithBodyPropertyVariants_scopeVariantKeepsDefaultBodyValue() throws Exception {
		String spec = String.join("\n",
				"openapi: 3.0.0",
				"info:",
				"  title: Test",
				"  version: '1.0'",
				"paths:",
				"  /users:",
				"    post:",
				"      operationId: createUser",
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
				"        '201':",
				"          description: Created"
		);
		OpenAPI openAPI = parseSpec(spec);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, false, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(4, countOccurrences(xml, "<con:testCase"));
		assertFalse(xml.contains("POST_CaseOkScopeDev"), xml);
		assertTrue(xml.contains("POST_CaseOkScopeAdmin"), xml);
		assertTrue(xml.contains("POST_CaseErrorRequiredId"), xml);
	}

	@Test
	void coexistsWithValidateSchema_scopeVariantsDoNotGetSchemaAssertion() throws Exception {
		OpenAPI openAPI = parseSpec(SINGLE_QUERY_PARAM_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, true, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
		// Both fixed Ok Test Cases always get the success schema assertion (unconditionally, regardless of
		// validateSchema, which no longer gates anything); the scope-variant Test Case does not get one
		assertEquals(2, countOccurrences(xml, "Script Assertion"), "Only the 2 fixed Ok Test Cases should get the schema assertion, not the scope variant");
	}

	@Test
	void multipleResources_eachGetsItsOwnScopeVariants() throws Exception {
		OpenAPI openAPI = parseSpec(TWO_RESOURCES_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "GET_CaseOkScopeAdmin"), "Both /users and /orders test suites should get their own scope variant");
	}

	@Test
	void readOnly_excludesScopeVariantsForNonReadMethods() throws Exception {
		OpenAPI openAPI = parseSpec(GET_AND_POST_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				true, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("-POST-Suite"), "No test suite should be generated for POST when readOnly is true: " + xml);
		assertEquals(1, countOccurrences(xml, "GET_CaseOkScopeAdmin"), "Only the GET suite should get a scope variant");
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
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Collections.emptyList();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("scope "), "An empty (but non-null) oAuth2Profiles list must also be a no-op: " + xml);
	}

	@Test
	void duplicateProfileNames_generateOneVariantPerEntryBeyondFirstWithoutCrashing() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("dev", "write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(1, countOccurrences(xml, "GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already cover the first (dev) profile; only the second gets an extra Test Case");
	}

	@Test
	void numberOfScopesCapsAmongDuplicateProfileNames() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("dev", "write"),
				grantTypeProfile("dev", "read"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(1, countOccurrences(xml, "GET_CaseOkScopeDev"), "Cap must apply positionally (and skip the first/default profile) even when profile names repeat, without crashing");
	}

	@Test
	void numberOfScopesEqualsProfileCount_isANoOp() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already use the first profile (dev): " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
		assertEquals(3, countOccurrences(xml, "<con:testCase"), "numberOfScopes exactly equal to the profile count must use all of them (2 fixed + 1 extra)");
	}

	@Test
	void numberOfScopesIntegerMaxValue_isTreatedAsNoCapWithoutOverflow() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, Integer.MAX_VALUE, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(3, countOccurrences(xml, "<con:testCase"), "Integer.MAX_VALUE must behave like any other value >= profile count: use all (2 fixed + 1 extra), no overflow");
	}

	@Test
	void numberOfScopesSet_nullProfiles_isStillANoOpWithoutCrashing() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 3, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("scope "), "numberOfScopes must not cause a crash or spurious variants when oAuth2Profiles is null: " + xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"));
	}

	@Test
	void numberOfScopesSet_emptyProfilesList_isStillANoOpWithoutCrashing() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Collections.emptyList();

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 3, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("scope "), "numberOfScopes must not cause a crash or spurious variants when oAuth2Profiles is empty: " + xml);
	}

	@Test
	void largeNumberOfProfiles_numberOfScopesMatchingCount_generatesOneVariantEachWithoutError() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			profiles.add(grantTypeProfile("profile" + i, "openid"));
		}

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 25, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2 + 24, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases (profile0) plus one extra variant per remaining requested profile");
		assertFalse(xml.contains("GET_CaseOkScopeProfile0"), "The 2 fixed Ok Test Cases already cover profile0; it must not be duplicated: " + xml);
		for (int i = 1; i < 25; i++) {
			assertTrue(xml.contains("GET_CaseOkScopeProfile" + i), "Missing variant for profile" + i + ": " + xml);
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
				false, null, false, true, true, true, true, true, true, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(5, countOccurrences(xml, "<con:testCase"));
		assertFalse(xml.contains("GET_CaseOkScopeDev"), xml);
		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
	}

	@Test
	void multipleMethodsOnSamePath_eachGetsIndependentScopeVariants() throws Exception {
		OpenAPI openAPI = parseSpec(GET_AND_POST_SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("GET_CaseOkScopeAdmin"), xml);
		assertTrue(xml.contains("POST_CaseOkScopeAdmin"), xml);
		assertEquals(2, countOccurrences(xml, "CaseOkScopeAdmin"), "Both the GET and POST test suites for /users should each get their own scope variant");
	}

	@Test
	void differentGrantTypes_allGenerateScopeVariantsWithoutError() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				oAuth2Profile("authCode", GrantType.AUTHORIZATION_CODE, "openid"),
				oAuth2Profile("implicit", GrantType.IMPLICIT, "openid"),
				oAuth2Profile("password", GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS, "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 3, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("GET_CaseOkScopeAuthCode"), "The 2 fixed Ok Test Cases already cover the first profile (authCode): " + xml);
		assertTrue(xml.contains("GET_CaseOkScopeImplicit"), xml);
		assertTrue(xml.contains("GET_CaseOkScopePassword"), xml);
	}

	@Test
	void scopeVariantPreservesCustomHeadersFromDefaultRequest() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"));
		List<Header> headers = Arrays.asList(header("X-Custom", "abc123"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, headers, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(countOccurrences(xml, "abc123") >= 4, "Custom header value should appear on the default request, both fixed Ok Test Cases and the scope-variant clone: " + xml);
	}

	@Test
	void numberOfScopesCapsAmongLargeProfileList() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			profiles.add(grantTypeProfile("profile" + i, "openid"));
		}

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 10, null);
		String xml = soapUIProject.getFileContent();

		assertEquals(2 + 9, countOccurrences(xml, "<con:testCase"), "2 fixed Ok Test Cases (profile0) plus 9 capped extra scope variants");
		assertFalse(xml.contains("GET_CaseOkScopeProfile0"), "The 2 fixed Ok Test Cases already cover profile0; it must not be duplicated: " + xml);
		for (int i = 1; i < 10; i++) {
			assertTrue(xml.contains("GET_CaseOkScopeProfile" + i), "Missing variant for profile" + i + ": " + xml);
		}
		for (int i = 10; i < 25; i++) {
			assertFalse(xml.contains("GET_CaseOkScopeProfile" + i), "Should not generate variant for profile" + i + " beyond the cap: " + xml);
		}
	}

	@Test
	void numberOfScopesCap_coexistsWithApplicationToken_onlyAffectsScopeVariants() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		List<OAuth2Profile> profiles = Arrays.asList(
				grantTypeProfile("dev", "openid"),
				grantTypeProfile("admin", "openid, write"),
				grantTypeProfile("qa", "openid, read"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, true, 1, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("GET_CaseOkApplicationTokenDev"), "numberOfScopes must not cap applicationToken variants: " + xml);
		assertTrue(xml.contains("GET_CaseOkApplicationTokenAdmin"), xml);
		assertTrue(xml.contains("GET_CaseOkApplicationTokenQa"), xml);
		assertFalse(xml.contains("GET_CaseOkScopeDev"), "The 2 fixed Ok Test Cases already cover the first profile (dev); numberOfScopes=1 caps scope variants to zero extra: " + xml);
		assertFalse(xml.contains("GET_CaseOkScopeAdmin"), xml);
		assertFalse(xml.contains("GET_CaseOkScopeQa"), xml);
	}

	@Test
	void profileNameWithXmlSpecialCharacters_producesWellFormedXml() throws Exception {
		OpenAPI openAPI = parseSpec(SPEC);
		String weirdName = "Admin & <QA> \"team\"";
		List<OAuth2Profile> profiles = Arrays.asList(grantTypeProfile("dev", "openid"), grantTypeProfile(weirdName, "openid"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, profiles, null, null,
				false, null, true, false, false, false, false, false, false, true, false, 2, null);
		String xml = soapUIProject.getFileContent();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

		String expectedTestCaseName = "GET_CaseOkScope" + weirdName;
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
