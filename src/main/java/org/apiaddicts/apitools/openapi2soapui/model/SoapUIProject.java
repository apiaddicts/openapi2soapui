package org.apiaddicts.apitools.openapi2soapui.model;

import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SOAP_UI_PROJECT_FILE_EXTENSION;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SOAP_UI_PROJECT_FILE_NAME;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SUITE_SUFFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.CASE_SUFFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.STEP_SUFFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.DEFAULT_REQUEST_NAME;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.EJECUTION_TEST_STEP;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.HEADER;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.PATH;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.QUERY;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.DEFAULT;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.JSON;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SUCCESS_TEST_CASE;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.VALID_HTTP_STATUS_CODES_ASSERTION;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.WRONG_STATUS_CODE;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SCRIPT_ASSERTION;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.MISSING_BODY_PROPERTY_VARIANT_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.WRONG_BODY_PROPERTY_VARIANT_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.HAS_SCOPES_VARIANT_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.APPLICATION_TOKEN_VARIANT_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.MICROCKS_RESPONSE_NAME_HEADER;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.AUTHORIZATIONS_TEST_SUITE_NAME;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SELECT_QUERY_PARAM;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.EXCLUDE_QUERY_PARAM;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_SUITE_SUFFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_CASE_OK_ALL_PROPERTIES;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_CASE_OK_REQUIRED_PROPERTIES;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_CASE_ERROR_STATUS_CODE_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_CASE_ERROR_REQUIRED_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_ARRAY_ITEM_SEGMENT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.net.MalformedURLException;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.XmlException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.JMSDeliveryModeTypeConfig;
import com.eviware.soapui.config.JMSHeaderConfConfig;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.StringToStringMapConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.config.CredentialsConfig.AuthType;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenPosition;
import com.eviware.soapui.impl.rest.OAuth2Profile.OAuth2Flow;
import com.eviware.soapui.impl.rest.RestRepresentation.Type;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import org.apiaddicts.apitools.openapi2soapui.request.GrantType;
import org.apiaddicts.apitools.openapi2soapui.request.Header;
import org.apiaddicts.apitools.openapi2soapui.request.ExampleValues;
import org.apiaddicts.apitools.openapi2soapui.request.ExamplesConfig;
import org.apiaddicts.apitools.openapi2soapui.request.CustomAuthorizationRequest;
import org.apiaddicts.apitools.openapi2soapui.util.QueryParamExampleUtils;
import org.apiaddicts.apitools.openapi2soapui.util.RefResolver;

/**
 * Class with properties to build SoapUI Project
 */
@Slf4j
@Getter
public class SoapUIProject {
	/**
	 * apiName from request body
	 */
	private String apiName;
	/**
	 * apiVersion from Open API Spec
	 */
	private String apiVersion;
	/**
	 * Open API Spec as Java Object
	 */
	private OpenAPI openAPI;
	/**
	 * Temporal file to save SoapUI Project
	 */
	private File file;
	/**
	 * Request headers from request body
	 */
	private List<Header> headers;
	/**
	 * SoapUI Project as Java Object
	 */
	private WsdlProject project;
	/**
	 * REST Service in SoapUI Project
	 */
	private RestService restService;
	/**
	 * Test case names from request body
	 */
	private Set<String> testCaseNames;
	/**
	 * When true, only GET and OPTIONS test cases are generated
	 */
	private boolean readOnly;
	/**
	 * When false (default), for every Method with a JSON request body, generates one 400 Test Case per required
	 * body property missing (recursing into nested required objects) plus one 400 Test Case per scalar body
	 * property given an invalid value — each variant otherwise using normal values for every other property.
	 * When true, collapses this to at most one "missing required property" Test Case (the first one found) and
	 * zero "wrong value" Test Cases.
	 */
	private boolean minimalEndpoints;
	/**
	 * When true, adds an X-Microcks-Response-Name header to each request, in addition to any custom headers
	 */
	private boolean microcksHeaders;
	/**
	 * When true, oneOf/anyOf schemas are resolved using their first candidate when generating example bodies.
	 * allOf schemas are always merged into a single object, regardless of this flag.
	 */
	private boolean generateOneOfAnyOf;
	/**
	 * When true, adds a Script Assertion to each main test-case request's test step that validates the response
	 * body against the JSON Schema of the operation's first 2xx JSON response
	 */
	private boolean validateSchema;
	/**
	 * Only relevant when validateSchema is true. When true, the response JSON Schema used by the
	 * validateSchema assertion is embedded literally in the Script Assertion text (previous/only
	 * behavior). When false (default), the schema is stored as a SoapUI Project Property and read
	 * at runtime from the script via a context.expand("${#Project#key}") call instead.
	 */
	private boolean schemaIsInline;
	/**
	 * Only relevant when validateSchema is true. When true (default), the JSON Schema embedded or
	 * referenced by the validateSchema assertion is pretty-printed (indented). When false, it is
	 * serialized compactly (no extra whitespace).
	 */
	private boolean schemaPrettyPrint;
	/**
	 * Custom example values from request body, used before falling back to internal defaults
	 */
	private ExamplesConfig examples;
	/**
	 * OpenAPI Operation for each generated Method, keyed by a stable path+httpMethod key (not by RestMethod
	 * object identity, which is not guaranteed stable across SoapUI accessor calls), used to build optional
	 * query parameter variant requests
	 */
	private Map<String, Operation> operationByMethodKey = new HashMap<>();
	/**
	 * When true, request-body example values are embedded literally in the JSON body.
	 * When false (default), each generated scalar body value is stored as a SoapUI Project
	 * Property and referenced from the body via a "${#Project#key}" expansion token.
	 */
	private boolean isInline;
	/**
	 * When true, in addition to the testCaseNames-based Test Cases, generates one extra Test Case per
	 * configured OAuth2 Profile beyond the first, each wired to that specific profile via its own
	 * Credentials config — independent of the default Request, which always uses the first profile (see
	 * setRequestAuthProfile) and is never duplicated by an extra Test Case for that same profile. No-op
	 * when there are no configured OAuth2 Profiles, or when only one is configured (or numberOfScopes
	 * resolves to 1): the default Request alone already covers that single variant.
	 */
	private boolean hasScopes;
	/**
	 * Only relevant when hasScopes is also true. When true, additionally generates one extra Test Case
	 * per configured OAuth2 Profile whose grant type is CLIENT_CREDENTIALS (an application-only token,
	 * with no user), separate from the hasScopes scope variant Test Cases. No-op when hasScopes is
	 * false, or when no CLIENT_CREDENTIALS-grant profile is configured.
	 */
	private boolean applicationToken;
	/**
	 * Only relevant when hasScopes is also true. The total
	 * number of Test Cases wired to a profile-based scope credential for this Method — counting both the
	 * default Request (always the first configured profile) and any extra scope-variant Test Cases —
	 * using the first numberOfScopes configured OAuth2 Profiles (in the order they were added to the
	 * SoapUI Project). Values less than 1 (null, zero, or negative) are treated as 1: no extra Test Case
	 * is generated, since the default Request alone already covers the first (and only) profile, whose scope-variant loop starts at the second scope-token variable rather
	 * than duplicating the default request. Values greater than or equal to the configured profile count
	 * use all configured profiles (default Request + one extra Test Case per remaining profile). Does not
	 * affect applicationToken Test Cases.
	 */
	private int numberOfScopes;
	/**
	 * Incremented once per JSON request body generated (see getRequestExample), used as a
	 * globally-unique prefix for Project Property keys so that fields with the same name/path
	 * across different operations never collide.
	 */
	private int bodyPropertyCounter = 0;
	/**
	 * Incremented once per validateSchema assertion built, used as a globally-unique suffix for the
	 * "schema<N>" Project Property key when schemaIsInline is false (mirrors bodyPropertyCounter).
	 */
	private int schemaPropertyCounter = 0;
	/**
	 * For the JSON request body currently being generated: maps each "${#Project#key}" token
	 * produced to whether its underlying value is string-typed (true) or not (false: number/
	 * boolean/date). Reset at the start of every getRequestExample call. Used after
	 * mapObjectToJsonString to strip the JSON quotes the org.json serializer necessarily puts
	 * around the token (a Java String) when the real value is not itself a JSON string.
	 */
	private Map<String, Boolean> currentBodyTokenTypes = new LinkedHashMap<>();
	/**
	 * While building a minimalEndpoints body-property-variant Test Case, the dotted path of the one required
	 * property to omit from the JSON body being built (see addBodyPropertyVariantTestCase); null otherwise,
	 * so the normal (main test case) body-building path is unaffected
	 */
	private String bodyVariantOmitPath;
	/**
	 * While building a minimalEndpoints body-property-variant Test Case, the one property whose value should
	 * be substituted with a type-aware invalid value instead of its normal example (see
	 * addBodyPropertyVariantTestCase); null otherwise, so the normal (main test case) body-building path is
	 * unaffected
	 */
	private BodyPropertyCandidate bodyVariantWrongCandidate;
	/**
	 * When true, switches Test Suite/Test Case generation to the RSI "APIs de servicios" naming convention
	 * (notes.txt §2.2.3): Test Suite {path}_{apiName}_{apiVersion}-{METHOD}-Suite (run type SEQUENTIAL,
	 * abortOnError false), and exactly 4 Test Cases per Method — {METHOD}_CaseOkAllProperties,
	 * {METHOD}_CaseOkRequiredProperties, one {METHOD}_CaseErrorStatusCode{StatusCode} per documented non-2xx
	 * response, and one {METHOD}_CaseErrorRequired{Field} per required body property and required query
	 * parameter — each carrying its own status-code and (when available) response-schema assertions,
	 * regardless of validateSchema. When true, testCaseNames and the minimalEndpoints "missing"/"wrong"
	 * body-property variants are not used for that Method; hasScopes/applicationToken variants are
	 * unaffected. When false (default), behavior is entirely unchanged.
	 */
	private boolean serviceApiConvention;

	/**
	 * A single candidate body property found by collectBodyPropertyCandidates, eligible to become a
	 * minimalEndpoints "wrong value" variant Test Case
	 */
	private static final class BodyPropertyCandidate {
		private final String path;
		@SuppressWarnings("rawtypes")
		private final Schema schema;

		@SuppressWarnings("rawtypes")
		private BodyPropertyCandidate(String path, Schema schema) {
			this.path = path;
			this.schema = schema;
		}
	}

	/**
	 * Backward-compatible overload; schemaPrettyPrint defaults to true.
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, true, examples);
	}

	/**
	 * Backward-compatible overload; hasScopes defaults to false.
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, false, examples);
	}

	/**
	 * Backward-compatible overload; applicationToken defaults to false.
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, hasScopes, false, examples);
	}

	/**
	 * Backward-compatible overload; numberOfScopes defaults to null, treated as 1 (no extra scope-variant
	 * Test Case beyond the default Request — see the numberOfScopes field javadoc).
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, Boolean applicationToken, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, hasScopes, applicationToken, null, examples);
	}

	/**
	 * Backward-compatible overload; customAuthorizationsFile defaults to none.
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, Boolean applicationToken, Integer numberOfScopes, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, hasScopes, applicationToken, numberOfScopes, examples, null);
	}

	/**
	 * Backward-compatible overload; serviceApiConvention defaults to false.
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, Boolean applicationToken, Integer numberOfScopes, ExamplesConfig examples, List<CustomAuthorizationRequest> customAuthorizationsFile) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, hasScopes, applicationToken, numberOfScopes, examples, customAuthorizationsFile, false);
	}

	/**
	 * SoapUIProject constructor
	 * Set default test case names if testCaseNames is null or empty
	 * Create temporal file to save SoapUI Project
	 * Create instance of WsdlProject as SoapUI Project
	 * Set SoapUI Project name
	 * Set SoapUI Project Authentication Profiles
	 * Add REST Service to SoapUI Project
	 * Set REST Service Endpoints
	 * Create the "authorizations" Test Suite (empty) if customAuthorizationsFile is not empty, so it is the first Test Suite in the project
	 * Set REST Service Resources
	 * Set SoapUI Project Test Cases
	 * Populate the "authorizations" Test Suite with its synthetic Resources/Methods/Requests and Test Cases
	 * @param apiName from request body
	 * @param openAPI OpenAPI Java Object
	 * @param oAuth2Profiles authentication profiles from request body
	 * @param headers from request body
	 * @param testCaseNames from request body
	 * @param readOnly if true, only GET and OPTIONS test cases are generated
	 * @param minimalEndpoints if false (default), generates a 400 test case per missing required body property and per invalid-value body property; if true, collapses this to at most one missing-required case and none for invalid values; has no effect when serviceApiConvention is true
	 * @param microcksHeaders if true, adds an X-Microcks-Response-Name header to each request, in addition to any custom headers
	 * @param generateOneOfAnyOf if true, oneOf/anyOf schemas are resolved using their first candidate when generating example bodies
	 * @param validateSchema if true, adds a Script Assertion to each main test-case request's test step that validates the response body against the JSON Schema of the operation's first 2xx JSON response; ignored when serviceApiConvention is true (that convention's status-code/schema assertions are always added)
	 * @param schemaIsInline only relevant when validateSchema is true; if false (default), the response JSON Schema is stored as a SoapUI Project Property and read via a context.expand("${#Project#key}") call instead of being embedded literally
	 * @param isInline if false (default), JSON request-body example values are stored as SoapUI Project Properties and referenced via a "${#Project#key}" token instead of being embedded literally
	 * @param schemaPrettyPrint if true (default), the JSON Schema used by the validateSchema assertion is pretty-printed (indented); if false, it is serialized compactly with no extra whitespace
	 * @param hasScopes if true, generates one additional test case per configured oAuth2Profiles entry beyond the first, each wired to that profile's own authentication, independent of the default request (which always uses the first profile and is never duplicated by an extra test case)
	 * @param applicationToken only relevant when hasScopes is also true; if true, additionally generates one extra test case per configured oAuth2Profiles entry whose grant type is CLIENT_CREDENTIALS, separate from the hasScopes scope variant test cases
	 * @param numberOfScopes only relevant when hasScopes is also true; the total number of test cases wired to a profile-based scope credential, counting the default request, using the first numberOfScopes configured oAuth2Profiles entries (in configured order). Values less than 1 (null, zero, negative) are treated as 1 (no extra test case). Does not affect applicationToken test cases
	 * @param examples custom example values from request body, used before falling back to internal defaults
	 * @param customAuthorizationsFile custom authorization requests from request body; if not empty, a dedicated "authorizations" Test Suite is created and added before the per-endpoint Test Suites
	 * @param serviceApiConvention if true, switches Test Suite/Test Case generation to the RSI "APIs de servicios" naming convention (see the field javadoc for the full naming/behavior description); if false (default), behavior is entirely unchanged
	 * @throws IOException
	 * @throws XmlException
	 * @throws SoapUIException
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, Boolean applicationToken, Integer numberOfScopes, ExamplesConfig examples, List<CustomAuthorizationRequest> customAuthorizationsFile, Boolean serviceApiConvention) throws IOException, XmlException, SoapUIException {
		this.apiName = apiName;
		this.openAPI = openAPI;
		this.headers = headers;
		this.examples = examples;

		this.apiVersion = openAPI.getInfo().getVersion();

		if (testCaseNames == null || testCaseNames.isEmpty()) {
			this.testCaseNames = new HashSet<>(Arrays.asList(SUCCESS_TEST_CASE));
		} else {
			this.testCaseNames = testCaseNames;
		}

		this.readOnly = Boolean.TRUE.equals(readOnly);
		this.minimalEndpoints = Boolean.TRUE.equals(minimalEndpoints);
		this.microcksHeaders = Boolean.TRUE.equals(microcksHeaders);
		this.generateOneOfAnyOf = Boolean.TRUE.equals(generateOneOfAnyOf);
		this.validateSchema = Boolean.TRUE.equals(validateSchema);
		this.schemaIsInline = Boolean.TRUE.equals(schemaIsInline);
		this.isInline = Boolean.TRUE.equals(isInline);
		this.schemaPrettyPrint = !Boolean.FALSE.equals(schemaPrettyPrint);
		this.hasScopes = Boolean.TRUE.equals(hasScopes);
		this.applicationToken = Boolean.TRUE.equals(applicationToken);
		this.numberOfScopes = (numberOfScopes != null) ? numberOfScopes : 0;
		this.serviceApiConvention = Boolean.TRUE.equals(serviceApiConvention);

		createTempFile();

		project = new WsdlProject();
		project.setName(apiName + "_" + apiVersion);

		if (oAuth2Profiles != null) {
			setAuthProfiles(oAuth2Profiles);
		}

		restService = (RestService) project.addNewInterface(apiName, RestServiceFactory.REST_TYPE);
		restService.setDescription(openAPI.getInfo().getDescription());

		setRestServiceEndpoints(openAPI.getServers(), serverPattern);

		WsdlTestSuite authorizationsTestSuite = null;
		if (customAuthorizationsFile != null && !customAuthorizationsFile.isEmpty()) {
			authorizationsTestSuite = project.addNewTestSuite(AUTHORIZATIONS_TEST_SUITE_NAME + "_" + SUITE_SUFFIX);
		}

		setRestServiceResources(openAPI.getPaths());
		setTestCases();

		if (authorizationsTestSuite != null) {
			setCustomAuthorizationTestCases(authorizationsTestSuite, customAuthorizationsFile);
		}
	}

	/**
	 * Create temporal file to save SoapUI Project
	 * @throws IOException
	 */
	private void createTempFile() throws IOException {
		file = File.createTempFile(SOAP_UI_PROJECT_FILE_NAME, SOAP_UI_PROJECT_FILE_EXTENSION);
	}

	/**
	 * Set REST Service Endpoints and BasePath
	 * Iterate OpenAPI servers, extract host part and set as Endpoint
	 * If REST Service has not basePath, extract from first server item and set it 
	 * @param servers list of servers in OpenAPI
	 */
	private void setRestServiceEndpoints(List<Server> servers, String serverPattern) {
		if (servers == null || servers.isEmpty()) return;
		List<Server> filtered;
		if (serverPattern != null && !serverPattern.isBlank()) {
			String cleanPattern = serverPattern.replace("%", "");
			Optional<Server> match = servers.stream()
					.filter(s -> s.getUrl().contains(cleanPattern))
					.findFirst();
			filtered = match.isPresent()
					? Collections.singletonList(match.get())
					: Collections.singletonList(servers.get(0));
		} else {
			filtered = Collections.singletonList(servers.get(0));
		}
		for (Server server : filtered) {
			String serverUrl = server.getUrl();
			try {
				URL url = new URL(serverUrl);
				String basePath = url.getPath();
				String protocol = url.getProtocol();
				String host = url.getHost();
				String endpoint = String.format("%s://%s", protocol, host);
				restService.addEndpoint(endpoint);
				if (restService.getBasePath() == null || restService.getBasePath().isBlank()) restService.setBasePath(basePath);
			} catch (MalformedURLException e) {
				log.debug("MalformedURLException", e);
				restService.addEndpoint(server.getUrl());
				restService.setBasePath("");
			}
		}
	}

	/**
	 * Set REST Service Resources
	 * Iterate OpenAPI paths and add as Resource to REST Service
	 * Set Methods to each Resource
	 * Set Request to Methods in each Resoruce
	 * @param paths list of paths in OpenAPI
	 */
	private void setRestServiceResources(Paths paths) {
		if (paths != null && !paths.isEmpty()) {
			paths.forEach((pathName, pathItem) -> {
				RestResource restResource = restService.addNewResource(pathName, pathName);
				setResourceMethods(restResource, pathItem.readOperationsMap());
				setMethodsRequests(pathName, pathItem);
			});
		}
	}

	/**
	 * Set Parameter Properties
	 * Set Resource/Method Parameter properties based on the properties of the OpenAPI Parameter
	 * @param parameter Resource/Method Parameter
	 * @param openAPIParameter OpenAPI Parameter
	 */
	private void setParameterProperties(RestParamProperty parameter, Parameter openAPIParameter) {
		if (parameter != null) {
			parameter.setDescription(openAPIParameter.getDescription());
			if (openAPIParameter.getRequired() != null && openAPIParameter.getRequired()) parameter.setRequired(true);
			
			if (openAPIParameter.getIn().equalsIgnoreCase(HEADER)) {
				parameter.setStyle(ParameterStyle.HEADER);
			} else if (openAPIParameter.getIn().equalsIgnoreCase(PATH)) {
				parameter.setStyle(ParameterStyle.TEMPLATE);
			} else if (openAPIParameter.getIn().equalsIgnoreCase(QUERY)) {
				parameter.setStyle(ParameterStyle.QUERY);
			}
		}
	}

	/**
	 * Get OpenAPI Parameter Example
	 * Validate if the parameter has the examples, example or x-example property and if so, it returns its value 
	 * @param openAPIParameter
	 * @return parameter example
	 */
	private Object getParameterExample(Parameter openAPIParameter) {
		if (openAPIParameter.getExample() != null) {
			return openAPIParameter.getExample();
		} else if (openAPIParameter.getExamples() != null && !openAPIParameter.getExamples().isEmpty()) {
			return openAPIParameter.getExamples().entrySet().iterator().next().getValue().getValue();
		} else if (openAPIParameter.getExtensions() != null && openAPIParameter.getExtensions().get("x-example") != null) {
			return openAPIParameter.getExtensions().get("x-example");
		}
		return null;
	}

	/**
	 * Set example to Resoruce Parameter
	 * @param restResource instance of Resoruce
	 * @param parameter instance of Resoruce Parameter
	 * @param openAPIParameter instance of OpenAPI Parameter
	 */
	private void setResourceParameterExample(RestResource restResource, RestParamProperty parameter, Parameter openAPIParameter) {
		Object example = getParameterExample(openAPIParameter);
		if (example != null && !example.toString().isBlank()) restResource.setPropertyValue(parameter.getName(), example.toString());
	}

	/**
	 * Set example to Method Parameter
	 * @param restMethod instance of Method
	 * @param parameter instance of Method Parameter
	 * @param openAPIParameter instance of OpenAPI Parameter
	 */
	private void setMethodParameterExample(RestMethod restMethod, RestParamProperty parameter, Parameter openAPIParameter) {
		Object example = getParameterExample(openAPIParameter);
		if (example != null && !example.toString().isBlank()) restMethod.setPropertyValue(parameter.getName(), example.toString());
	}

	/**
	 * Set Resource Parameters
	 * Iterate OpenAPI Path Parameters and set as Parameter of Resource
	 * @param restResource instance of Resoruce
	 * @param openAPIParameters list of OpenAPI Path Parameters
	 */
	private void setResourceParameters(RestResource restResource, List<Parameter> openAPIParameters) {
		if (openAPIParameters != null && !openAPIParameters.isEmpty()) {
			openAPIParameters.forEach(openAPIParameter -> {
				RestParamProperty resourceParam = restResource.addProperty(openAPIParameter.getName());
				if (resourceParam != null) {
					setParameterProperties(resourceParam, openAPIParameter);
					setResourceParameterExample(restResource, resourceParam, openAPIParameter);
				}
			});
		}
	}

	/**
	 * Set Method Parameters
	 * Iterate OpenAPI Operation Parameters and set as Parameter of Method
	 * @param restMethod instance of Method
	 * @param openAPIParameters list of OpenAPI Operation Parameters
	 */
	private void setMethodParameters(RestMethod restMethod, List<Parameter> openAPIParameters) {
		if (openAPIParameters != null && !openAPIParameters.isEmpty()) {
			openAPIParameters.forEach(openAPIParameter -> {
				RestParamProperty methodParam = restMethod.addProperty(openAPIParameter.getName());
				if (methodParam != null) {
					setParameterProperties(methodParam, openAPIParameter);
					setMethodParameterExample(restMethod, methodParam, openAPIParameter);
				}
			});
		}
	}
	
	/**
	 * Set Resource Methods
	 * Iterate OpenAPI Path Operations and add as Method to Resource
	 * Set Methods to Resource
	 * Set Response Representatios (For each Response code and for each media types in response code)
	 * If has request body set Request Representatios (Media types)
	 * @param operations list of path operations
	 */
	private void setResourceMethods(RestResource restResource, Map<HttpMethod, Operation> operations) {
		if (operations != null && !operations.isEmpty()) {
			operations.forEach((httpMethod, operation) -> {
				RestMethod restMethod = restResource.addNewMethod((operation.getOperationId() != null) ? operation.getOperationId() : httpMethod.name());
				restMethod.setMethod(RestRequestInterface.HttpMethod.valueOf(httpMethod.name()));
				restMethod.setDescription((operation.getDescription() != null) ? operation.getDescription() : "");
				
				if (operation.getRequestBody() != null) {
					setMethodRequestRepresentations(restMethod, operation.getRequestBody());
				}
				
				setMethodResponseRepresentations(restMethod, operation.getResponses());
			});
		}
	}

	/**
	 * Set Request Representatios to Method
	 * Iterate content/mediaTypes of OpenAPI RequestBody and set as Request Representation
	 * @param restMethod instance of Method
	 * @param requestBody instance of OpenAPI RequestBody
	 */
	private void setMethodRequestRepresentations(RestMethod restMethod, RequestBody requestBody) {
		Content mediaTypes = requestBody.getContent();
		if (mediaTypes != null && !mediaTypes.isEmpty()) {
			mediaTypes.forEach((mediaTypeStr, mediaTypeObject) -> {
				RestRepresentation restRepresentation = restMethod.addNewRepresentation(Type.REQUEST);
				restRepresentation.setMediaType(mediaTypeStr);
			});
		}
	}

	/**
	 * Set Response Representatios to Method
	 * Iterate responses/content/mediaTypes of OpenAPI Responses and set as Response Representation
	 * @param restMethod instance of Method
	 * @param responses instance of OpenAPI Responses
	 */
	private void setMethodResponseRepresentations(RestMethod restMethod, ApiResponses responses) {
		if (responses != null && !responses.isEmpty()) {
			responses.forEach((code, responseItem) -> {
				Content mediaTypes = responseItem.getContent();
				if (mediaTypes != null && !mediaTypes.isEmpty()) {
					mediaTypes.forEach((mediaTypeStr, mediaTypeItem) -> {
						RestRepresentation representation = restMethod.addNewRepresentation(Type.RESPONSE);
						representation.setMediaType(mediaTypeStr);
						if (!code.equalsIgnoreCase(DEFAULT)) {
							representation.setStatus(Arrays.asList(code));
						} else {
							representation.setStatus(new ArrayList<>());
						}
					});
				}
			});
		}
	}

	/**
	 * Set Request to Method
	 * Find Resource by Full Path
	 * Iterate Operations in OpenAPI Path
	 * For each Operation, search the Method by operation id or method name
	 * Create and configure Request and add to Method
	 * @param pathName path name to find Resource
	 * @param pathItem instance of OpenAPI Path to iterate its Operations
	 */
	private void setMethodsRequests(String pathName, PathItem pathItem) {
		RestResource restResource = restService.getResourceByFullPath(restService.getBasePath() + pathName);
		
		if (restResource != null) {
			pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
				RestMethod restMethod = restResource.getRestMethodByName((operation.getOperationId() != null) ? operation.getOperationId() : httpMethod.name());
				if (restMethod == null) return;
				operationByMethodKey.put(methodKey(restResource.getPath(), httpMethod.name()), operation);
				RestRequest restRequest = restMethod.addNewRequest(DEFAULT_REQUEST_NAME);
				RestRequestConfig restRequestConfig = restRequest.getConfig();
						
				restRequestConfig.setOriginalUri(restService.getEndpoints()[0] + restResource.getFullPath(true));
				setRequestAuthProfile(restRequestConfig);
				setRequestJMSConfig(restRequestConfig);
				
				restRequest.setEndpoint(restService.getEndpoints()[0]);
				setRequestMediaType(restRequest, operation);

				setResourceParameters(restResource, pathItem.getParameters());
				setMethodParameters(restMethod, operation.getParameters());
				
				if (operation.getRequestBody() != null) {
					Content content = operation.getRequestBody().getContent();
					if (content != null && !content.isEmpty()) {
						setRequestContent(restRequest, content);
					}
				}
				
				setRequestHeaders(restRequest, operation);
			});
		}
	}

	/**
	 * Set Request Headers
	 * Iterate headers received in request body and set to Request
	 * If microcksHeaders is true, additionally set the X-Microcks-Response-Name header
	 * @param restRequest instance of Method Request
	 * @param operation instance of OpenAPI Operation, used to resolve the Microcks response example name
	 */
	private void setRequestHeaders(RestRequest restRequest, Operation operation) {
		StringToStringMap requestHeaders = new StringToStringMap();
		if (headers != null && !headers.isEmpty()) {
			headers.forEach(header -> requestHeaders.put(header.getKey(), header.getValue()));
		}
		if (microcksHeaders && !requestHeaders.containsKey(MICROCKS_RESPONSE_NAME_HEADER)) {
			String exampleName = getMicrocksExampleName(operation);
			requestHeaders.put(MICROCKS_RESPONSE_NAME_HEADER, exampleName != null ? exampleName : DEFAULT);
		}
		if (!requestHeaders.isEmpty()) {
			restRequest.setRequestHeaders(requestHeaders);
		}
	}

	/**
	 * Get Microcks Example Name
	 * Look for the first named example defined on the operation's responses, checking 2xx responses in
	 * spec declaration order first, then the "default" response, and every media type within each response
	 * @param operation instance of OpenAPI Operation
	 * @return example name, or null if the operation has no response example
	 */
	private String getMicrocksExampleName(Operation operation) {
		if (operation.getResponses() == null) return null;
		List<ApiResponse> candidateResponses = new ArrayList<>();
		operation.getResponses().forEach((code, response) -> {
			if (code.startsWith("2")) candidateResponses.add(response);
		});
		if (operation.getResponses().getDefault() != null) {
			candidateResponses.add(operation.getResponses().getDefault());
		}

		for (ApiResponse response : candidateResponses) {
			String exampleName = getFirstExampleName(response);
			if (exampleName != null) return exampleName;
		}
		return null;
	}

	/**
	 * Get First Example Name
	 * Iterate every media type of the Response content and return the key of the first non-empty examples map
	 * @param response instance of OpenAPI Response
	 * @return example name, or null if no media type of this response has named examples
	 */
	private String getFirstExampleName(ApiResponse response) {
		if (response == null || response.getContent() == null || response.getContent().isEmpty()) return null;
		for (MediaType mediaType : response.getContent().values()) {
			if (mediaType.getExamples() != null && !mediaType.getExamples().isEmpty()) {
				return mediaType.getExamples().entrySet().iterator().next().getKey();
			}
		}
		return null;
	}

	/**
	 * Set Request Content
	 * Get OpenAPI Request Body example and set as Request Content
	 * @param restRequest instance of Request
	 * @param content instance of OpenAPI RequestBody.Content
	 */
	private void setRequestContent(RestRequest restRequest, Content content) {
		if (content != null && !content.isEmpty()) {
			content.forEach((mediaTypeStr, mediaTypeObject) -> {
				if (mediaTypeStr.toLowerCase().contains(JSON)) {
					RefResolver refResolver = new RefResolver(openAPI);
					Object example = getRequestExample(mediaTypeObject, refResolver);
					if (example != null) {
						String exampleStr = mapObjectToJsonString(example);
						exampleStr = stripQuotesAroundNonStringTokens(exampleStr);
						if (exampleStr != null) {
							restRequest.setRequestContent(exampleStr);
						}
					}
				}
			});
		}
	}

	/**
	 * Get Request Body Example
	 * Validate if the MediaType or Schema has the examples or exampleproperty and if so, it returns its value
	 * If not, iterate all properties of Schema and set example for each one
	 * @param mediaType instance of OpenAPI Media Type
	 * @param refResolver instance of RefResolver
	 * @return example
	 */
	@SuppressWarnings("rawtypes")
	private Object getRequestExample(MediaType mediaType, RefResolver refResolver) {
		bodyPropertyCounter++;
		currentBodyTokenTypes = new LinkedHashMap<>();
		Object example;
		Schema<?> schema = resolveComposedSchema(refResolver.resolveSchema(mediaType.getSchema()), refResolver);
		if (mediaType.getExample() != null) {
			example = mediaType.getExample();
		} else if (mediaType.getExamples() != null && !mediaType.getExamples().isEmpty()) {
			example = mediaType.getExamples().entrySet().iterator().next().getValue().getValue();
		} else {
			example = schema.getExample();
		}
		if (example == null) {
			Map<String, Schema> properties = schema.getProperties();
			example = iterateProperties(properties, refResolver, "");
		}
		return example;
	}

	/**
	 * Iterate all properties of schema an set an example, if schema is $ref, $ref is resolved
	 * @param properties map of properties (property name, property schema)
	 * @param refResolver to help resolve schemas $ref
	 * @param path underscore-joined property path built so far, used to key Project Properties when isInline is false
	 * @return json object with example of its properties
	 */
	@SuppressWarnings("rawtypes")
	private JSONObject iterateProperties(Map<String, Schema> properties, RefResolver refResolver, String path) {
		JSONObject json = new JSONObject();
		if (properties != null  && !properties.isEmpty()) {
			properties.forEach((propertyName, property) -> {
				String childPath = path.isEmpty() ? propertyName : path + "_" + propertyName;
				if (childPath.equals(bodyVariantOmitPath)) return;
				property = refResolver.resolveSchema(property);
				try {
					Object example = getPropertyExample(property, refResolver, childPath);
					json.put(propertyName, example);
				} catch (JSONException e) {
					log.warn("Error iterateProperties", e);
				}
			});
		}
		return json;
	}

	/**
	 * Get property example
	 * Validate if Property Schema has example, if so, return example value
	 * If not, return a generic value according to data type
	 * @param property
	 * @param refResolver
	 * @param path underscore-joined property path, used to key Project Properties when isInline is false
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	private Object getPropertyExample(Schema property, RefResolver refResolver, String path) throws JSONException {
		if (bodyVariantWrongCandidate != null && path.equals(bodyVariantWrongCandidate.path)) {
			return registerBodyValue(path, QueryParamExampleUtils.invalidValue(bodyVariantWrongCandidate.schema, examples != null ? examples.getWrong() : null));
		}
		Object example = property.getExample();
		if (example != null) {
			return registerBodyValue(path, example);
		}
		return getExampleForResolvedType(resolveComposedSchema(property, refResolver), refResolver, path);
	}

	/**
	 * Get example for a resolved (non-composed) schema, dispatching by concrete schema type
	 * @param property resolved Schema
	 * @param refResolver instance of RefResolver
	 * @param path underscore-joined property path, used to key Project Properties when isInline is false
	 * @return example value for the schema's type
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	private Object getExampleForResolvedType(Schema property, RefResolver refResolver, String path) throws JSONException {
		if (property instanceof ObjectSchema) {
			return iterateProperties(((ObjectSchema) property).getProperties(), refResolver, path);
		} else if (property instanceof ArraySchema) {
			JSONArray jsonArray = new JSONArray();
			Schema<?> items = refResolver.resolveSchema(((ArraySchema) property).getItems());
			jsonArray.put(getPropertyExample(items, refResolver, path + "_item"));
			return jsonArray;
		} else if (property instanceof IntegerSchema || property instanceof NumberSchema) {
			return registerBodyValue(path, getConfiguredExample(false, ExampleValues::getNumber, java.math.BigDecimal.ZERO));
		} else if (property instanceof BooleanSchema) {
			return registerBodyValue(path, getConfiguredExample(false, ExampleValues::getBooleanValue, true));
		} else if (property instanceof DateSchema) {
			return registerBodyValue(path, getConfiguredExample(false, ExampleValues::getDate, new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
		} else if (property instanceof StringSchema) {
			return registerBodyValue(path, getStringExample((StringSchema) property));
		}
		return registerBodyValue(path, "");
	}

	/**
	 * Get example for a String schema, honoring enum values and the date-time format before falling back
	 * to a configured/default string
	 * @param stringProperty String Schema
	 * @return example value
	 */
	private Object getStringExample(StringSchema stringProperty) {
		List<String> enums = stringProperty.getEnum();
		if (enums != null && !enums.isEmpty()) {
			return enums.get(0);
		} else if ("date-time".equalsIgnoreCase(stringProperty.getFormat())) {
			return getConfiguredExample(false, ExampleValues::getDateTime, "");
		}
		return getConfiguredExample(false, ExampleValues::getString, "");
	}

	/**
	 * Look up a custom example value from the request body's "examples" configuration, falling back to
	 * defaultValue if "examples" was not provided, or the requested space/field was not configured
	 * @param wrong true to look up examples.wrong, false to look up examples.successful
	 * @param getter accessor for the desired field on ExampleValues
	 * @param defaultValue value to use if not configured
	 * @return the configured value, or defaultValue
	 */
	private <T> T getConfiguredExample(boolean wrong, java.util.function.Function<ExampleValues, T> getter, T defaultValue) {
		if (examples == null) {
			return defaultValue;
		}
		ExampleValues values = wrong ? examples.getWrong() : examples.getSuccessful();
		if (values == null) {
			return defaultValue;
		}
		T configured = getter.apply(values);
		return configured != null ? configured : defaultValue;
	}

	/**
	 * Resolve oneOf/anyOf/allOf composition on a schema
	 * allOf is always merged into a single object schema (properties/required of every member), regardless of generateOneOfAnyOf
	 * When generateOneOfAnyOf is true, oneOf/anyOf are resolved to their first candidate schema
	 * Loops to also resolve composition nested inside a merged/chosen candidate, bounded to avoid runaway recursion
	 * @param schema to resolve
	 * @param refResolver instance of RefResolver
	 * @return resolved schema, or the original schema unchanged if it has no applicable composition
	 */
	@SuppressWarnings("rawtypes")
	private Schema resolveComposedSchema(Schema schema, RefResolver refResolver) {
		final int MAX_ITERATIONS = 10;
		for (int i = 0; i < MAX_ITERATIONS; i++) {
			Schema resolved = resolveComposedSchemaOnce(schema, refResolver);
			if (resolved == schema) {
				return resolved;
			}
			schema = resolved;
		}
		return schema;
	}

	/**
	 * Resolve a single level of oneOf/anyOf/allOf composition on a schema
	 * @param schema to resolve
	 * @param refResolver instance of RefResolver
	 * @return resolved schema, or the original schema unchanged if it has no applicable composition
	 */
	@SuppressWarnings("rawtypes")
	private Schema resolveComposedSchemaOnce(Schema schema, RefResolver refResolver) {
		List<Schema> allOf = schema.getAllOf();
		if (allOf != null && !allOf.isEmpty()) {
			return mergeAllOf(allOf, refResolver);
		} else if (generateOneOfAnyOf && schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
			return refResolver.resolveSchema((Schema) schema.getOneOf().get(0));
		} else if (generateOneOfAnyOf && schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
			return refResolver.resolveSchema((Schema) schema.getAnyOf().get(0));
		}
		return schema;
	}

	/**
	 * Merge every allOf member into a single object schema (properties/required of every member)
	 * @param allOf list of member schemas to merge
	 * @param refResolver instance of RefResolver
	 * @return merged object schema
	 */
	@SuppressWarnings("rawtypes")
	private ObjectSchema mergeAllOf(List<Schema> allOf, RefResolver refResolver) {
		ObjectSchema merged = new ObjectSchema();
		Map<String, Schema> mergedProperties = new HashMap<>();
		List<String> mergedRequired = new ArrayList<>();
		for (Schema member : allOf) {
			Schema resolvedMember = refResolver.resolveSchema(member);
			if (resolvedMember.getProperties() != null) {
				mergedProperties.putAll(resolvedMember.getProperties());
			}
			if (resolvedMember.getRequired() != null) {
				mergedRequired.addAll(resolvedMember.getRequired());
			}
		}
		merged.setProperties(mergedProperties);
		merged.setRequired(mergedRequired);
		return merged;
	}

	/**
	 * Convert Object or JSONObject to JSON String, always pretty-printed (indented)
	 * @param object to convert
	 * @return json string
	 */
	private String mapObjectToJsonString(Object object) {
		return mapObjectToJsonString(object, true);
	}

	/**
	 * Convert Object or JSONObject to JSON String
	 * @param object to convert
	 * @param prettyPrint if true, the JSON is indented; if false, it is serialized compactly with no extra whitespace
	 * @return json string
	 */
	private String mapObjectToJsonString(Object object, boolean prettyPrint) {
		String jsonString = null;
		if (object instanceof JSONObject) {
			try {
				jsonString = prettyPrint ? ((JSONObject) object).toString(2) : ((JSONObject) object).toString();
			} catch (JSONException e) {
				log.debug("Error mapObjectToJsonString", e);
			}
		} else {
			ObjectMapper mapper = new ObjectMapper();
			try {
				jsonString = prettyPrint
						? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object).replaceAll("\\r", "")
						: mapper.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				log.debug("Error mapObjectToJsonString", e);
			}
		}
		return jsonString;
	}

	/**
	 * If isInline is false and value is a tokenizable scalar (String/Number/Boolean), stores it as a
	 * SoapUI Project Property keyed by a globally-unique, path-based name and returns the
	 * "${#Project#key}" reference token to place in the JSON body instead of the literal value.
	 * Otherwise (isInline true, value is null, or value is a non-scalar Object such as a whole-object
	 * schema example) returns value unchanged.
	 * @param path underscore-joined property path built by iterateProperties/getExampleForResolvedType
	 * @param value the computed example value for that path
	 * @return the literal value (isInline / non-scalar) or a "${#Project#key}" token (otherwise)
	 */
	private Object registerBodyValue(String path, Object value) {
		if (isInline || value == null || !(value instanceof String || value instanceof Number || value instanceof Boolean)) {
			return value;
		}
		String key = "body" + bodyPropertyCounter + "_" + path.replaceAll("[^A-Za-z0-9_]", "_");
		project.setPropertyValue(key, String.valueOf(value));
		currentBodyTokenTypes.put(key, value instanceof String);
		return "${#Project#" + key + "}";
	}

	/**
	 * Strips the JSON quotes the org.json serializer necessarily wrote around each non-string token
	 * (org.json only knows how to serialize the token as a Java String), so that after SoapUI
	 * resolves the property at runtime the field keeps its correct JSON type (number/boolean)
	 * instead of becoming a quoted string. No-op when isInline is true (currentBodyTokenTypes is empty).
	 * @param json serialized request body JSON, possibly containing "${#Project#key}" tokens
	 * @return json with quotes stripped around every non-string token
	 */
	private String stripQuotesAroundNonStringTokens(String json) {
		if (json == null || currentBodyTokenTypes.isEmpty()) {
			return json;
		}
		for (Map.Entry<String, Boolean> entry : currentBodyTokenTypes.entrySet()) {
			if (Boolean.FALSE.equals(entry.getValue())) {
				String token = "${#Project#" + entry.getKey() + "}";
				json = json.replace("\"" + token + "\"", token);
			}
		}
		return json;
	}

	/**
	 * Set Media Type to Request
	 * If OpenAPI Operation has success response, get Medi Type of success response and set as Reques Media Type
	 * @param restRequest instance of Request
	 * @param operation instance of OpenAPI Operation
	 */
	private void setRequestMediaType(RestRequest restRequest, Operation operation) {
		Set<String> successResponsesCodes = operation.getResponses().keySet().stream().filter(s -> s.startsWith("2")).collect(Collectors.toSet());
		if (!successResponsesCodes.isEmpty()) {
			ApiResponse succesResponse = operation.getResponses().get(successResponsesCodes.iterator().next());
			if (succesResponse.getContent() != null && succesResponse.getContent().entrySet() != null && !succesResponse.getContent().entrySet().isEmpty()) {
				String mediaTypeStr = succesResponse.getContent().entrySet().iterator().next().getKey();
				restRequest.setMediaType(mediaTypeStr);
			}
		}
	}

	/**
	 * Set Request JMS Config
	 * Add new JMS Config to Request Config
	 * @param restRequestConfig instance of Request Config
	 */
	private void setRequestJMSConfig(RestRequestConfig restRequestConfig) {
		JMSHeaderConfConfig jmsConfig = restRequestConfig.addNewJmsConfig();
		jmsConfig.setJMSDeliveryMode(JMSDeliveryModeTypeConfig.PERSISTENT);
		restRequestConfig.addNewJmsPropertyConfig();
	}

	/**
	 * Set Request Authentication Profile
	 * If SoapUi Project has OAuth 2.0 Profiles, set selected authentication profile to Request 
	 * @param restRequestConfig
	 */
	private void setRequestAuthProfile(RestRequestConfig restRequestConfig) {
		List<OAuth2Profile> oAuth2ProfileList = project.getOAuth2ProfileContainer().getOAuth2ProfileList();
		if (oAuth2ProfileList != null && !oAuth2ProfileList.isEmpty()) {
			OAuth2Profile oAuth2Profile = project.getOAuth2ProfileContainer().getOAuth2ProfileList().get(0);
			CredentialsConfig credentialsConfig = CredentialsConfig.Factory.newInstance();
			credentialsConfig.setSelectedAuthProfile(oAuth2Profile.getName());
			credentialsConfig.setAuthType(AuthType.O_AUTH_2_0);
			restRequestConfig.setCredentials(credentialsConfig);
		}
	}

	/**
	 * Set Authentication profiles to SoapUI Project
	 * @param oAuth2Profiles list of authentication items
	 */
	private void setAuthProfiles(List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles) {
		if (oAuth2Profiles != null && !oAuth2Profiles.isEmpty()) {
			oAuth2Profiles.forEach(this::setAuthProfile);
		}
	}
	
	/**
	 * Configure OAuth 2.0 Profile and add to SoapUI Project
	 * @param oAuth2Profiles authentication item
	 */
	private void setAuthProfile(org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile oAuth2Profile) {
		if (oAuth2Profile.getGrantType() != null) {
			OAuth2Flow oAuth2Flow = (oAuth2Profile.getGrantType().equals(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS))
					? OAuth2Flow.valueOf(oAuth2Profile.getGrantType().getText()) : OAuth2Flow.valueOf(oAuth2Profile.getGrantType().getText()+"_GRANT");
			
			OAuth2Profile oAuth2ProfileSoapUI = project.getOAuth2ProfileContainer().addNewOAuth2Profile(oAuth2Profile.getProfileName());
			oAuth2ProfileSoapUI.setOAuth2Flow(oAuth2Flow);
			oAuth2ProfileSoapUI.setClientID(oAuth2Profile.getClientId());
			oAuth2ProfileSoapUI.setAccessTokenPosition(AccessTokenPosition.valueOf(oAuth2Profile.getAccessTokenPosition().name()));
			if (oAuth2Profile.getScope() != null) {
				oAuth2ProfileSoapUI.setScope(oAuth2Profile.getScope());
			}
			
			if (oAuth2Flow.equals(OAuth2Flow.AUTHORIZATION_CODE_GRANT)
					|| oAuth2Flow.equals(OAuth2Flow.CLIENT_CREDENTIALS_GRANT)
					|| oAuth2Flow.equals(OAuth2Flow.RESOURCE_OWNER_PASSWORD_CREDENTIALS)) {
				oAuth2ProfileSoapUI.setClientSecret(oAuth2Profile.getClientSecret());
				oAuth2ProfileSoapUI.setAccessTokenURI(oAuth2Profile.getAccessTokenURI());
			}

			if (oAuth2Flow.equals(OAuth2Flow.RESOURCE_OWNER_PASSWORD_CREDENTIALS)) {
				oAuth2ProfileSoapUI.setResourceOwnerName(oAuth2Profile.getUsername());
				oAuth2ProfileSoapUI.setResourceOwnerPassword(oAuth2Profile.getPassword());
			}
			
			if (oAuth2Flow.equals(OAuth2Flow.AUTHORIZATION_CODE_GRANT)
					|| oAuth2Flow.equals(OAuth2Flow.IMPLICIT_GRANT)) {
				oAuth2ProfileSoapUI.setAuthorizationURI(oAuth2Profile.getAuthorizationURI());
				oAuth2ProfileSoapUI.setRedirectURI(oAuth2Profile.getRedirectURI());
			}
			
		} else {
			OAuth2Profile oAuth2ProfileSoapUI = project.getOAuth2ProfileContainer().addNewOAuth2Profile(oAuth2Profile.getProfileName());
			if (oAuth2Profile.getAccessToken() == null || oAuth2Profile.getAccessToken().isBlank()) {
				oAuth2ProfileSoapUI.setAccessToken("");
			} else {
				oAuth2ProfileSoapUI.setAccessToken(oAuth2Profile.getAccessToken());
			}
		}
	}
	
	/**
	 * The Test Suite itself is created (via project.addNewTestSuite) before setRestServiceResources()/setTestCases()
	 * run, so it is always the first Test Suite in the generated SoapUI Project; the synthetic Resources built here
	 * are only added to the REST Service afterwards, so setTestCases() never iterates over them and does not
	 * generate a duplicate, unwanted Test Suite for them
	 * @param testSuite the already-created "authorizations" Test Suite to populate
	 * @param customAuthorizationsFile custom authorization requests from request body, in the order they should appear
	 */
	private void setCustomAuthorizationTestCases(WsdlTestSuite testSuite, List<CustomAuthorizationRequest> customAuthorizationsFile) {
		int index = 0;
		for (CustomAuthorizationRequest customRequest : customAuthorizationsFile) {
			index++;
			RestResource restResource = restService.addNewResource(customRequest.getName(),
					"/" + AUTHORIZATIONS_TEST_SUITE_NAME + "/" + index);
			RestMethod restMethod = restResource.addNewMethod(customRequest.getName());
			restMethod.setMethod(RestRequestInterface.HttpMethod.valueOf(customRequest.getMethod().toUpperCase()));

			RestRequest restRequest = restMethod.addNewRequest(DEFAULT_REQUEST_NAME);
			restRequest.setEndpoint(customRequest.getEndpoint());
			restRequest.getConfig().setOriginalUri(customRequest.getEndpoint());
			if (customRequest.getMediaType() != null && !customRequest.getMediaType().isBlank()) {
				restRequest.setMediaType(customRequest.getMediaType());
			}
			if (customRequest.getBody() != null && !customRequest.getBody().isBlank()) {
				restRequest.setRequestContent(customRequest.getBody());
			}
			if (customRequest.getHeaders() != null && !customRequest.getHeaders().isEmpty()) {
				StringToStringMap requestHeaders = new StringToStringMap();
				customRequest.getHeaders().forEach(header -> requestHeaders.put(header.getKey(), header.getValue()));
				restRequest.setRequestHeaders(requestHeaders);
			}

			WsdlTestCase testCase = testSuite.addNewTestCase(customRequest.getName() + "_" + CASE_SUFFIX);
			TestStepConfig stepConfig = RestRequestStepFactory.createConfig(restRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
			testCase.addTestStep(stepConfig);
		}
	}

	/**
	 * Set Test Cases
	 * Iterate SoapUI Project Resources and Methods and add Test Suite for each Method
	 * Add Test Cases to Test Suite
	 * Add Test Steps (Request and Groovy Script) to each Test Case
	 */
	private void setTestCases() {
		List<RestResource> resources = restService.getAllResources();
		if (resources == null || resources.isEmpty()) return;
		resources.forEach(restResource -> {
			List<RestMethod> methods = restResource.getRestMethodList();
			if (methods != null && !methods.isEmpty()) {
				methods.forEach(restMethod -> addTestSuiteForMethod(restResource, restMethod));
			}
		});
	}

	/**
	 * Add Test Suite for a single Method
	 * Skipped for non read-only Methods when readOnly is true
	 * Adds a Test Case per configured test case name, plus minimalEndpoints body-property-variant Test Cases
	 * When validateSchema is true, also attaches a Script Assertion validating the response body against the
	 * operation's JSON Schema to each main test step, unless the operation declares a $select/$exclude query
	 * parameter (a partial response would not match the full schema)
	 * @param restResource instance of Resource owning the Method
	 * @param restMethod instance of Method to generate the Test Suite for
	 */
	private void addTestSuiteForMethod(RestResource restResource, RestMethod restMethod) {
		String method = restMethod.getMethod().name();
		if (readOnly && !"GET".equals(method) && !"OPTIONS".equals(method)) return;
		Operation operation = operationByMethodKey.get(methodKey(restResource.getPath(), method));

		WsdlTestSuite testSuite;
		if (serviceApiConvention) {
			String testSuiteName = restResource.getPath() + "_" + apiName + "_" + apiVersion + "-" + method + "-" + SERVICE_API_SUITE_SUFFIX;
			testSuite = project.addNewTestSuite(testSuiteName);
			testSuite.setRunType(TestSuite.TestSuiteRunType.SEQUENTIAL);
			testSuite.setAbortOnError(false);
			addServiceApiConventionTestCases(restMethod, testSuite, operation, method);
		} else {
			String testSuiteName = restResource.getPath() + "_" + method + "_" + SUITE_SUFFIX;
			testSuite = project.addNewTestSuite(testSuiteName);
			for (String testCaseNameItem : testCaseNames) {
				String testCaseName = testCaseNameItem + "_" + CASE_SUFFIX;
				WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
				TestStepConfig ejecutionTestStepConfig = RestRequestStepFactory.createConfig(restMethod.getRequestByName(DEFAULT_REQUEST_NAME), EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
				WsdlTestStep testStep = testCase.addTestStep(ejecutionTestStepConfig);
				if (validateSchema && !hasPartialResponseQueryParam(operation)) {
					addSchemaValidationAssertion(testStep, operation);
				}
			}
			addBodyPropertyVariantTestCases(restMethod, testSuite, operation);
		}

		if (hasScopes) {
			if (applicationToken) {
				addApplicationTokenTestCases(restMethod, testSuite);
			}
			addScopeVariantTestCases(restMethod, testSuite);
		}
	}

	/**
	 * Add Service Api Convention Test Cases
	 * Orchestrates the 4 Test Cases the RSI "APIs de servicios" convention requires per Method (notes.txt
	 * §2.2.3): CaseOkAllProperties, CaseOkRequiredProperties, one CaseErrorStatusCode{StatusCode} per
	 * documented non-2xx response, and one CaseErrorRequired{Field} per required body property and required
	 * query parameter. bodySchema is resolved once here (RefResolver only resolves a given $ref once per
	 * instance lifetime) and passed down, rather than re-resolved by each case builder
	 * @param restMethod instance of Method to generate Test Cases for
	 * @param testSuite Test Suite to add the Test Cases to
	 * @param operation instance of OpenAPI Operation bound to this Method, or null if unknown
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	@SuppressWarnings("rawtypes")
	private void addServiceApiConventionTestCases(RestMethod restMethod, WsdlTestSuite testSuite, Operation operation, String method) {
		RestRequest defaultRequest = restMethod.getRequestByName(DEFAULT_REQUEST_NAME);
		RefResolver refResolver = new RefResolver(openAPI);
		Schema bodySchema = getRequestBodyJsonSchema(operation, refResolver);

		RestRequest okAllPropertiesRequest = addOkAllPropertiesTestCase(restMethod, defaultRequest, testSuite, operation, refResolver, method);
		addOkRequiredPropertiesTestCase(restMethod, defaultRequest, testSuite, operation, refResolver, bodySchema, method);
		addErrorStatusCodeTestCases(restMethod, okAllPropertiesRequest, testSuite, operation, refResolver, method);
		addErrorRequiredFieldTestCases(restMethod, okAllPropertiesRequest, testSuite, operation, refResolver, bodySchema, method);
	}

	/**
	 * Add {METHOD}_CaseOkAllProperties Test Case
	 * Clones the default Request (its body already includes every property, required and optional), applies
	 * a valid value to every query parameter (required and optional), and asserts the documented success
	 * status code(s) plus the success response schema (unconditionally — not gated by validateSchema)
	 * @param restMethod instance of Method to add the Test Case to
	 * @param defaultRequest the Method's default Request, cloned as the base for this Test Case's Request
	 * @param testSuite Test Suite to add the Test Case to
	 * @param operation instance of OpenAPI Operation, or null if unknown
	 * @param refResolver instance of RefResolver
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 * @return the created Request, reused as the cloning base for the ErrorStatusCode/ErrorRequired Test Cases
	 */
	private RestRequest addOkAllPropertiesTestCase(RestMethod restMethod, RestRequest defaultRequest, WsdlTestSuite testSuite,
			Operation operation, RefResolver refResolver, String method) {
		RestRequest variantRequest = restMethod.cloneRequest(defaultRequest, SERVICE_API_CASE_OK_ALL_PROPERTIES);
		applyQueryParameterValues(variantRequest, operation, refResolver, false);

		String testCaseName = method + "_Case" + SERVICE_API_CASE_OK_ALL_PROPERTIES;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addSuccessAssertions(testStep, operation, refResolver);
		return variantRequest;
	}

	/**
	 * Add {METHOD}_CaseOkRequiredProperties Test Case
	 * Clones the default Request, rebuilds its body with only required properties (via
	 * buildRequiredPropertiesExample) when a JSON body is declared, applies a valid value to only the
	 * required query parameters, and asserts the same success status code(s) + schema as CaseOkAllProperties
	 * @param restMethod instance of Method to add the Test Case to
	 * @param defaultRequest the Method's default Request, cloned as the base for this Test Case's Request
	 * @param testSuite Test Suite to add the Test Case to
	 * @param operation instance of OpenAPI Operation, or null if unknown
	 * @param refResolver instance of RefResolver
	 * @param bodySchema resolved JSON request body schema, or null if the operation has no JSON body
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	@SuppressWarnings("rawtypes")
	private void addOkRequiredPropertiesTestCase(RestMethod restMethod, RestRequest defaultRequest, WsdlTestSuite testSuite,
			Operation operation, RefResolver refResolver, Schema bodySchema, String method) {
		RestRequest variantRequest = restMethod.cloneRequest(defaultRequest, SERVICE_API_CASE_OK_REQUIRED_PROPERTIES);
		if (bodySchema != null) {
			bodyPropertyCounter++;
			currentBodyTokenTypes = new LinkedHashMap<>();
			JSONObject body = buildRequiredPropertiesExample(bodySchema, refResolver, "");
			String exampleStr = mapObjectToJsonString(body);
			exampleStr = stripQuotesAroundNonStringTokens(exampleStr);
			if (exampleStr != null) {
				variantRequest.setRequestContent(exampleStr);
			}
		}
		applyQueryParameterValues(variantRequest, operation, refResolver, true);

		String testCaseName = method + "_Case" + SERVICE_API_CASE_OK_REQUIRED_PROPERTIES;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addSuccessAssertions(testStep, operation, refResolver);
	}

	/**
	 * Build Required Properties Example
	 * Recursive, required-only counterpart to iterateProperties: at each node, includes only the properties
	 * named in THAT node's own "required" list (not iterateProperties's full properties map). A property
	 * that is itself optional at its parent level is omitted entirely, even if it declares its own required
	 * sub-fields, since the doc's target semantics are "only what must be sent", not "every required leaf
	 * reachable through any path". Reuses getPropertyExample for every scalar leaf, so Project Property
	 * tokenization, enums, date-time formatting and configured examples.successful overrides all apply
	 * identically to the CaseOkAllProperties body
	 * @param resolvedSchema already fully-resolved ($ref + composition) schema node to inspect
	 * @param refResolver instance of RefResolver
	 * @param path underscore-joined property path built so far, used to key Project Properties when isInline is false
	 * @return json object with example values for only the required properties found at/under this node
	 */
	@SuppressWarnings("rawtypes")
	private JSONObject buildRequiredPropertiesExample(Schema resolvedSchema, RefResolver refResolver, String path) {
		JSONObject json = new JSONObject();
		if (resolvedSchema.getProperties() == null || resolvedSchema.getRequired() == null) return json;
		for (Object requiredNameObj : resolvedSchema.getRequired()) {
			String propertyName = requiredNameObj.toString();
			Schema property = (Schema) resolvedSchema.getProperties().get(propertyName);
			if (property == null) continue;
			String childPath = path.isEmpty() ? propertyName : path + "_" + propertyName;
			Schema refResolvedProperty = refResolver.resolveSchema(property);
			Schema fullyResolvedProperty = resolveComposedSchema(refResolvedProperty, refResolver);
			try {
				Object example;
				if (fullyResolvedProperty instanceof ObjectSchema) {
					example = buildRequiredPropertiesExample(fullyResolvedProperty, refResolver, childPath);
				} else if (fullyResolvedProperty instanceof ArraySchema) {
					Schema itemsSchema = refResolver.resolveSchema(((ArraySchema) fullyResolvedProperty).getItems());
					Schema fullyResolvedItems = resolveComposedSchema(itemsSchema, refResolver);
					JSONArray array = new JSONArray();
					array.put(fullyResolvedItems instanceof ObjectSchema
							? buildRequiredPropertiesExample(fullyResolvedItems, refResolver, childPath + "_item")
							: getPropertyExample(itemsSchema, refResolver, childPath + "_item"));
					example = array;
				} else {
					example = getPropertyExample(refResolvedProperty, refResolver, childPath);
				}
				json.put(propertyName, example);
			} catch (JSONException e) {
				log.warn("Error buildRequiredPropertiesExample", e);
			}
		}
		return json;
	}

	/**
	 * Apply Query Parameter Values
	 * Sets a value on the given Request (not the shared Method) for every query parameter declared by the
	 * Operation, using QueryParamExampleUtils.validValue to generate a type-aware valid value
	 * @param request the Request to set values on (a per-Test-Case clone, never the shared default Request/Method)
	 * @param operation instance of OpenAPI Operation, or null (no-op)
	 * @param refResolver instance of RefResolver
	 * @param requiredOnly when true, only required query parameters are given a value; when false, every query parameter is
	 */
	private void applyQueryParameterValues(RestRequest request, Operation operation, RefResolver refResolver, boolean requiredOnly) {
		if (operation == null || operation.getParameters() == null) return;
		operation.getParameters().stream()
				.filter(param -> QUERY.equalsIgnoreCase(param.getIn()))
				.filter(param -> !requiredOnly || Boolean.TRUE.equals(param.getRequired()))
				.forEach(param -> {
					Schema<?> resolvedSchema = param.getSchema() != null ? refResolver.resolveSchema(param.getSchema()) : null;
					String value = QueryParamExampleUtils.validValue(resolvedSchema, examples != null ? examples.getSuccessful() : null);
					setRequestParameterValue(request, param.getName(), value);
				});
	}

	/**
	 * Set Request Parameter Value
	 * Sets a per-Request parameter value override, writing directly into the Request's own
	 * RestRequestConfig#getParameters() (a flat name/value map, distinct from the Method-level
	 * RestParamProperty definitions that own each parameter's name/style/required metadata). The higher-level
	 * RestRequest#setPropertyValue(name, value) convenience method updates only an in-memory, non-persisted
	 * cache for this SoapUI version — it does not get serialized by WsdlProject#saveIn — so it cannot be used
	 * to give distinct Test Cases their own distinct query parameter values
	 * @param request the Request to set the value on (a per-Test-Case clone, never the shared default Request/Method)
	 * @param name parameter name (must already be declared on the Method — see setMethodParameters)
	 * @param value value to set; updates the existing entry for name if present, else adds a new one
	 */
	private void setRequestParameterValue(RestRequest request, String name, String value) {
		RestRequestConfig config = request.getConfig();
		StringToStringMapConfig parameters = config.getParameters();
		if (parameters == null) {
			parameters = config.addNewParameters();
		}
		for (StringToStringMapConfig.Entry entry : parameters.getEntryArray()) {
			if (name.equals(entry.getKey())) {
				entry.setValue(value);
				return;
			}
		}
		StringToStringMapConfig.Entry entry = parameters.addNewEntry();
		entry.setKey(name);
		entry.setValue(value);
	}

	/**
	 * Add Success Assertions
	 * Adds a status-code assertion for the operation's documented 2xx code(s) plus a schema assertion for its
	 * success response (skipped when the operation has none, or declares a $select/$exclude query parameter).
	 * Unconditional: not gated by the validateSchema flag, since the serviceApiConvention doc treats these
	 * assertions as inherent to CaseOkAllProperties/CaseOkRequiredProperties, not optional
	 * @param testStep instance of Test Step to attach assertions to
	 * @param operation instance of OpenAPI Operation, or null (no-op)
	 * @param refResolver instance of RefResolver
	 */
	private void addSuccessAssertions(WsdlTestStep testStep, Operation operation, RefResolver refResolver) {
		if (operation == null) return;
		addStatusCodeAssertion(testStep, getSuccessStatusCodesCsv(operation));
		if (!hasPartialResponseQueryParam(operation)) {
			addSchemaValidationAssertionForSchema(testStep, getSuccessJsonResponseSchema(operation, refResolver), refResolver);
		}
	}

	/**
	 * Get Success Status Codes Csv
	 * @param operation instance of OpenAPI Operation
	 * @return comma-separated list of the operation's documented 2xx status codes, or an empty string if none
	 */
	private String getSuccessStatusCodesCsv(Operation operation) {
		if (operation.getResponses() == null) return "";
		return operation.getResponses().keySet().stream()
				.filter(code -> code.startsWith("2"))
				.collect(Collectors.joining(","));
	}

	/**
	 * Add Status Code Assertion
	 * @param testStep instance of Test Step to attach the assertion to
	 * @param codesCsv comma-separated list of status codes the assertion should accept
	 */
	private void addStatusCodeAssertion(WsdlTestStep testStep, String codesCsv) {
		ValidHttpStatusCodesAssertion assertion = (ValidHttpStatusCodesAssertion)
				((RestTestRequestStep) testStep).addAssertion(VALID_HTTP_STATUS_CODES_ASSERTION);
		assertion.setCodes(codesCsv);
	}

	/**
	 * Add Error Status Code Test Cases
	 * Adds one {METHOD}_CaseErrorStatusCode{StatusCode} Test Case per distinct non-2xx, non-"default"
	 * status code documented in the operation's responses, in declaration order
	 * @param restMethod instance of Method to add the Test Cases to
	 * @param baseRequest the CaseOkAllProperties Request, cloned as the base for each variant (a fully
	 *        populated request the tester later adapts to actually trigger the error — see notes.txt §2.2.4/2.2.8)
	 * @param testSuite Test Suite to add the Test Cases to
	 * @param operation instance of OpenAPI Operation, or null (no-op)
	 * @param refResolver instance of RefResolver
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	private void addErrorStatusCodeTestCases(RestMethod restMethod, RestRequest baseRequest, WsdlTestSuite testSuite,
			Operation operation, RefResolver refResolver, String method) {
		if (operation == null || operation.getResponses() == null) return;
		for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
			String code = entry.getKey();
			if (code.equalsIgnoreCase(DEFAULT) || code.startsWith("2")) continue;
			addErrorStatusCodeTestCase(restMethod, baseRequest, testSuite, operation, refResolver, method, code, entry.getValue());
		}
	}

	private void addErrorStatusCodeTestCase(RestMethod restMethod, RestRequest baseRequest, WsdlTestSuite testSuite,
			Operation operation, RefResolver refResolver, String method, String statusCode, ApiResponse response) {
		RestRequest variantRequest = restMethod.cloneRequest(baseRequest, SERVICE_API_CASE_ERROR_STATUS_CODE_PREFIX + statusCode);
		applyMicrocksHeaderForStatus(variantRequest, operation, statusCode);

		String testCaseName = method + "_Case" + SERVICE_API_CASE_ERROR_STATUS_CODE_PREFIX + statusCode;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addStatusCodeAssertion(testStep, statusCode);
		addSchemaValidationAssertionForSchema(testStep, getJsonResponseSchema(response, refResolver), refResolver);
	}

	/**
	 * Add Error Required Field Test Cases
	 * Adds one {METHOD}_CaseErrorRequired{Field} Test Case per required body property (recursively, via the
	 * existing collectRequiredPropertyPaths) and per required query parameter. All variants assert the same
	 * status code + (when documented) response schema, resolved once via resolveRequiredFieldErrorStatusCode
	 * @param restMethod instance of Method to add the Test Cases to
	 * @param baseRequest the CaseOkAllProperties Request, cloned as the base for each variant
	 * @param testSuite Test Suite to add the Test Cases to
	 * @param operation instance of OpenAPI Operation, or null
	 * @param refResolver instance of RefResolver
	 * @param bodySchema resolved JSON request body schema, or null if the operation has no JSON body
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	@SuppressWarnings("rawtypes")
	private void addErrorRequiredFieldTestCases(RestMethod restMethod, RestRequest baseRequest, WsdlTestSuite testSuite,
			Operation operation, RefResolver refResolver, Schema bodySchema, String method) {
		String assertStatusCode = resolveRequiredFieldErrorStatusCode(operation);
		ApiResponse errorResponse = (operation != null && operation.getResponses() != null) ? operation.getResponses().get(assertStatusCode) : null;
		Schema errorSchema = errorResponse != null ? getJsonResponseSchema(errorResponse, refResolver) : null;

		if (bodySchema != null) {
			List<String> requiredBodyPaths = new ArrayList<>();
			collectRequiredPropertyPaths(bodySchema, refResolver, "", requiredBodyPaths);
			for (String path : requiredBodyPaths) {
				addErrorRequiredBodyFieldTestCase(restMethod, baseRequest, testSuite, bodySchema, refResolver, operation, method, path, assertStatusCode, errorSchema);
			}
		}
		if (operation != null) {
			for (Parameter param : collectRequiredQueryParameters(operation)) {
				addErrorRequiredQueryFieldTestCase(restMethod, baseRequest, testSuite, operation, method, param, assertStatusCode, errorSchema, refResolver);
			}
		}
	}

	/**
	 * Resolve Required Field Error Status Code
	 * Picks the status code CaseErrorRequired{Field} Test Cases should assert: the operation's documented
	 * "400" response if present (the conventional validation-error code, matching the old convention's own
	 * WRONG_STATUS_CODE), else the first documented 4xx response in declaration order, else the hardcoded
	 * "400" fallback (with no schema assertion, since no such response is actually documented)
	 * @param operation instance of OpenAPI Operation, or null
	 * @return status code to assert
	 */
	private String resolveRequiredFieldErrorStatusCode(Operation operation) {
		if (operation != null && operation.getResponses() != null) {
			if (operation.getResponses().containsKey(WRONG_STATUS_CODE)) {
				return WRONG_STATUS_CODE;
			}
			for (String code : operation.getResponses().keySet()) {
				if (code.startsWith("4")) {
					return code;
				}
			}
		}
		return WRONG_STATUS_CODE;
	}

	/**
	 * Collect Required Query Parameters
	 * @param operation instance of OpenAPI Operation
	 * @return the operation's query parameters declared as required, in declaration order
	 */
	private List<Parameter> collectRequiredQueryParameters(Operation operation) {
		if (operation.getParameters() == null) return Collections.emptyList();
		return operation.getParameters().stream()
				.filter(param -> QUERY.equalsIgnoreCase(param.getIn()) && Boolean.TRUE.equals(param.getRequired()))
				.collect(Collectors.toList());
	}

	@SuppressWarnings("rawtypes")
	private void addErrorRequiredBodyFieldTestCase(RestMethod restMethod, RestRequest baseRequest, WsdlTestSuite testSuite,
			Schema bodySchema, RefResolver refResolver, Operation operation, String method, String omitPath,
			String assertStatusCode, Schema errorSchema) {
		RestRequest variantRequest = restMethod.cloneRequest(baseRequest, SERVICE_API_CASE_ERROR_REQUIRED_PREFIX + omitPath);

		bodyPropertyCounter++;
		currentBodyTokenTypes = new LinkedHashMap<>();
		bodyVariantOmitPath = omitPath;
		try {
			JSONObject body = iterateProperties(bodySchema.getProperties(), refResolver, "");
			String exampleStr = mapObjectToJsonString(body);
			exampleStr = stripQuotesAroundNonStringTokens(exampleStr);
			if (exampleStr != null) {
				variantRequest.setRequestContent(exampleStr);
			}
		} finally {
			bodyVariantOmitPath = null;
		}
		applyMicrocksHeaderForStatus(variantRequest, operation, assertStatusCode);

		String testCaseName = method + "_Case" + SERVICE_API_CASE_ERROR_REQUIRED_PREFIX + toCaseFieldName(omitPath);
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addStatusCodeAssertion(testStep, assertStatusCode);
		addSchemaValidationAssertionForSchema(testStep, errorSchema, refResolver);
	}

	@SuppressWarnings("rawtypes")
	private void addErrorRequiredQueryFieldTestCase(RestMethod restMethod, RestRequest baseRequest, WsdlTestSuite testSuite,
			Operation operation, String method, Parameter param, String assertStatusCode, Schema errorSchema, RefResolver refResolver) {
		RestRequest variantRequest = restMethod.cloneRequest(baseRequest, SERVICE_API_CASE_ERROR_REQUIRED_PREFIX + param.getName());
		setRequestParameterValue(variantRequest, param.getName(), "");
		applyMicrocksHeaderForStatus(variantRequest, operation, assertStatusCode);

		String testCaseName = method + "_Case" + SERVICE_API_CASE_ERROR_REQUIRED_PREFIX + toCaseFieldName(param.getName());
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addStatusCodeAssertion(testStep, assertStatusCode);
		addSchemaValidationAssertionForSchema(testStep, errorSchema, refResolver);
	}

	/**
	 * To Case Field Name
	 * PascalCases each underscore-separated segment of a body property dotted-path or a plain query
	 * parameter name, concatenating with no separator, and drops any "item" segment (the array-recursion
	 * marker collectRequiredPropertyPaths injects) — e.g. "orders_item_sku" -&gt; "OrdersSku", "page_size" -&gt; "PageSize"
	 * @param dottedOrParamName underscore-joined body property path, or a plain (zero-underscore) query parameter name
	 * @return PascalCase field name for use in a {METHOD}_CaseErrorRequired{Field} Test Case name
	 */
	private String toCaseFieldName(String dottedOrParamName) {
		StringBuilder result = new StringBuilder();
		for (String segment : dottedOrParamName.split("_")) {
			if (segment.isEmpty() || SERVICE_API_ARRAY_ITEM_SEGMENT.equalsIgnoreCase(segment)) continue;
			result.append(Character.toUpperCase(segment.charAt(0))).append(segment.substring(1));
		}
		return result.toString();
	}

	/**
	 * Add Schema Validation Assertion
	 * Looks up the JSON Schema of the operation's first 2xx JSON response and, if found, attaches a Script
	 * Assertion to the given test step that parses the response body and validates it against that schema
	 * Skipped (with a warning) when the operation has no 2xx response with a JSON body to validate against
	 * @param testStep instance of Test Step to attach the assertion to
	 * @param operation instance of OpenAPI Operation the test step was built from, or null if unknown
	 */
	private void addSchemaValidationAssertion(WsdlTestStep testStep, Operation operation) {
		if (operation == null) return;
		RefResolver refResolver = new RefResolver(openAPI);
		Schema<?> responseSchema = getSuccessJsonResponseSchema(operation, refResolver);
		if (responseSchema == null) {
			log.warn("Test step {} has no 2xx JSON response schema to validate; validateSchema assertion skipped", testStep.getName());
			return;
		}
		addSchemaValidationAssertionForSchema(testStep, responseSchema, refResolver);
	}

	/**
	 * Add Schema Validation Assertion For Schema
	 * Builds+attaches a Script Assertion validating the response body against the given, already-resolved
	 * schema. Schema-agnostic: reused for both the operation's success schema and any documented error
	 * response's own schema (see the serviceApiConvention case builders)
	 * @param testStep instance of Test Step to attach the assertion to
	 * @param responseSchema already-resolved Schema to validate the response body against, or null (no-op)
	 * @param refResolver instance of RefResolver
	 */
	@SuppressWarnings("rawtypes")
	private void addSchemaValidationAssertionForSchema(WsdlTestStep testStep, Schema responseSchema, RefResolver refResolver) {
		if (responseSchema == null) return;
		String script = buildSchemaValidationScript(buildJsonSchemaDefinition(responseSchema, refResolver, new HashSet<>()));
		if (script == null) return;
		GroovyScriptAssertion assertion = (GroovyScriptAssertion)
				((RestTestRequestStep) testStep).addAssertion(SCRIPT_ASSERTION);
		assertion.setScriptText(script);
	}

	/**
	 * Get Success Json Response Schema
	 * Looks for the first 2xx response (in spec declaration order) that declares a JSON media type, and returns
	 * its (ref-resolved) Schema
	 * @param operation instance of OpenAPI Operation
	 * @param refResolver instance of RefResolver
	 * @return resolved Schema, or null if no 2xx response declares a JSON body
	 */
	@SuppressWarnings("rawtypes")
	private Schema getSuccessJsonResponseSchema(Operation operation, RefResolver refResolver) {
		if (operation.getResponses() == null) return null;
		for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
			if (!entry.getKey().startsWith("2")) continue;
			Schema schema = getJsonResponseSchema(entry.getValue(), refResolver);
			if (schema != null) return schema;
		}
		return null;
	}

	/**
	 * Get Json Response Schema
	 * Looks for the first JSON media type declared on the given response, and returns its (ref-resolved) Schema.
	 * The per-response half of getSuccessJsonResponseSchema's search, reusable for any single response
	 * (e.g. a specific documented error status), not just "the first 2xx"
	 * @param response instance of OpenAPI ApiResponse, or null
	 * @param refResolver instance of RefResolver
	 * @return resolved Schema, or null if the response declares no JSON body
	 */
	@SuppressWarnings("rawtypes")
	private Schema getJsonResponseSchema(ApiResponse response, RefResolver refResolver) {
		if (response == null) return null;
		Content content = response.getContent();
		if (content == null) return null;
		for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
			Schema schema = mediaTypeEntry.getValue().getSchema();
			if (mediaTypeEntry.getKey().toLowerCase().contains(JSON) && schema != null) {
				return refResolver.resolveSchema(schema);
			}
		}
		return null;
	}

	/**
	 * Build Json Schema Definition
	 * Converts an OpenAPI Schema tree into a plain JSON-Schema-shaped Map (type/properties/required/items/enum/
	 * nullable, plus allOf merged and oneOf/anyOf represented as an "anyOf" list)
	 * Independent from generateOneOfAnyOf/example-generation logic, since this builds a real schema definition
	 * for validation rather than a single example value
	 * $ref is resolved directly here (not via RefResolver): RefResolver only resolves a given $ref once for its
	 * whole lifetime (to protect its example-generation walk from infinite recursion on cyclic schemas), which
	 * would silently under-resolve a schema that is legitimately referenced more than once in the same response
	 * (e.g. the same Address schema used for both billingAddress and shippingAddress). refsInPath instead tracks
	 * only the refs currently being expanded along the current branch, so repeated-but-not-cyclic refs are fully
	 * resolved every time, while a true cycle (a schema that references itself, directly or indirectly) is still
	 * safely bounded to an open/permissive object instead of overflowing the stack
	 * @param schema to convert
	 * @param refResolver instance of RefResolver, still used to resolve allOf members via the shared mergeAllOf helper
	 * @param refsInPath $ref names currently being expanded along this branch, to detect cycles
	 * @return Map representing the equivalent JSON Schema definition, or null if schema is null
	 */
	@SuppressWarnings("rawtypes")
	private Object buildJsonSchemaDefinition(Schema schema, RefResolver refResolver, Set<String> refsInPath) {
		if (schema == null) return null;
		String ref = schema.get$ref();
		if (ref != null) {
			if (refsInPath.contains(ref)) {
				return new LinkedHashMap<>();
			}
			Schema target = resolveComponentSchema(ref);
			if (target == null) return new LinkedHashMap<>();
			Set<String> nextRefsInPath = new HashSet<>(refsInPath);
			nextRefsInPath.add(ref);
			return buildJsonSchemaDefinition(target, refResolver, nextRefsInPath);
		}
		boolean nullable = Boolean.TRUE.equals(schema.getNullable());
		if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
			return applyNullable(buildJsonSchemaDefinition(mergeAllOf(schema.getAllOf(), refResolver), refResolver, refsInPath), nullable);
		}
		List<Schema> alternatives = (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) ? schema.getOneOf() : schema.getAnyOf();
		if (alternatives != null && !alternatives.isEmpty()) {
			Map<String, Object> definition = new LinkedHashMap<>();
			List<Object> anyOf = new ArrayList<>();
			for (Schema alternative : alternatives) {
				anyOf.add(buildJsonSchemaDefinition(alternative, refResolver, refsInPath));
			}
			definition.put("anyOf", anyOf);
			return applyNullable(definition, nullable);
		}
		Map<String, Object> definition = new LinkedHashMap<>();
		if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
			definition.put("enum", schema.getEnum());
		}
		if (schema instanceof ArraySchema) {
			definition.put("type", "array");
			definition.put("items", buildJsonSchemaDefinition(((ArraySchema) schema).getItems(), refResolver, refsInPath));
		} else if (schema instanceof IntegerSchema) {
			definition.put("type", "integer");
		} else if (schema instanceof NumberSchema) {
			definition.put("type", "number");
		} else if (schema instanceof BooleanSchema) {
			definition.put("type", "boolean");
		} else if (schema instanceof StringSchema || schema instanceof DateSchema) {
			definition.put("type", "string");
		} else {
			definition.put("type", "object");
			Map<String, Object> properties = new LinkedHashMap<>();
			if (schema.getProperties() != null) {
				Map<String, Schema> schemaProperties = schema.getProperties();
				schemaProperties.forEach((propertyName, propertySchema) ->
						properties.put(propertyName, buildJsonSchemaDefinition(propertySchema, refResolver, refsInPath)));
			}
			definition.put("properties", properties);
			if (schema.getRequired() != null) {
				definition.put("required", schema.getRequired());
			}
			if (Boolean.FALSE.equals(schema.getAdditionalProperties())) {
				definition.put("additionalProperties", false);
			}
		}
		addCommonConstraints(schema, definition);
		return applyNullable(definition, nullable);
	}

	/**
	 * Add Common Constraints
	 * Copies the JSON Schema keywords that matter most for catching real validation issues (pattern, format,
	 * string length, numeric bounds, array size/uniqueness) from the OpenAPI Schema into the definition Map,
	 * when present. Keywords that don't apply to the schema's type are simply absent in the source schema, so
	 * this can be called unconditionally for every leaf definition
	 * @param schema source OpenAPI Schema
	 * @param definition JSON-Schema-shaped Map to enrich in place
	 */
	@SuppressWarnings("rawtypes")
	private void addCommonConstraints(Schema schema, Map<String, Object> definition) {
		if (schema.getPattern() != null) {
			definition.put("pattern", schema.getPattern());
		}
		if (schema.getFormat() != null) {
			definition.put("format", schema.getFormat());
		}
		if (schema.getMinLength() != null) {
			definition.put("minLength", schema.getMinLength());
		}
		if (schema.getMaxLength() != null) {
			definition.put("maxLength", schema.getMaxLength());
		}
		if (schema.getMinimum() != null) {
			definition.put("minimum", schema.getMinimum());
		}
		if (schema.getMaximum() != null) {
			definition.put("maximum", schema.getMaximum());
		}
		if (schema.getMinItems() != null) {
			definition.put("minItems", schema.getMinItems());
		}
		if (schema.getMaxItems() != null) {
			definition.put("maxItems", schema.getMaxItems());
		}
		if (Boolean.TRUE.equals(schema.getUniqueItems())) {
			definition.put("uniqueItems", true);
		}
	}

	/**
	 * Apply Nullable
	 * Marks a schema definition Map as nullable, so the validator accepts a null instance at that node
	 * @param definition Map produced by buildJsonSchemaDefinition, or any other value (left untouched if not a Map)
	 * @param nullable whether the original OpenAPI schema declared nullable: true
	 * @return the same definition, with "nullable": true added when applicable
	 */
	@SuppressWarnings("unchecked")
	private Object applyNullable(Object definition, boolean nullable) {
		if (nullable && definition instanceof Map) {
			((Map<String, Object>) definition).put("nullable", true);
		}
		return definition;
	}

	/**
	 * Resolve Component Schema
	 * Looks up a $ref directly in components/schemas, independent of RefResolver's resolve-once cache
	 * @param ref $ref string, e.g. "#/components/schemas/Address"
	 * @return the referenced Schema, or null if components/schemas or the key itself is not found
	 */
	@SuppressWarnings("rawtypes")
	private Schema resolveComponentSchema(String ref) {
		if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) return null;
		String[] refParts = ref.split("/");
		return openAPI.getComponents().getSchemas().get(refParts[refParts.length - 1]);
	}

	/**
	 * Build Schema Validation Script
	 * Builds a self-contained Groovy script (no external libraries required) that parses the response body as
	 * JSON and recursively validates it against the given JSON Schema definition, failing the assertion with a
	 * descriptive message when the body is not JSON or does not match the schema
	 * When schemaIsInline is false (default), the schema JSON is stored as a SoapUI Project Property instead of
	 * being embedded in the script, and the script reads it at runtime via context.expand(...)
	 * @param jsonSchemaDefinition Map produced by buildJsonSchemaDefinition
	 * @return Groovy script source, or null if the schema definition could not be serialized
	 */
	private String buildSchemaValidationScript(Object jsonSchemaDefinition) {
		String schemaJson = mapObjectToJsonString(jsonSchemaDefinition, schemaPrettyPrint);
		if (schemaJson == null) return null;
		String schemaSource;
		if (schemaIsInline) {
			// Escape backslashes and single quotes so the JSON text survives Groovy's own string-literal escaping
			// unchanged (matters for enum values containing backslashes, e.g. Windows-style paths)
			String safeSchemaJson = schemaJson.replace("\\", "\\\\").replace("'", "\\'");
			schemaSource = "def schema = new groovy.json.JsonSlurper().parseText('''" + safeSchemaJson + "''')";
		} else {
			schemaPropertyCounter++;
			String key = "schema" + schemaPropertyCounter;
			project.setPropertyValue(key, schemaJson);
			schemaSource = "def schema = new groovy.json.JsonSlurper().parseText(context.expand('${#Project#" + key + "}'))";
		}
		return String.join("\n",
				"def json = null",
				"try {",
				"    json = new groovy.json.JsonSlurper().parseText(messageExchange.responseContent)",
				"} catch (Exception e) {",
				"    assert false : \"A JSON response was expected\"",
				"}",
				"",
				schemaSource,
				"",
				"def errors = []",
				"def validateNode",
				"validateNode = { instance, sch, path ->",
				"    if (sch == null) { return }",
				"    if (instance == null) {",
				"        if (!(sch.nullable == true)) { errors << (path + \": expected non-null value\") }",
				"        return",
				"    }",
				"    if (sch.enum != null) {",
				"        if (!sch.enum.contains(instance)) {",
				"            errors << (path + \": value is not one of the allowed enum values\")",
				"        }",
				"        return",
				"    }",
				"    if (sch.anyOf != null) {",
				"        def matched = false",
				"        for (candidate in sch.anyOf) {",
				"            def before = errors.size()",
				"            validateNode(instance, candidate, path)",
				"            if (errors.size() == before) { matched = true; break }",
				"            while (errors.size() > before) { errors.remove(errors.size() - 1) }",
				"        }",
				"        if (!matched) { errors << (path + \": does not match any of the expected schemas\") }",
				"        return",
				"    }",
				"    switch (sch.type) {",
				"        case 'object':",
				"            if (!(instance instanceof Map)) { errors << (path + \": expected object\"); return }",
				"            (sch.required ?: []).each { req -> if (!instance.containsKey(req)) { errors << (path + \".\" + req + \": required property missing\") } }",
				"            (sch.properties ?: [:]).each { propName, propSchema -> if (instance.containsKey(propName)) { validateNode(instance[propName], propSchema, path + \".\" + propName) } }",
				"            if (sch.additionalProperties == false) {",
				"                def allowedProps = (sch.properties ?: [:]).keySet()",
				"                instance.keySet().each { key -> if (!allowedProps.contains(key)) { errors << (path + \".\" + key + \": additional property not allowed\") } }",
				"            }",
				"            break",
				"        case 'array':",
				"            if (!(instance instanceof List)) { errors << (path + \": expected array\"); return }",
				"            if (sch.minItems != null && instance.size() < sch.minItems) { errors << (path + \": fewer than minItems \" + sch.minItems) }",
				"            if (sch.maxItems != null && instance.size() > sch.maxItems) { errors << (path + \": more than maxItems \" + sch.maxItems) }",
				"            if (sch.uniqueItems == true && instance.size() != (instance as Set).size()) { errors << (path + \": items are not unique\") }",
				"            instance.eachWithIndex { item, idx -> validateNode(item, sch.items, path + \"[\" + idx + \"]\") }",
				"            break",
				"        case 'string':",
				"            if (!(instance instanceof String)) { errors << (path + \": expected string\"); return }",
				"            if (sch.pattern && !(instance =~ sch.pattern).find()) { errors << (path + \": does not match pattern '\" + sch.pattern + \"'\") }",
				"            if (sch.format == 'email' && !(instance ==~ /^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$/)) { errors << (path + \": does not match format 'email'\") }",
				"            if (sch.format == 'uuid' && !(instance ==~ /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/)) { errors << (path + \": does not match format 'uuid'\") }",
				"            if (sch.format == 'date' && !(instance ==~ /^\\d{4}-\\d{2}-\\d{2}$/)) { errors << (path + \": does not match format 'date'\") }",
				"            if (sch.format == 'date-time' && !(instance ==~ /^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})?$/)) { errors << (path + \": does not match format 'date-time'\") }",
				"            if (sch.minLength != null && instance.length() < sch.minLength) { errors << (path + \": shorter than minLength \" + sch.minLength) }",
				"            if (sch.maxLength != null && instance.length() > sch.maxLength) { errors << (path + \": longer than maxLength \" + sch.maxLength) }",
				"            break",
				"        case 'integer':",
				"            if (!(instance instanceof Integer || instance instanceof Long || instance instanceof BigInteger || (instance instanceof BigDecimal && instance.scale() <= 0))) { errors << (path + \": expected integer\"); return }",
				"            if (sch.minimum != null && new BigDecimal(instance.toString()).compareTo(new BigDecimal(sch.minimum.toString())) < 0) { errors << (path + \": below minimum \" + sch.minimum) }",
				"            if (sch.maximum != null && new BigDecimal(instance.toString()).compareTo(new BigDecimal(sch.maximum.toString())) > 0) { errors << (path + \": above maximum \" + sch.maximum) }",
				"            break",
				"        case 'number':",
				"            if (!(instance instanceof Number)) { errors << (path + \": expected number\"); return }",
				"            if (sch.minimum != null && new BigDecimal(instance.toString()).compareTo(new BigDecimal(sch.minimum.toString())) < 0) { errors << (path + \": below minimum \" + sch.minimum) }",
				"            if (sch.maximum != null && new BigDecimal(instance.toString()).compareTo(new BigDecimal(sch.maximum.toString())) > 0) { errors << (path + \": above maximum \" + sch.maximum) }",
				"            break",
				"        case 'boolean':",
				"            if (!(instance instanceof Boolean)) { errors << (path + \": expected boolean\") }",
				"            break",
				"        default:",
				"            break",
				"    }",
				"}",
				"",
				"validateNode(json, schema, '$')",
				"",
				"assert errors.isEmpty() : \"Response body does not match the expected JSON Schema: \" + errors.join('; ')"
		);
	}

	/**
	 * Build a stable key identifying a Method within the SoapUI Project, independent of object identity,
	 * to bridge OpenAPI Operation data between setMethodsRequests and setTestCases
	 * @param path Resource path
	 * @param httpMethod HTTP method name
	 * @return composite key
	 */
	private String methodKey(String path, String httpMethod) {
		return path + "#" + httpMethod;
	}

	/**
	 * Has Partial Response Query Param
	 * True when the Operation declares a $select or $exclude query parameter (OData-style partial response
	 * selection), in which case a schema-validation assertion is skipped, since a partial response will not
	 * match the operation's full JSON Schema.
	 * @param operation instance of OpenAPI Operation, or null
	 * @return true if a $select/$exclude query parameter is declared
	 */
	private boolean hasPartialResponseQueryParam(Operation operation) {
		if (operation == null || operation.getParameters() == null) return false;
		return operation.getParameters().stream()
				.filter(param -> QUERY.equalsIgnoreCase(param.getIn()))
				.anyMatch(param -> SELECT_QUERY_PARAM.equalsIgnoreCase(param.getName()) || EXCLUDE_QUERY_PARAM.equalsIgnoreCase(param.getName()));
	}

	/**
	 * Add Body Property Variant Test Cases
	 * Targets the Method's JSON request
	 * body rather than query parameters. Performs a preorder, depth-first walk of the body schema to find:
	 *  - every required property (recursing into nested required objects/arrays of objects), each becoming a
	 *    400 Test Case that omits just that one property from an otherwise-normal body
	 *  - every scalar/object/array property, each becoming a 400 Test Case that substitutes a type-aware
	 *    invalid value for just that one property, leaving every other property with its normal value
	 * No-op when the Method has no JSON request body
	 * @param restMethod instance of Method to generate variants for
	 * @param testSuite Test Suite to add the variant Test Cases to
	 * @param operation instance of OpenAPI Operation bound to this Method, or null if unknown
	 */
	/**
	 * Get Request Body Json Schema
	 * Finds the operation's JSON request body media type (if any) and returns its fully-resolved
	 * ($ref + oneOf/anyOf/allOf composition) Schema, provided it declares at least one property
	 * @param operation instance of OpenAPI Operation, or null
	 * @param refResolver instance of RefResolver
	 * @return resolved Schema, or null if the operation has no JSON request body with properties
	 */
	@SuppressWarnings("rawtypes")
	private Schema getRequestBodyJsonSchema(Operation operation, RefResolver refResolver) {
		if (operation == null || operation.getRequestBody() == null) return null;
		Content content = operation.getRequestBody().getContent();
		if (content == null) return null;
		MediaType jsonMediaType = null;
		for (Map.Entry<String, MediaType> entry : content.entrySet()) {
			if (entry.getKey().toLowerCase().contains(JSON)) {
				jsonMediaType = entry.getValue();
				break;
			}
		}
		if (jsonMediaType == null || jsonMediaType.getSchema() == null) return null;
		Schema bodySchema = resolveComposedSchema(refResolver.resolveSchema(jsonMediaType.getSchema()), refResolver);
		return bodySchema.getProperties() == null ? null : bodySchema;
	}

	@SuppressWarnings("rawtypes")
	private void addBodyPropertyVariantTestCases(RestMethod restMethod, WsdlTestSuite testSuite, Operation operation) {
		RefResolver refResolver = new RefResolver(openAPI);
		Schema bodySchema = getRequestBodyJsonSchema(operation, refResolver);
		if (bodySchema == null) return;

		List<String> requiredPaths = new ArrayList<>();
		collectRequiredPropertyPaths(bodySchema, refResolver, "", requiredPaths);
		if (minimalEndpoints && requiredPaths.size() > 1) {
			requiredPaths = requiredPaths.subList(0, 1);
		}

		List<BodyPropertyCandidate> wrongCandidates = new ArrayList<>();
		if (!minimalEndpoints) {
			collectBodyPropertyCandidates(bodySchema, refResolver, "", wrongCandidates);
		}

		RestRequest defaultRequest = restMethod.getRequestByName(DEFAULT_REQUEST_NAME);
		for (String requiredPath : requiredPaths) {
			addBodyPropertyVariantTestCase(restMethod, defaultRequest, testSuite, bodySchema, refResolver, operation, requiredPath, null);
		}
		for (BodyPropertyCandidate wrongCandidate : wrongCandidates) {
			addBodyPropertyVariantTestCase(restMethod, defaultRequest, testSuite, bodySchema, refResolver, operation, null, wrongCandidate);
		}
	}

	/**
	 * Collect Required Property Paths
	 * Preorder, depth-first walk of the schema tree: at each node, first collects every property named in that
	 * node's own "required" list (in declared order), then recurses into every object/array-of-object property
	 * (whether required or not) in properties-map order, so nested required properties are found after every
	 * required property of their ancestors
	 * @param schema schema node to inspect
	 * @param refResolver instance of RefResolver
	 * @param path underscore-joined property path built so far
	 * @param out accumulator for dotted paths of required properties found, in discovery order
	 */
	@SuppressWarnings("rawtypes")
	private void collectRequiredPropertyPaths(Schema schema, RefResolver refResolver, String path, List<String> out) {
		if (schema == null) return;
		schema = resolveComposedSchema(refResolver.resolveSchema(schema), refResolver);
		if (schema.getRequired() != null) {
			for (Object requiredName : schema.getRequired()) {
				out.add(path.isEmpty() ? requiredName.toString() : path + "_" + requiredName);
			}
		}
		Map<String, Schema> properties = schema.getProperties();
		if (properties == null) return;
		properties.forEach((name, property) -> {
			String childPath = path.isEmpty() ? name : path + "_" + name;
			Schema resolvedProperty = resolveComposedSchema(refResolver.resolveSchema(property), refResolver);
			if (resolvedProperty instanceof ObjectSchema) {
				collectRequiredPropertyPaths(resolvedProperty, refResolver, childPath, out);
			} else if (resolvedProperty instanceof ArraySchema) {
				collectRequiredPropertyPaths(((ArraySchema) resolvedProperty).getItems(), refResolver, childPath + "_item", out);
			}
		});
	}

	/**
	 * Collect Body Property Candidates
	 * Preorder, depth-first walk of the schema tree: every property (of any type — object, array, or scalar)
	 * becomes a "wrong value" candidate, and every object/array-of-object property is also recursed into so its
	 * own children become independent candidates too
	 * @param schema schema node to inspect
	 * @param refResolver instance of RefResolver
	 * @param path underscore-joined property path built so far
	 * @param out accumulator for candidates found, in discovery order
	 */
	@SuppressWarnings("rawtypes")
	private void collectBodyPropertyCandidates(Schema schema, RefResolver refResolver, String path, List<BodyPropertyCandidate> out) {
		if (schema == null) return;
		Schema resolved = resolveComposedSchema(refResolver.resolveSchema(schema), refResolver);
		Map<String, Schema> properties = resolved.getProperties();
		if (properties == null) return;
		properties.forEach((name, property) -> {
			String childPath = path.isEmpty() ? name : path + "_" + name;
			Schema resolvedProperty = resolveComposedSchema(refResolver.resolveSchema(property), refResolver);
			out.add(new BodyPropertyCandidate(childPath, resolvedProperty));
			if (resolvedProperty instanceof ObjectSchema) {
				collectBodyPropertyCandidates(resolvedProperty, refResolver, childPath, out);
			} else if (resolvedProperty instanceof ArraySchema) {
				Schema items = refResolver.resolveSchema(((ArraySchema) resolvedProperty).getItems());
				collectBodyPropertyCandidates(items, refResolver, childPath + "_item", out);
			}
		});
	}

	/**
	 * Add Body Property Variant Test Case
	 * Clones the default Request, rebuilds its JSON body with exactly one property either omitted
	 * (omitPath, the "missing required property" variant) or substituted with an invalid value
	 * (wrongCandidate, the "wrong value" variant), and adds a Test Case asserting HTTP status 400.
	 * When microcksHeaders is true, the X-Microcks-Response-Name header is recomputed for status 400 specifically.
	 * @param restMethod instance of Method to add the variant Request to
	 * @param defaultRequest the Method's default Request, cloned as the base for the variant Request
	 * @param testSuite Test Suite to add the variant Test Case to
	 * @param bodySchema resolved JSON request body schema
	 * @param refResolver instance of RefResolver
	 * @param operation instance of OpenAPI Operation, used to resolve the status-400 Microcks example name
	 * @param omitPath dotted path of the required property to omit, or null for a "wrong value" variant
	 * @param wrongCandidate property to substitute with an invalid value, or null for a "missing" variant
	 */
	@SuppressWarnings("rawtypes")
	private void addBodyPropertyVariantTestCase(RestMethod restMethod, RestRequest defaultRequest, WsdlTestSuite testSuite,
			Schema bodySchema, RefResolver refResolver, Operation operation, String omitPath, BodyPropertyCandidate wrongCandidate) {
		String targetPath = omitPath != null ? omitPath : wrongCandidate.path;
		String requestName = (omitPath != null ? MISSING_BODY_PROPERTY_VARIANT_PREFIX : WRONG_BODY_PROPERTY_VARIANT_PREFIX) + targetPath;
		RestRequest variantRequest = restMethod.cloneRequest(defaultRequest, requestName);

		bodyPropertyCounter++;
		currentBodyTokenTypes = new LinkedHashMap<>();
		bodyVariantOmitPath = omitPath;
		bodyVariantWrongCandidate = wrongCandidate;
		try {
			JSONObject body = iterateProperties(bodySchema.getProperties(), refResolver, "");
			String exampleStr = mapObjectToJsonString(body);
			exampleStr = stripQuotesAroundNonStringTokens(exampleStr);
			if (exampleStr != null) {
				variantRequest.setRequestContent(exampleStr);
			}
		} finally {
			bodyVariantOmitPath = null;
			bodyVariantWrongCandidate = null;
		}
		applyMicrocksHeaderForStatus(variantRequest, operation, WRONG_STATUS_CODE);

		String testCaseName = requestName + "_" + CASE_SUFFIX;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		ValidHttpStatusCodesAssertion assertion = (ValidHttpStatusCodesAssertion)
				((RestTestRequestStep) testStep).addAssertion(VALID_HTTP_STATUS_CODES_ASSERTION);
		assertion.setCodes(WRONG_STATUS_CODE);
	}

	/**
	 * Add Scope Variant Test Cases
	 * Adds one Test Case per profile among the first numberOfScopes configured OAuth2 Profiles (in the
	 * order they were added to the SoapUI Project, floored to 1 — see numberOfScopes), each wired to that
	 * specific profile via its own Credentials config, independent of the default Request (which always
	 * uses only the first profile, see setRequestAuthProfile). The first profile is skipped here: the
	 * default Request already uses it, so an extra Test Case for that same profile would be a pure
	 * duplicate. No-op when there are no configured OAuth2 Profiles.
	 * @param restMethod instance of Method to generate variants for
	 * @param testSuite Test Suite to add the variant Test Cases to
	 */
	private void addScopeVariantTestCases(RestMethod restMethod, WsdlTestSuite testSuite) {
		List<OAuth2Profile> oAuth2ProfileList = project.getOAuth2ProfileContainer().getOAuth2ProfileList();
		if (oAuth2ProfileList == null || oAuth2ProfileList.isEmpty()) return;
		int desiredScopeCount = Math.max(numberOfScopes, 1);
		if (desiredScopeCount < oAuth2ProfileList.size()) {
			oAuth2ProfileList = oAuth2ProfileList.subList(0, desiredScopeCount);
		}
		if (oAuth2ProfileList.size() <= 1) return;
		RestRequest defaultRequest = restMethod.getRequestByName(DEFAULT_REQUEST_NAME);
		oAuth2ProfileList.subList(1, oAuth2ProfileList.size())
				.forEach(oAuth2Profile -> addScopeVariantTestCase(restMethod, defaultRequest, testSuite, oAuth2Profile));
	}

	/**
	 * Add Scope Variant Test Case
	 * Clone the default Request (carrying over its endpoint, media type, body and headers), then replace its
	 * Credentials with a brand-new one selecting the given OAuth2 Profile. Never mutates the clone's inherited
	 * Credentials object in place, to avoid any risk of it being shared with the default Request's own config.
	 * @param restMethod instance of Method to add the variant Request to
	 * @param defaultRequest the Method's default Request, cloned as the base for the variant Request
	 * @param testSuite Test Suite to add the variant Test Case to
	 * @param oAuth2Profile the SoapUI-native OAuth2 Profile this variant Test Case should be wired to
	 */
	private void addScopeVariantTestCase(RestMethod restMethod, RestRequest defaultRequest, WsdlTestSuite testSuite, OAuth2Profile oAuth2Profile) {
		String requestName = HAS_SCOPES_VARIANT_PREFIX + oAuth2Profile.getName();
		RestRequest variantRequest = restMethod.cloneRequest(defaultRequest, requestName);

		CredentialsConfig credentialsConfig = CredentialsConfig.Factory.newInstance();
		credentialsConfig.setSelectedAuthProfile(oAuth2Profile.getName());
		credentialsConfig.setAuthType(AuthType.O_AUTH_2_0);
		variantRequest.getConfig().setCredentials(credentialsConfig);

		String testCaseName = requestName + "_" + CASE_SUFFIX;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		testCase.addTestStep(stepConfig);
	}

	/**
	 * Add Application Token Test Cases
	 * Only called when hasScopes is also true. For every configured OAuth2 Profile whose grant type is
	 * CLIENT_CREDENTIALS (an application-only token, with no user), add an additional Test Case wired to
	 * that specific profile, separate from the hasScopes scope variant Test Cases. No-op when there are no
	 * CLIENT_CREDENTIALS-grant profiles configured.
	 * @param restMethod instance of Method to generate variants for
	 * @param testSuite Test Suite to add the variant Test Cases to
	 */
	private void addApplicationTokenTestCases(RestMethod restMethod, WsdlTestSuite testSuite) {
		List<OAuth2Profile> oAuth2ProfileList = project.getOAuth2ProfileContainer().getOAuth2ProfileList();
		if (oAuth2ProfileList == null || oAuth2ProfileList.isEmpty()) return;
		RestRequest defaultRequest = restMethod.getRequestByName(DEFAULT_REQUEST_NAME);
		oAuth2ProfileList.stream()
				.filter(oAuth2Profile -> OAuth2Flow.CLIENT_CREDENTIALS_GRANT.equals(oAuth2Profile.getOAuth2Flow()))
				.forEach(oAuth2Profile -> addApplicationTokenTestCase(restMethod, defaultRequest, testSuite, oAuth2Profile));
	}

	/**
	 * Add Application Token Test Case
	 * Clone the default Request (carrying over its endpoint, media type, body and headers), then replace its
	 * Credentials with a brand-new one selecting the given CLIENT_CREDENTIALS-grant OAuth2 Profile. Never
	 * mutates the clone's inherited Credentials object in place, to avoid any risk of it being shared with
	 * the default Request's own config.
	 * @param restMethod instance of Method to add the variant Request to
	 * @param defaultRequest the Method's default Request, cloned as the base for the variant Request
	 * @param testSuite Test Suite to add the variant Test Case to
	 * @param oAuth2Profile the SoapUI-native, CLIENT_CREDENTIALS-grant OAuth2 Profile this variant Test Case should be wired to
	 */
	private void addApplicationTokenTestCase(RestMethod restMethod, RestRequest defaultRequest, WsdlTestSuite testSuite, OAuth2Profile oAuth2Profile) {
		String requestName = APPLICATION_TOKEN_VARIANT_PREFIX + oAuth2Profile.getName();
		RestRequest variantRequest = restMethod.cloneRequest(defaultRequest, requestName);

		CredentialsConfig credentialsConfig = CredentialsConfig.Factory.newInstance();
		credentialsConfig.setSelectedAuthProfile(oAuth2Profile.getName());
		credentialsConfig.setAuthType(AuthType.O_AUTH_2_0);
		variantRequest.getConfig().setCredentials(credentialsConfig);

		String testCaseName = requestName + "_" + CASE_SUFFIX;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		testCase.addTestStep(stepConfig);
	}

	/**
	 * Get Microcks Example Name For Status
	 * Looks up the named example for the given literal status code, falling back to the "default" response.
	 * @param operation instance of OpenAPI Operation
	 * @param statusCode literal status code to look up (e.g. "400")
	 * @return example name, or null if that status/default response has no named example
	 */
	private String getMicrocksExampleNameForStatus(Operation operation, String statusCode) {
		if (operation == null || operation.getResponses() == null) return null;
		ApiResponse response = operation.getResponses().get(statusCode);
		if (response == null) {
			response = operation.getResponses().getDefault();
		}
		return getFirstExampleName(response);
	}

	/**
	 * Apply Microcks Header For Status
	 * When microcksHeaders is true, rebuilds the given Request's headers (custom headers plus a fresh
	 * X-Microcks-Response-Name) using the named example for the given status code specifically, instead of
	 * the operation-wide first-2xx-or-default value the Request inherited from setRequestHeaders. Used for
	 * body-property-variant Test Cases, which have a well-defined target status distinct from the
	 * operation's success response.
	 * @param request the Request to update
	 * @param operation instance of OpenAPI Operation, used to resolve the example name
	 * @param statusCode literal status code this Request targets
	 */
	private void applyMicrocksHeaderForStatus(RestRequest request, Operation operation, String statusCode) {
		if (!microcksHeaders) return;
		StringToStringMap requestHeaders = new StringToStringMap();
		if (headers != null && !headers.isEmpty()) {
			headers.forEach(header -> requestHeaders.put(header.getKey(), header.getValue()));
		}
		String exampleName = getMicrocksExampleNameForStatus(operation, statusCode);
		requestHeaders.put(MICROCKS_RESPONSE_NAME_HEADER, exampleName != null ? exampleName : DEFAULT);
		request.setRequestHeaders(requestHeaders);
	}

	/**
	 * Get content of SoapUI Project File (XML)
	 * @return SoapUI Project file content
	 * @throws IOException Exception
	 */
	public String getFileContent() throws IOException {
		String fileContent = "";
		if (project != null) {
			if (file == null) {
				createTempFile();
			}
			project.saveIn(file);
			fileContent = Files.readString(file.toPath());
		}
		return fileContent;
	}
	
	/**
	 * Delete temporal file
	 * @return result
	 */
	public boolean deleteTemporaryFile() {
		return file.delete();
	}
	
}
