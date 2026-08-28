package org.apiaddicts.apitools.openapi2soapui.model;

import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SOAP_UI_PROJECT_FILE_EXTENSION;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SOAP_UI_PROJECT_FILE_NAME;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.STEP_SUFFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.DEFAULT_REQUEST_NAME;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.EJECUTION_TEST_STEP;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.HEADER;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.PATH;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.QUERY;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.DEFAULT;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.JSON;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.VALID_HTTP_STATUS_CODES_ASSERTION;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.WRONG_STATUS_CODE;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SCRIPT_ASSERTION;
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
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_CASE_OK_SCOPE_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_CASE_OK_APPLICATION_TOKEN_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_ARRAY_ITEM_SEGMENT;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SERVICE_API_CASE_INFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.ARRAY_ITEM_PATH_SUFFIX;

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
import io.swagger.v3.oas.models.media.DateTimeSchema;
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

@Slf4j
@Getter
public class SoapUIProject {

	private String apiName;

	private String apiVersion;

	private OpenAPI openAPI;

	private File file;

	private List<Header> headers;

	private WsdlProject project;

	private RestService restService;

	private Set<String> testCaseNames;

	private boolean readOnly;

	private boolean minimalEndpoints;

	private boolean microcksHeaders;

	private boolean generateOneOfAnyOf;

	private boolean validateSchema;

	private boolean schemaIsInline;

	private boolean schemaPrettyPrint;

	private ExamplesConfig examples;

	private Map<String, Operation> operationByMethodKey = new HashMap<>();

	private boolean isInline;

	private boolean hasScopes;

	private boolean applicationToken;

	private int numberOfScopes;

	private int bodyPropertyCounter = 0;

	private int schemaPropertyCounter = 0;

	private Map<String, Boolean> currentBodyTokenTypes = new LinkedHashMap<>();

	private String bodyVariantOmitPath;

	private String bodyVariantWrongPath;

	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, true, examples);
	}

	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, false, examples);
	}

	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, hasScopes, false, examples);
	}

	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, Boolean applicationToken, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, hasScopes, applicationToken, null, examples);
	}

	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, Boolean applicationToken, Integer numberOfScopes, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
		this(apiName, openAPI, oAuth2Profiles, headers, testCaseNames, readOnly, serverPattern, minimalEndpoints,
				microcksHeaders, generateOneOfAnyOf, validateSchema, schemaIsInline, isInline, schemaPrettyPrint, hasScopes, applicationToken, numberOfScopes, examples, null);
	}

	/**
	 * @param apiName from request body
	 * @param openAPI OpenAPI Java Object
	 * @param oAuth2Profiles authentication profiles from request body
	 * @param headers from request body
	 * @param testCaseNames from request body; for each name, an extra {METHOD}_Case{name} Test Case identical to {METHOD}_CaseOkAllProperties is generated. Empty/null (default) generates none
	 * @param readOnly if true, only GET and OPTIONS test cases are generated
	 * @param minimalEndpoints if false (default), generates {METHOD}_CaseErrorRequired{Field} for every required body property and required query parameter; if true, collapses this to at most one such Test Case
	 * @param microcksHeaders if true, adds an X-Microcks-Response-Name header to each request, in addition to any custom headers
	 * @param generateOneOfAnyOf if true, oneOf/anyOf schemas are resolved using their first candidate when generating example bodies
	 * @param validateSchema if true (default), each generated Test Case gets a schema assertion in addition to its status-code assertion; if explicitly false, no schema assertion is added to any Test Case (the status-code assertion is always added either way)
	 * @param schemaIsInline if false (default), the response JSON Schema used by a Test Case's schema assertion is stored as a SoapUI Project Property and read via a context.expand("${#Project#key}") call instead of being embedded literally
	 * @param isInline if false (default), JSON request-body example values are stored as SoapUI Project Properties and referenced via a "${#Project#key}" token instead of being embedded literally
	 * @param schemaPrettyPrint if true (default), the JSON Schema used by a Test Case's schema assertion is pretty-printed (indented); if false, it is serialized compactly with no extra whitespace
	 * @param hasScopes if true, generates one additional test case per configured oAuth2Profiles entry beyond the first, each wired to that profile's own authentication, independent of the default request (which always uses the first profile and is never duplicated by an extra test case)
	 * @param applicationToken only relevant when hasScopes is also true; if true, additionally generates one extra test case per configured oAuth2Profiles entry whose grant type is CLIENT_CREDENTIALS, separate from the hasScopes scope variant test cases
	 * @param numberOfScopes only relevant when hasScopes is also true; the total number of test cases wired to a profile-based scope credential, counting the default request, using the first numberOfScopes configured oAuth2Profiles entries (in configured order). Values less than 1 (null, zero, negative) are treated as 1 (no extra test case). Does not affect applicationToken test cases
	 * @param examples custom example values from request body, used before falling back to internal defaults
	 * @param customAuthorizationsFile custom authorization requests from request body; if not empty, a dedicated "authorizations" Test Suite is created and added before the per-endpoint Test Suites
	 * @throws IOException
	 * @throws XmlException
	 * @throws SoapUIException
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, Boolean schemaIsInline, Boolean isInline, Boolean schemaPrettyPrint, Boolean hasScopes, Boolean applicationToken, Integer numberOfScopes, ExamplesConfig examples, List<CustomAuthorizationRequest> customAuthorizationsFile) throws IOException, XmlException, SoapUIException {
		this.apiName = apiName;
		this.openAPI = openAPI;
		this.headers = headers;
		this.examples = examples;

		this.apiVersion = openAPI.getInfo().getVersion();

		this.testCaseNames = (testCaseNames != null) ? testCaseNames : Collections.emptySet();

		this.readOnly = Boolean.TRUE.equals(readOnly);
		this.minimalEndpoints = Boolean.TRUE.equals(minimalEndpoints);
		this.microcksHeaders = Boolean.TRUE.equals(microcksHeaders);
		this.generateOneOfAnyOf = Boolean.TRUE.equals(generateOneOfAnyOf);
		this.validateSchema = !Boolean.FALSE.equals(validateSchema);
		this.schemaIsInline = Boolean.TRUE.equals(schemaIsInline);
		this.isInline = Boolean.TRUE.equals(isInline);
		this.schemaPrettyPrint = !Boolean.FALSE.equals(schemaPrettyPrint);
		this.hasScopes = Boolean.TRUE.equals(hasScopes);
		this.applicationToken = Boolean.TRUE.equals(applicationToken);
		this.numberOfScopes = (numberOfScopes != null) ? numberOfScopes : 0;

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
			authorizationsTestSuite = project.addNewTestSuite(AUTHORIZATIONS_TEST_SUITE_NAME + "_" + apiName + "_" + apiVersion + "-" + SERVICE_API_SUITE_SUFFIX);
			authorizationsTestSuite.setRunType(TestSuite.TestSuiteRunType.SEQUENTIAL);
			authorizationsTestSuite.setAbortOnError(false);
		}

		setRestServiceResources(openAPI.getPaths());
		setTestCases();

		if (authorizationsTestSuite != null) {
			setCustomAuthorizationTestCases(authorizationsTestSuite, customAuthorizationsFile);
		}
	}

	/**
	 * @throws IOException
	 */
	private void createTempFile() throws IOException {
		file = File.createTempFile(SOAP_UI_PROJECT_FILE_NAME, SOAP_UI_PROJECT_FILE_EXTENSION);
	}

	/**
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
			filtered = Collections.singletonList(match.orElseGet(() -> servers.get(0)));
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
	 * @param restResource instance of Resoruce
	 * @param parameter instance of Resoruce Parameter
	 * @param openAPIParameter instance of OpenAPI Parameter
	 */
	private void setResourceParameterExample(RestResource restResource, RestParamProperty parameter, Parameter openAPIParameter) {
		Object example = getParameterExample(openAPIParameter);
		if (example != null && !example.toString().isBlank()) restResource.setPropertyValue(parameter.getName(), example.toString());
	}

	/**
	 * @param restMethod instance of Method
	 * @param parameter instance of Method Parameter
	 * @param openAPIParameter instance of OpenAPI Parameter
	 */
	private void setMethodParameterExample(RestMethod restMethod, RestParamProperty parameter, Parameter openAPIParameter) {
		Object example = getParameterExample(openAPIParameter);
		if (example != null && !example.toString().isBlank()) restMethod.setPropertyValue(parameter.getName(), example.toString());
	}

	/**
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
	 * @param property
	 * @param refResolver
	 * @param path underscore-joined property path, used to key Project Properties when isInline is false
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	private Object getPropertyExample(Schema property, RefResolver refResolver, String path) throws JSONException {
		Object example = property.getExample();
		if (example != null) {
			return registerBodyValue(path, example);
		}
		return getExampleForResolvedType(resolveComposedSchema(property, refResolver), refResolver, path);
	}

	/**
	 * @param property resolved Schema
	 * @param refResolver instance of RefResolver
	 * @param path underscore-joined property path, used to key Project Properties when isInline is false
	 * @return example value for the schema's type
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	private Object getExampleForResolvedType(Schema property, RefResolver refResolver, String path) throws JSONException {
		if (bodyVariantWrongPath != null && bodyVariantWrongPath.equals(path)
				&& !isObjectSchema(property) && !isArraySchema(property)) {
			return registerBodyValue(path, QueryParamExampleUtils.invalidValue(property, examples != null ? examples.getWrong() : null));
		}
		if (isObjectSchema(property)) {
			return iterateProperties(property.getProperties(), refResolver, path);
		} else if (isArraySchema(property)) {
			JSONArray jsonArray = new JSONArray();
			Schema<?> items = refResolver.resolveSchema(property.getItems());
			jsonArray.put(getPropertyExample(items, refResolver, path + ARRAY_ITEM_PATH_SUFFIX));
			return jsonArray;
		} else if (property instanceof IntegerSchema || property instanceof NumberSchema
				|| INTEGER_TYPE.equals(property.getType()) || NUMBER_TYPE.equals(property.getType())) {
			return registerBodyValue(path, getConfiguredExample(false, ExampleValues::getNumber, java.math.BigDecimal.ZERO));
		} else if (property instanceof BooleanSchema || BOOLEAN_TYPE.equals(property.getType())) {
			return registerBodyValue(path, getConfiguredExample(false, ExampleValues::getBooleanValue, true));
		} else if (property instanceof DateSchema) {
			return registerBodyValue(path, getConfiguredExample(false, ExampleValues::getDate, new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
		} else if (property instanceof DateTimeSchema) {
			return registerBodyValue(path, getConfiguredExample(false, ExampleValues::getDateTime, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())));
		} else if (property instanceof StringSchema || STRING_TYPE.equals(property.getType())) {
			return registerBodyValue(path, getStringExample(property));
		}
		return registerBodyValue(path, "");
	}

	/**
	 * @param stringProperty string-typed Schema
	 * @return example value
	 */
	@SuppressWarnings("rawtypes")
	private Object getStringExample(Schema stringProperty) {
		List enums = stringProperty.getEnum();
		if (enums != null && !enums.isEmpty()) {
			return enums.get(0);
		} else if ("date-time".equalsIgnoreCase(stringProperty.getFormat())) {
			return getConfiguredExample(false, ExampleValues::getDateTime, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
		}
		return getConfiguredExample(false, ExampleValues::getString, "");
	}

	/**
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

	private static final String STRING_TYPE = "string";
	private static final String OBJECT_TYPE = "object";
	private static final String ARRAY_TYPE = "array";
	private static final String INTEGER_TYPE = "integer";
	private static final String NUMBER_TYPE = "number";
	private static final String BOOLEAN_TYPE = "boolean";

	private static final int MAX_ALLOF_DEPTH = 20;

	/**
	 * @param allOf list of member schemas to merge
	 * @param refResolver instance of RefResolver
	 * @return merged object schema
	 */
	@SuppressWarnings("rawtypes")
	private ObjectSchema mergeAllOf(List<Schema> allOf, RefResolver refResolver) {
		ObjectSchema merged = new ObjectSchema();
		Map<String, Schema> mergedProperties = new HashMap<>();
		List<String> mergedRequired = new ArrayList<>();
		collectAllOfMembers(allOf, refResolver, mergedProperties, mergedRequired, 0);
		merged.setProperties(mergedProperties);
		merged.setRequired(mergedRequired);
		return merged;
	}

	/**
	 * @param allOf list of member schemas to merge
	 * @param refResolver instance of RefResolver
	 * @param mergedProperties accumulator for merged properties
	 * @param mergedRequired accumulator for merged required property names
	 * @param depth current allOf nesting depth
	 */
	@SuppressWarnings("rawtypes")
	private void collectAllOfMembers(List<Schema> allOf, RefResolver refResolver, Map<String, Schema> mergedProperties, List<String> mergedRequired, int depth) {
		if (depth > MAX_ALLOF_DEPTH) {
			log.warn("allOf nesting exceeded {} levels; deeper members were not merged", MAX_ALLOF_DEPTH);
			return;
		}
		for (Schema member : allOf) {
			Schema resolvedMember = refResolver.resolveSchema(member);
			List<Schema> nestedAllOf = resolvedMember.getAllOf();
			if (nestedAllOf != null && !nestedAllOf.isEmpty()) {
				collectAllOfMembers(nestedAllOf, refResolver, mergedProperties, mergedRequired, depth + 1);
			}
			if (resolvedMember.getProperties() != null) {
				mergedProperties.putAll(resolvedMember.getProperties());
			}
			if (resolvedMember.getRequired() != null) {
				mergedRequired.addAll(resolvedMember.getRequired());
			}
		}
	}

	/**
	 * @param s schema to classify
	 * @return true if the schema represents an object
	 */
	@SuppressWarnings("rawtypes")
	private static boolean isObjectSchema(Schema s) {
		return s instanceof ObjectSchema
			|| OBJECT_TYPE.equals(s.getType())
			|| (s.getProperties() != null && !s.getProperties().isEmpty());
	}

	/**
	 * @param s schema to classify
	 * @return true if the schema represents an array
	 */
	@SuppressWarnings("rawtypes")
	private static boolean isArraySchema(Schema s) {
		return s instanceof ArraySchema || ARRAY_TYPE.equals(s.getType()) || s.getItems() != null;
	}

	/**
	 * @param object to convert
	 * @return json string
	 */
	private String mapObjectToJsonString(Object object) {
		return mapObjectToJsonString(object, true);
	}

	/**
	 * @param object to convert
	 * @param prettyPrint if true, the JSON is indented; if false, it is serialized compactly with no extra whitespace
	 * @return json string
	 */
	private String mapObjectToJsonString(Object object, boolean prettyPrint) {
		String jsonString = null;
		if (object instanceof JSONObject json) {
			try {
				jsonString = prettyPrint ? json.toString(2) : json.toString();
			} catch (JSONException e) {
				log.debug("Error mapObjectToJsonString", e);
			}
		} else {
			ObjectMapper mapper = new ObjectMapper();
			try {
				jsonString = prettyPrint
						? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object).replace("\r", "")
						: mapper.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				log.debug("Error mapObjectToJsonString", e);
			}
		}
		return jsonString;
	}

	/**
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
	 * @param restRequestConfig instance of Request Config
	 */
	private void setRequestJMSConfig(RestRequestConfig restRequestConfig) {
		JMSHeaderConfConfig jmsConfig = restRequestConfig.addNewJmsConfig();
		jmsConfig.setJMSDeliveryMode(JMSDeliveryModeTypeConfig.PERSISTENT);
		restRequestConfig.addNewJmsPropertyConfig();
	}

	/**
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
	 * @param oAuth2Profiles list of authentication items
	 */
	private void setAuthProfiles(List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles) {
		if (oAuth2Profiles != null && !oAuth2Profiles.isEmpty()) {
			oAuth2Profiles.forEach(this::setAuthProfile);
		}
	}
	
	/**
	 * @param oAuth2Profiles authentication item
	 */
	private void setAuthProfile(org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile oAuth2Profile) {
		if (oAuth2Profile.getGrantType() != null) {
			String grantType = oAuth2Profile.getGrantType().getText();
			OAuth2Flow oAuth2Flow = OAuth2Flow.valueOf(
					oAuth2Profile.getGrantType().equals(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS)
							? grantType : grantType + "_GRANT");

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

			WsdlTestCase testCase = testSuite.addNewTestCase(customRequest.getMethod().toUpperCase() + SERVICE_API_CASE_INFIX + toCaseFieldName(customRequest.getName()));
			TestStepConfig stepConfig = RestRequestStepFactory.createConfig(restRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
			testCase.addTestStep(stepConfig);
		}
	}

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
	 * @param restResource instance of Resource owning the Method
	 * @param restMethod instance of Method to generate the Test Suite for
	 */
	private void addTestSuiteForMethod(RestResource restResource, RestMethod restMethod) {
		String method = restMethod.getMethod().name();
		if (readOnly && !"GET".equals(method) && !"OPTIONS".equals(method)) return;
		Operation operation = operationByMethodKey.get(methodKey(restResource.getPath(), method));

		String testSuiteName = restResource.getPath() + "_" + apiName + "_" + apiVersion + "-" + method + "-" + SERVICE_API_SUITE_SUFFIX;
		WsdlTestSuite testSuite = project.addNewTestSuite(testSuiteName);
		testSuite.setRunType(TestSuite.TestSuiteRunType.SEQUENTIAL);
		testSuite.setAbortOnError(false);
		addServiceApiConventionTestCases(restMethod, testSuite, operation, method);

		if (hasScopes) {
			if (applicationToken) {
				addApplicationTokenTestCases(restMethod, testSuite, method);
			}
			addScopeVariantTestCases(restMethod, testSuite, method);
		}
	}

	/**
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
		addCustomNamedTestCases(restMethod, okAllPropertiesRequest, testSuite, operation, refResolver, method);
	}

	/**
	 * @param restMethod instance of Method to add the Test Cases to
	 * @param okAllPropertiesRequest the CaseOkAllProperties Request, cloned as the base for each variant
	 * @param testSuite Test Suite to add the Test Cases to
	 * @param operation instance of OpenAPI Operation, or null
	 * @param refResolver instance of RefResolver
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	private void addCustomNamedTestCases(RestMethod restMethod, RestRequest okAllPropertiesRequest, WsdlTestSuite testSuite,
			Operation operation, RefResolver refResolver, String method) {
		for (String name : testCaseNames) {
			RestRequest variantRequest = restMethod.cloneRequest(okAllPropertiesRequest, name);
			WsdlTestCase testCase = testSuite.addNewTestCase(method + SERVICE_API_CASE_INFIX + name);
			TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
			WsdlTestStep testStep = testCase.addTestStep(stepConfig);
			addSuccessAssertions(testStep, operation, refResolver);
		}
	}

	/**
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

		String testCaseName = method + SERVICE_API_CASE_INFIX + SERVICE_API_CASE_OK_ALL_PROPERTIES;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addSuccessAssertions(testStep, operation, refResolver);
		return variantRequest;
	}

	/**
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
			JSONObject body = buildRequiredPropertiesExample(bodySchema, new RefResolver(openAPI), "");
			String exampleStr = mapObjectToJsonString(body);
			exampleStr = stripQuotesAroundNonStringTokens(exampleStr);
			if (exampleStr != null) {
				variantRequest.setRequestContent(exampleStr);
			}
		}
		applyQueryParameterValues(variantRequest, operation, refResolver, true);

		String testCaseName = method + SERVICE_API_CASE_INFIX + SERVICE_API_CASE_OK_REQUIRED_PROPERTIES;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addSuccessAssertions(testStep, operation, refResolver);
	}

	/**
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
			try {
				json.put(propertyName, buildRequiredPropertyExampleValue(property, refResolver, childPath));
			} catch (JSONException e) {
				log.warn("Error buildRequiredPropertiesExample", e);
			}
		}
		return json;
	}

	/**
	 * @param property required property's own (not yet resolved) Schema
	 * @param refResolver instance of RefResolver
	 * @param childPath underscore-joined property path for this property, used to key Project Properties when isInline is false
	 * @return example value for this property
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	private Object buildRequiredPropertyExampleValue(Schema property, RefResolver refResolver, String childPath) throws JSONException {
		Schema refResolvedProperty = refResolver.resolveSchema(property);
		Schema fullyResolvedProperty = resolveComposedSchema(refResolvedProperty, refResolver);
		if (isObjectSchema(fullyResolvedProperty)) {
			return buildRequiredPropertiesExample(fullyResolvedProperty, refResolver, childPath);
		}
		if (isArraySchema(fullyResolvedProperty)) {
			Schema itemsSchema = refResolver.resolveSchema(fullyResolvedProperty.getItems());
			Schema fullyResolvedItems = resolveComposedSchema(itemsSchema, refResolver);
			JSONArray array = new JSONArray();
			array.put(isObjectSchema(fullyResolvedItems)
					? buildRequiredPropertiesExample(fullyResolvedItems, refResolver, childPath + ARRAY_ITEM_PATH_SUFFIX)
					: getPropertyExample(itemsSchema, refResolver, childPath + ARRAY_ITEM_PATH_SUFFIX));
			return array;
		}
		return getPropertyExample(refResolvedProperty, refResolver, childPath);
	}

	/**
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
	 * @param testStep instance of Test Step to attach the assertion to
	 * @param codesCsv comma-separated list of status codes the assertion should accept
	 */
	private void addStatusCodeAssertion(WsdlTestStep testStep, String codesCsv) {
		ValidHttpStatusCodesAssertion assertion = (ValidHttpStatusCodesAssertion)
				((RestTestRequestStep) testStep).addAssertion(VALID_HTTP_STATUS_CODES_ASSERTION);
		assertion.setCodes(codesCsv);
	}

	private static final class ErrorCaseContext {
		private final RestMethod restMethod;
		private final RestRequest baseRequest;
		private final WsdlTestSuite testSuite;
		private final Operation operation;
		private final RefResolver refResolver;
		private final String method;

		private ErrorCaseContext(RestMethod restMethod, RestRequest baseRequest, WsdlTestSuite testSuite,
				Operation operation, RefResolver refResolver, String method) {
			this.restMethod = restMethod;
			this.baseRequest = baseRequest;
			this.testSuite = testSuite;
			this.operation = operation;
			this.refResolver = refResolver;
			this.method = method;
		}
	}

	/**
	 * @param restMethod instance of Method to add the Test Cases to
	 * @param baseRequest the CaseOkAllProperties Request, cloned as the base for each variant (a fully
	 *        populated request the tester later adapts to actually trigger the error)
	 * @param testSuite Test Suite to add the Test Cases to
	 * @param operation instance of OpenAPI Operation, or null (no-op)
	 * @param refResolver instance of RefResolver
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	private void addErrorStatusCodeTestCases(RestMethod restMethod, RestRequest baseRequest, WsdlTestSuite testSuite,
			Operation operation, RefResolver refResolver, String method) {
		if (operation == null || operation.getResponses() == null) return;
		ErrorCaseContext context = new ErrorCaseContext(restMethod, baseRequest, testSuite, operation, refResolver, method);
		for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
			String code = entry.getKey();
			if (code.equalsIgnoreCase(DEFAULT) || code.startsWith("2")) continue;
			addErrorStatusCodeTestCase(context, code, entry.getValue());
		}
	}

	private void addErrorStatusCodeTestCase(ErrorCaseContext context, String statusCode, ApiResponse response) {
		RestRequest variantRequest = context.restMethod.cloneRequest(context.baseRequest, SERVICE_API_CASE_ERROR_STATUS_CODE_PREFIX + statusCode);
		applyMicrocksHeaderForStatus(variantRequest, context.operation, statusCode);

		String testCaseName = context.method + SERVICE_API_CASE_INFIX + SERVICE_API_CASE_ERROR_STATUS_CODE_PREFIX + statusCode;
		WsdlTestCase testCase = context.testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addStatusCodeAssertion(testStep, statusCode);
		addSchemaValidationAssertionForSchema(testStep, getJsonResponseSchema(response, context.refResolver), context.refResolver);
	}

	/**
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

		List<String> requiredBodyPaths = new ArrayList<>();
		if (bodySchema != null) {
			collectRequiredPropertyPaths(bodySchema, refResolver, "", requiredBodyPaths);
		}
		List<Parameter> requiredQueryParams = operation != null ? collectRequiredQueryParameters(operation) : Collections.emptyList();

		if (minimalEndpoints) {
			if (!requiredBodyPaths.isEmpty()) {
				requiredBodyPaths = requiredBodyPaths.subList(0, 1);
				requiredQueryParams = Collections.emptyList();
			} else if (!requiredQueryParams.isEmpty()) {
				requiredQueryParams = requiredQueryParams.subList(0, 1);
			}
		}

		ErrorCaseContext context = new ErrorCaseContext(restMethod, baseRequest, testSuite, operation, refResolver, method);
		for (String path : requiredBodyPaths) {
			addErrorRequiredBodyFieldTestCase(context, bodySchema, path, assertStatusCode, errorSchema);
		}
		for (Parameter param : requiredQueryParams) {
			addErrorRequiredQueryFieldTestCase(context, param, assertStatusCode, errorSchema);
		}
	}

	/**
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
	private void addErrorRequiredBodyFieldTestCase(ErrorCaseContext context, Schema bodySchema, String omitPath,
			String assertStatusCode, Schema errorSchema) {
		RestRequest variantRequest = context.restMethod.cloneRequest(context.baseRequest, SERVICE_API_CASE_ERROR_REQUIRED_PREFIX + omitPath);

		bodyPropertyCounter++;
		currentBodyTokenTypes = new LinkedHashMap<>();
		boolean injectWrong = examples != null && examples.getWrong() != null;
		if (injectWrong) {
			bodyVariantWrongPath = omitPath;
		} else {
			bodyVariantOmitPath = omitPath;
		}
		try {
			JSONObject body = iterateProperties(bodySchema.getProperties(), new RefResolver(openAPI), "");
			String exampleStr = mapObjectToJsonString(body);
			exampleStr = stripQuotesAroundNonStringTokens(exampleStr);
			if (exampleStr != null) {
				variantRequest.setRequestContent(exampleStr);
			}
		} finally {
			bodyVariantOmitPath = null;
			bodyVariantWrongPath = null;
		}
		applyMicrocksHeaderForStatus(variantRequest, context.operation, assertStatusCode);

		String testCaseName = context.method + SERVICE_API_CASE_INFIX + SERVICE_API_CASE_ERROR_REQUIRED_PREFIX + toCaseFieldName(omitPath);
		WsdlTestCase testCase = context.testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addStatusCodeAssertion(testStep, assertStatusCode);
		addSchemaValidationAssertionForSchema(testStep, errorSchema, context.refResolver);
	}

	@SuppressWarnings("rawtypes")
	private void addErrorRequiredQueryFieldTestCase(ErrorCaseContext context, Parameter param, String assertStatusCode, Schema errorSchema) {
		RestRequest variantRequest = context.restMethod.cloneRequest(context.baseRequest, SERVICE_API_CASE_ERROR_REQUIRED_PREFIX + param.getName());
		String errorValue = "";
		if (examples != null && examples.getWrong() != null && param.getSchema() != null) {
			errorValue = QueryParamExampleUtils.invalidValue(new RefResolver(openAPI).resolveSchema(param.getSchema()), examples.getWrong());
		}
		setRequestParameterValue(variantRequest, param.getName(), errorValue);
		applyMicrocksHeaderForStatus(variantRequest, context.operation, assertStatusCode);

		String testCaseName = context.method + SERVICE_API_CASE_INFIX + SERVICE_API_CASE_ERROR_REQUIRED_PREFIX + toCaseFieldName(param.getName());
		WsdlTestCase testCase = context.testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		addStatusCodeAssertion(testStep, assertStatusCode);
		addSchemaValidationAssertionForSchema(testStep, errorSchema, context.refResolver);
	}

	/**
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
	 * @param testStep instance of Test Step to attach the assertion to
	 * @param responseSchema already-resolved Schema to validate the response body against, or null (no-op)
	 * @param refResolver instance of RefResolver
	 */
	@SuppressWarnings("rawtypes")
	private void addSchemaValidationAssertionForSchema(WsdlTestStep testStep, Schema responseSchema, RefResolver refResolver) {
		if (!validateSchema || responseSchema == null) return;
		String script = buildSchemaValidationScript(buildJsonSchemaDefinition(responseSchema, refResolver, new HashSet<>()));
		if (script == null) return;
		GroovyScriptAssertion assertion = (GroovyScriptAssertion)
				((RestTestRequestStep) testStep).addAssertion(SCRIPT_ASSERTION);
		assertion.setScriptText(script);
	}

	/**
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
	 * @param schema to convert
	 * @param refResolver instance of RefResolver, still used to resolve allOf members via the shared mergeAllOf helper
	 * @param refsInPath $ref names currently being expanded along this branch, to detect cycles
	 * @return Map representing the equivalent JSON Schema definition, or null if schema is null
	 */
	@SuppressWarnings("rawtypes")
	private Object buildJsonSchemaDefinition(Schema schema, RefResolver refResolver, Set<String> refsInPath) {
		if (schema == null) return null;
		if (schema.get$ref() != null) {
			return buildRefDefinition(schema.get$ref(), refResolver, refsInPath);
		}
		boolean nullable = Boolean.TRUE.equals(schema.getNullable());
		Object composed = buildComposedDefinition(schema, refResolver, refsInPath);
		if (composed != null) {
			return applyNullable(composed, nullable);
		}
		return applyNullable(buildLeafDefinition(schema, refResolver, refsInPath), nullable);
	}

	@SuppressWarnings("rawtypes")
	private Object buildRefDefinition(String ref, RefResolver refResolver, Set<String> refsInPath) {
		if (refsInPath.contains(ref)) {
			return new LinkedHashMap<>();
		}
		Schema target = resolveComponentSchema(ref);
		if (target == null) return new LinkedHashMap<>();
		Set<String> nextRefsInPath = new HashSet<>(refsInPath);
		nextRefsInPath.add(ref);
		return buildJsonSchemaDefinition(target, refResolver, nextRefsInPath);
	}

	@SuppressWarnings("rawtypes")
	private Object buildComposedDefinition(Schema schema, RefResolver refResolver, Set<String> refsInPath) {
		if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
			return buildJsonSchemaDefinition(mergeAllOf(schema.getAllOf(), refResolver), refResolver, refsInPath);
		}
		List<Schema> alternatives = (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) ? schema.getOneOf() : schema.getAnyOf();
		if (alternatives == null || alternatives.isEmpty()) {
			return null;
		}
		Map<String, Object> definition = new LinkedHashMap<>();
		List<Object> anyOf = new ArrayList<>();
		for (Schema alternative : alternatives) {
			anyOf.add(buildJsonSchemaDefinition(alternative, refResolver, refsInPath));
		}
		definition.put("anyOf", anyOf);
		return definition;
	}

	@SuppressWarnings("rawtypes")
	private Object buildLeafDefinition(Schema schema, RefResolver refResolver, Set<String> refsInPath) {
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
		} else if (isStringType(schema)) {
			definition.put("type", STRING_TYPE);
		} else {
			buildObjectDefinition(schema, definition, refResolver, refsInPath);
		}
		addCommonConstraints(schema, definition);
		return definition;
	}

	@SuppressWarnings("rawtypes")
	private boolean isStringType(Schema schema) {
		return schema instanceof StringSchema || schema instanceof DateSchema || STRING_TYPE.equals(schema.getType());
	}

	@SuppressWarnings("rawtypes")
	private void buildObjectDefinition(Schema schema, Map<String, Object> definition, RefResolver refResolver, Set<String> refsInPath) {
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

	/**
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
	 * @param jsonSchemaDefinition Map produced by buildJsonSchemaDefinition
	 * @return Groovy script source, or null if the schema definition could not be serialized
	 */
	private String buildSchemaValidationScript(Object jsonSchemaDefinition) {
		String schemaJson = mapObjectToJsonString(jsonSchemaDefinition, schemaPrettyPrint);
		if (schemaJson == null) return null;
		String schemaSource;
		if (schemaIsInline) {
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
	 * @param path Resource path
	 * @param httpMethod HTTP method name
	 * @return composite key
	 */
	private String methodKey(String path, String httpMethod) {
		return path + "#" + httpMethod;
	}

	/**
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

	/**
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
			if (isObjectSchema(resolvedProperty)) {
				collectRequiredPropertyPaths(resolvedProperty, refResolver, childPath, out);
			} else if (isArraySchema(resolvedProperty)) {
				collectRequiredPropertyPaths(resolvedProperty.getItems(), refResolver, childPath + ARRAY_ITEM_PATH_SUFFIX, out);
			}
		});
	}

	/**
	 * @param restMethod instance of Method to generate variants for
	 * @param testSuite Test Suite to add the variant Test Cases to
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	private void addScopeVariantTestCases(RestMethod restMethod, WsdlTestSuite testSuite, String method) {
		List<OAuth2Profile> oAuth2ProfileList = project.getOAuth2ProfileContainer().getOAuth2ProfileList();
		if (oAuth2ProfileList == null || oAuth2ProfileList.isEmpty()) return;
		int desiredScopeCount = Math.max(numberOfScopes, 1);
		if (desiredScopeCount < oAuth2ProfileList.size()) {
			oAuth2ProfileList = oAuth2ProfileList.subList(0, desiredScopeCount);
		}
		if (oAuth2ProfileList.size() <= 1) return;
		RestRequest defaultRequest = restMethod.getRequestByName(DEFAULT_REQUEST_NAME);
		oAuth2ProfileList.subList(1, oAuth2ProfileList.size())
				.forEach(oAuth2Profile -> addScopeVariantTestCase(restMethod, defaultRequest, testSuite, oAuth2Profile, method));
	}

	/**
	 * @param restMethod instance of Method to add the variant Request to
	 * @param defaultRequest the Method's default Request, cloned as the base for the variant Request
	 * @param testSuite Test Suite to add the variant Test Case to
	 * @param oAuth2Profile the SoapUI-native OAuth2 Profile this variant Test Case should be wired to
	 */
	private void addScopeVariantTestCase(RestMethod restMethod, RestRequest defaultRequest, WsdlTestSuite testSuite, OAuth2Profile oAuth2Profile, String method) {
		String requestName = HAS_SCOPES_VARIANT_PREFIX + oAuth2Profile.getName();
		RestRequest variantRequest = restMethod.cloneRequest(defaultRequest, requestName);

		CredentialsConfig credentialsConfig = CredentialsConfig.Factory.newInstance();
		credentialsConfig.setSelectedAuthProfile(oAuth2Profile.getName());
		credentialsConfig.setAuthType(AuthType.O_AUTH_2_0);
		variantRequest.getConfig().setCredentials(credentialsConfig);

		String testCaseName = method + SERVICE_API_CASE_INFIX + SERVICE_API_CASE_OK_SCOPE_PREFIX + toCaseFieldName(oAuth2Profile.getName());
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		testCase.addTestStep(stepConfig);
	}

	/**
	 * @param restMethod instance of Method to generate variants for
	 * @param testSuite Test Suite to add the variant Test Cases to
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	private void addApplicationTokenTestCases(RestMethod restMethod, WsdlTestSuite testSuite, String method) {
		List<OAuth2Profile> oAuth2ProfileList = project.getOAuth2ProfileContainer().getOAuth2ProfileList();
		if (oAuth2ProfileList == null || oAuth2ProfileList.isEmpty()) return;
		RestRequest defaultRequest = restMethod.getRequestByName(DEFAULT_REQUEST_NAME);
		oAuth2ProfileList.stream()
				.filter(oAuth2Profile -> OAuth2Flow.CLIENT_CREDENTIALS_GRANT.equals(oAuth2Profile.getOAuth2Flow()))
				.forEach(oAuth2Profile -> addApplicationTokenTestCase(restMethod, defaultRequest, testSuite, oAuth2Profile, method));
	}

	/**
	 * @param restMethod instance of Method to add the variant Request to
	 * @param defaultRequest the Method's default Request, cloned as the base for the variant Request
	 * @param testSuite Test Suite to add the variant Test Case to
	 * @param oAuth2Profile the SoapUI-native, CLIENT_CREDENTIALS-grant OAuth2 Profile this variant Test Case should be wired to
	 * @param method HTTP method name in uppercase, used as the Test Case name prefix
	 */
	private void addApplicationTokenTestCase(RestMethod restMethod, RestRequest defaultRequest, WsdlTestSuite testSuite, OAuth2Profile oAuth2Profile, String method) {
		String requestName = APPLICATION_TOKEN_VARIANT_PREFIX + oAuth2Profile.getName();
		RestRequest variantRequest = restMethod.cloneRequest(defaultRequest, requestName);

		CredentialsConfig credentialsConfig = CredentialsConfig.Factory.newInstance();
		credentialsConfig.setSelectedAuthProfile(oAuth2Profile.getName());
		credentialsConfig.setAuthType(AuthType.O_AUTH_2_0);
		variantRequest.getConfig().setCredentials(credentialsConfig);

		String testCaseName = method + SERVICE_API_CASE_INFIX + SERVICE_API_CASE_OK_APPLICATION_TOKEN_PREFIX + toCaseFieldName(oAuth2Profile.getName());
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		testCase.addTestStep(stepConfig);
	}

	/**
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
		if (!requestHeaders.containsKey(MICROCKS_RESPONSE_NAME_HEADER)) {
			String exampleName = getMicrocksExampleNameForStatus(operation, statusCode);
			requestHeaders.put(MICROCKS_RESPONSE_NAME_HEADER, exampleName != null ? exampleName : DEFAULT);
		}
		request.setRequestHeaders(requestHeaders);
	}

	/**
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
	 * @return result
	 */
	public boolean deleteTemporaryFile() {
		return file.delete();
	}

}
