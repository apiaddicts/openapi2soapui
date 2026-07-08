package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.apiaddicts.apitools.openapi2soapui.request.CustomAuthorizationRequest;
import org.apiaddicts.apitools.openapi2soapui.request.GrantType;
import org.apiaddicts.apitools.openapi2soapui.request.Header;
import org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile;

class CustomAuthorizationsFileTest {

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

	private CustomAuthorizationRequest customAuthorization(String name, String method, String endpoint) {
		CustomAuthorizationRequest request = new CustomAuthorizationRequest();
		request.setName(name);
		request.setMethod(method);
		request.setEndpoint(endpoint);
		return request;
	}

	private Header header(String key, String value) {
		Header header = new Header();
		header.setKey(key);
		header.setValue(value);
		return header;
	}

	private SoapUIProject buildProject(OpenAPI openAPI, List<CustomAuthorizationRequest> customAuthorizationsFile) throws Exception {
		return new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, false, false, false, null, null, customAuthorizationsFile);
	}

	private List<Element> testSuitesInOrder(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		NodeList nodeList = document.getElementsByTagNameNS("*", "testSuite");
		List<Element> testSuites = new java.util.ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			testSuites.add((Element) nodeList.item(i));
		}
		return testSuites;
	}

	@Test
	void customAuthorizationsFile_generatesDedicatedSuiteBeforeEndpointSuites() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Application token", "POST", "https://api.example.com/security/token"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		List<Element> testSuites = testSuitesInOrder(xml);
		assertEquals(2, testSuites.size(), "Should have the authorizations suite plus the /users suite: " + xml);
		assertEquals("authorizations_TestSuite", testSuites.get(0).getAttribute("name"), "The authorizations suite must be first: " + xml);
		assertTrue(testSuites.get(1).getAttribute("name").startsWith("/users_GET"), "The endpoint suite must come after: " + xml);
	}

	@Test
	void multipleCustomAuthorizations_eachGetsOwnTestCaseInOrder() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Application token", "POST", "https://api.example.com/security/token"),
				customAuthorization("User token", "POST", "https://api.example.com/security/user-token"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Application token_TestCase"), xml);
		assertTrue(xml.contains("User token_TestCase"), xml);
		int applicationTokenIndex = xml.indexOf("Application token_TestCase");
		int userTokenIndex = xml.indexOf("User token_TestCase");
		assertTrue(applicationTokenIndex < userTokenIndex, "Test cases must be generated in the given order: " + xml);
	}

	@Test
	void customAuthorizationRequestFields_mapToGeneratedRequest() throws Exception {
		OpenAPI openAPI = parseSpec();
		CustomAuthorizationRequest request = customAuthorization("Application token", "POST", "https://api.example.com/security/token");
		request.setMediaType("application/x-www-form-urlencoded");
		request.setBody("grant_type=client_credentials&client_id=abc&client_secret=xyz");
		request.setHeaders(Arrays.asList(header("X-Custom", "abc123")));

		SoapUIProject soapUIProject = buildProject(openAPI, Arrays.asList(request));
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("https://api.example.com/security/token"), xml);
		assertTrue(xml.contains("application/x-www-form-urlencoded"), xml);
		assertTrue(xml.contains("grant_type=client_credentials"), xml);
		assertTrue(xml.contains("X-Custom"), xml);
		assertTrue(xml.contains("abc123"), xml);
	}

	@Test
	void nullCustomAuthorizationsFile_noAuthorizationsSuiteGenerated() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = buildProject(openAPI, null);
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("authorizations_TestSuite"), "No authorizations suite should be generated when the field is absent: " + xml);
		assertEquals(1, testSuitesInOrder(xml).size(), "Only the endpoint suite should exist");
	}

	@Test
	void emptyCustomAuthorizationsFile_noAuthorizationsSuiteGenerated() throws Exception {
		OpenAPI openAPI = parseSpec();

		SoapUIProject soapUIProject = buildProject(openAPI, Collections.emptyList());
		String xml = soapUIProject.getFileContent();

		assertFalse(xml.contains("authorizations_TestSuite"), "No authorizations suite should be generated for an empty list: " + xml);
		assertEquals(1, testSuitesInOrder(xml).size(), "Only the endpoint suite should exist");
	}

	@Test
	void orderIsPreservedAsGiven_unlikeOpenapi2postmansUnshiftReversal() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("First", "POST", "https://api.example.com/security/first"),
				customAuthorization("Second", "POST", "https://api.example.com/security/second"),
				customAuthorization("Third", "POST", "https://api.example.com/security/third"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		int first = xml.indexOf("First_TestCase");
		int second = xml.indexOf("Second_TestCase");
		int third = xml.indexOf("Third_TestCase");
		assertTrue(first >= 0 && second >= 0 && third >= 0, xml);
		assertTrue(first < second && second < third, "Entries must appear in the given order (First, Second, Third), not reversed: " + xml);
	}

	@Test
	void duplicateNamesAcrossEntries_generatesBothTestCasesWithoutCrashing() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Login", "POST", "https://api.example.com/security/token"),
				customAuthorization("Login", "POST", "https://api.example.com/security/refresh"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		assertEquals(2, countOccurrences(xml, "Login_TestCase"), "Both entries must generate their own test case even with a duplicate name: " + xml);
	}

	@Test
	void nameCollidingWithRealOpenApiPath_doesNotCorruptEndpointSuite() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("/users", "POST", "https://api.example.com/security/token"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		List<Element> testSuites = testSuitesInOrder(xml);
		assertEquals(2, testSuites.size(), xml);
		assertEquals("authorizations_TestSuite", testSuites.get(0).getAttribute("name"), xml);
		assertTrue(testSuites.get(1).getAttribute("name").startsWith("/users_GET"), "The real /users GET suite must still be generated correctly even though a custom authorization reuses its name: " + xml);
		assertEquals(1, countOccurrences(xml, "Success_TestCase"), "The real endpoint's default test case must still be generated exactly once: " + xml);
	}

	@Test
	void xmlSpecialCharactersInFields_producesWellFormedXml() throws Exception {
		OpenAPI openAPI = parseSpec();
		String weirdName = "Admin & <Login> \"token\"";
		CustomAuthorizationRequest request = customAuthorization(weirdName, "POST", "https://api.example.com/security/token?a=1&b=2");
		request.setBody("grant_type=client_credentials&client_id=<abc>&client_secret=\"xyz\"");
		request.setHeaders(Arrays.asList(header("X-Test", "value & \"quoted\" <tag>")));

		SoapUIProject soapUIProject = buildProject(openAPI, Arrays.asList(request));
		String xml = soapUIProject.getFileContent();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

		NodeList testCases = document.getElementsByTagNameNS("*", "testCase");
		boolean found = false;
		for (int i = 0; i < testCases.getLength(); i++) {
			Element element = (Element) testCases.item(i);
			if ((weirdName + "_TestCase").equals(element.getAttribute("name"))) {
				found = true;
				break;
			}
		}
		assertTrue(found, "Should find a well-formed testCase element correctly round-tripping special characters: " + xml);
	}

	@Test
	void blankMediaTypeAndBody_areTreatedAsAbsent() throws Exception {
		OpenAPI openAPI = parseSpec();
		CustomAuthorizationRequest request = customAuthorization("Login", "POST", "https://api.example.com/security/token");
		request.setMediaType("   ");
		request.setBody("   ");

		SoapUIProject soapUIProject = buildProject(openAPI, Arrays.asList(request));
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("mediaType=\"application/json\""), "Blank mediaType must fall back to SoapUI's default instead of being set literally as blank: " + xml);
	}

	@Test
	void emptyHeadersList_doesNotCrashAndBehavesLikeNoHeaders() throws Exception {
		OpenAPI openAPI = parseSpec();
		CustomAuthorizationRequest request = customAuthorization("Login", "POST", "https://api.example.com/security/token");
		request.setHeaders(Collections.emptyList());

		SoapUIProject soapUIProject = buildProject(openAPI, Arrays.asList(request));
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Login_TestCase"), xml);
	}

	@Test
	void getMethodCustomAuthorization_isSupported() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Login redirect", "GET", "https://api.example.com/security/authorize"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Login redirect_TestCase"), xml);
	}

	@Test
	void lowercaseAndMixedCaseMethod_isNormalizedAndAccepted() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Lowercase", "post", "https://api.example.com/security/token"),
				customAuthorization("MixedCase", "Get", "https://api.example.com/security/verify"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Lowercase_TestCase"), xml);
		assertTrue(xml.contains("MixedCase_TestCase"), xml);
	}

	@Test
	void readOnlyTrue_authorizationsSuiteIsUnaffectedByReadOnly() throws Exception {
		OpenAPI openAPI = parseSpec(GET_AND_POST_SPEC);
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Login", "POST", "https://api.example.com/security/token"));

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				true, null, true, false, false, false, false, false, false, false, false, null, null, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Login_TestCase"), "readOnly must not exclude the custom authorization request even though its method is POST: " + xml);
		assertFalse(xml.contains("/users_POST_TestSuite"), "readOnly must still exclude the real POST endpoint suite: " + xml);
		assertTrue(xml.contains("/users_GET_TestSuite"), xml);
	}

	@Test
	void coexistsWithHasScopesAndOAuth2Profiles_doesNotInterfere() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Login", "POST", "https://api.example.com/security/token"));

		OAuth2Profile profile = new OAuth2Profile();
		profile.setProfileName("dev");
		profile.setGrantType(GrantType.CLIENT_CREDENTIALS);
		profile.setClientId("clientId");
		profile.setClientSecret("clientSecret");
		profile.setAccessTokenURI("http://api.example.com/token");
		profile.setAccessTokenPosition(AccessTokenPosition.HEADER);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, Arrays.asList(profile), null, null,
				false, null, true, false, false, false, false, false, false, true, false, null, null, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("Login_TestCase"), xml);
		assertTrue(xml.contains("scope dev_TestCase"), xml);
		List<Element> testSuites = testSuitesInOrder(xml);
		assertEquals("authorizations_TestSuite", testSuites.get(0).getAttribute("name"), "The authorizations suite must still be first even when hasScopes/oAuth2Profiles are also used: " + xml);
	}

	@Test
	void emptyOpenApiPathsSpec_onlyAuthorizationsSuiteExists() throws Exception {
		OpenAPI openAPI = parseSpec(EMPTY_PATHS_SPEC);
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Login", "POST", "https://api.example.com/security/token"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		List<Element> testSuites = testSuitesInOrder(xml);
		assertEquals(1, testSuites.size(), "A spec with no paths must still generate the authorizations suite alone, without crashing: " + xml);
		assertEquals("authorizations_TestSuite", testSuites.get(0).getAttribute("name"), xml);
	}

	@Test
	void largeNumberOfCustomAuthorizations_allGeneratedInGivenOrder() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			customAuthorizationsFile.add(customAuthorization("Login" + i, "POST", "https://api.example.com/security/token" + i));
		}

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		int previousIndex = -1;
		for (int i = 0; i < 20; i++) {
			int index = xml.indexOf("Login" + i + "_TestCase");
			assertTrue(index > previousIndex, "Login" + i + " must appear after the previous entry in document order: " + xml);
			previousIndex = index;
		}
	}

	@Test
	void endpointThatIsNotAValidUrl_doesNotCrash() throws Exception {
		OpenAPI openAPI = parseSpec();
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Login", "POST", "not-a-valid-url"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("not-a-valid-url"), xml);
	}

	@Test
	void multipleRealResourcesCoexistWithAuthorizations_orderAndCountAreCorrect() throws Exception {
		OpenAPI openAPI = parseSpec(TWO_RESOURCES_SPEC);
		List<CustomAuthorizationRequest> customAuthorizationsFile = Arrays.asList(
				customAuthorization("Login", "POST", "https://api.example.com/security/token"),
				customAuthorization("Refresh", "POST", "https://api.example.com/security/refresh"));

		SoapUIProject soapUIProject = buildProject(openAPI, customAuthorizationsFile);
		String xml = soapUIProject.getFileContent();

		List<Element> testSuites = testSuitesInOrder(xml);
		assertEquals(3, testSuites.size(), xml);
		assertEquals("authorizations_TestSuite", testSuites.get(0).getAttribute("name"), xml);
	}
}
