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
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SUCCESS_STATUS_CODE;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.WRONG_STATUS_CODE;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.SCRIPT_ASSERTION;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.QUERY_PARAM_VARIANT_PREFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.QUERY_PARAM_VARIANT_WRONG_SUFFIX;
import static org.apiaddicts.apitools.openapi2soapui.constants.Constants.MICROCKS_RESPONSE_NAME_HEADER;

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
	 * When false, an extra valid/invalid test case pair is generated for each optional query parameter
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
	 * SoapUIProject constructor
	 * Set default test case names if testCaseNames is null or empty
	 * Create temporal file to save SoapUI Project
	 * Create instance of WsdlProject as SoapUI Project
	 * Set SoapUI Project name
	 * Set SoapUI Project Authentication Profiles
	 * Add REST Service to SoapUI Project
	 * Set REST Service Endpoints
	 * Set REST Service Resources
	 * Set SoapUI Project Test Cases
	 * @param apiName from request body
	 * @param openAPI OpenAPI Java Object
	 * @param oAuth2Profiles authentication profiles from request body
	 * @param headers from request body
	 * @param testCaseNames from request body
	 * @param readOnly if true, only GET and OPTIONS test cases are generated
	 * @param minimalEndpoints if false, an extra valid/invalid test case pair is generated for each optional query parameter
	 * @param microcksHeaders if true, adds an X-Microcks-Response-Name header to each request, in addition to any custom headers
	 * @param generateOneOfAnyOf if true, oneOf/anyOf schemas are resolved using their first candidate when generating example bodies
	 * @param validateSchema if true, adds a Script Assertion to each main test-case request's test step that validates the response body against the JSON Schema of the operation's first 2xx JSON response
	 * @param examples custom example values from request body, used before falling back to internal defaults
	 * @throws IOException
	 * @throws XmlException
	 * @throws SoapUIException
	 */
	public SoapUIProject(String apiName, OpenAPI openAPI, List<org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCaseNames, Boolean readOnly, String serverPattern, Boolean minimalEndpoints, Boolean microcksHeaders, Boolean generateOneOfAnyOf, Boolean validateSchema, ExamplesConfig examples) throws IOException, XmlException, SoapUIException {
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

		createTempFile();
		
		project = new WsdlProject();
		project.setName(apiName + "_" + apiVersion);
		
		if (oAuth2Profiles != null) {
			setAuthProfiles(oAuth2Profiles);
		}
		
		restService = (RestService) project.addNewInterface(apiName, RestServiceFactory.REST_TYPE);
		restService.setDescription(openAPI.getInfo().getDescription());
		
		setRestServiceEndpoints(openAPI.getServers(), serverPattern);
		setRestServiceResources(openAPI.getPaths());
		setTestCases();
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
		List<Server> filtered = servers;
		if (serverPattern != null && !serverPattern.isBlank()) {
			String cleanPattern = serverPattern.replace("%", "");
			Optional<Server> match = servers.stream()
					.filter(s -> s.getUrl().contains(cleanPattern))
					.findFirst();
			filtered = match.isPresent()
					? Collections.singletonList(match.get())
					: Collections.singletonList(servers.get(0));
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
			example = iterateProperties(properties, refResolver);
		}
		return example;
	}

	/**
	 * Iterate all properties of schema an set an example, if schema is $ref, $ref is resolved
	 * @param properties map of properties (property name, property schema)
	 * @param refResolver to help resolve schemas $ref
	 * @return json object with example of its properties 
	 */
	@SuppressWarnings("rawtypes")
	private JSONObject iterateProperties(Map<String, Schema> properties, RefResolver refResolver) {
		JSONObject json = new JSONObject();
		if (properties != null  && !properties.isEmpty()) {
			properties.forEach((propertyName, property) -> {
				property = refResolver.resolveSchema(property);
				try {
					Object example = getPropertyExample(property, refResolver);
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
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	private Object getPropertyExample(Schema property, RefResolver refResolver) throws JSONException {
		Object example = property.getExample();
		if (example != null) {
			return example;
		}
		return getExampleForResolvedType(resolveComposedSchema(property, refResolver), refResolver);
	}

	/**
	 * Get example for a resolved (non-composed) schema, dispatching by concrete schema type
	 * @param property resolved Schema
	 * @param refResolver instance of RefResolver
	 * @return example value for the schema's type
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	private Object getExampleForResolvedType(Schema property, RefResolver refResolver) throws JSONException {
		if (property instanceof ObjectSchema) {
			return iterateProperties(((ObjectSchema) property).getProperties(), refResolver);
		} else if (property instanceof ArraySchema) {
			JSONArray jsonArray = new JSONArray();
			Schema<?> items = refResolver.resolveSchema(((ArraySchema) property).getItems());
			jsonArray.put(getPropertyExample(items, refResolver));
			return jsonArray;
		} else if (property instanceof IntegerSchema || property instanceof NumberSchema) {
			return getConfiguredExample(false, ExampleValues::getNumber, java.math.BigDecimal.ZERO);
		} else if (property instanceof BooleanSchema) {
			return getConfiguredExample(false, ExampleValues::getBooleanValue, true);
		} else if (property instanceof DateSchema) {
			return getConfiguredExample(false, ExampleValues::getDate, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		} else if (property instanceof StringSchema) {
			return getStringExample((StringSchema) property);
		}
		return "";
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
	 * Convert Object or JSONObject to JSON String
	 * @param object to convert
	 * @return json string
	 */
	private String mapObjectToJsonString(Object object) {
		String jsonString = null;
		if (object instanceof JSONObject) {
			try {
				jsonString = ((JSONObject) object).toString(2);
			} catch (JSONException e) {
				log.debug("Error mapObjectToJsonString", e);
			}
		} else {
			ObjectMapper mapper = new ObjectMapper();
			try {
				jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object).replaceAll("\\r", "");
			} catch (JsonProcessingException e) {
				log.debug("Error mapObjectToJsonString", e);
			}
		}
		return jsonString;
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
	 * Adds a Test Case per configured test case name, plus optional query parameter variant Test Cases
	 * When validateSchema is true, also attaches a Script Assertion validating the response body against the
	 * operation's JSON Schema to each main test step
	 * @param restResource instance of Resource owning the Method
	 * @param restMethod instance of Method to generate the Test Suite for
	 */
	private void addTestSuiteForMethod(RestResource restResource, RestMethod restMethod) {
		String method = restMethod.getMethod().name();
		if (readOnly && !"GET".equals(method) && !"OPTIONS".equals(method)) return;
		String testSuiteName = restResource.getPath() + "_" + method + "_" + SUITE_SUFFIX;
		WsdlTestSuite testSuite = project.addNewTestSuite(testSuiteName);
		Operation operation = operationByMethodKey.get(methodKey(restResource.getPath(), method));
		for (String testCaseNameItem : testCaseNames) {
			String testCaseName = testCaseNameItem + "_" + CASE_SUFFIX;
			WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
			TestStepConfig ejecutionTestStepConfig = RestRequestStepFactory.createConfig(restMethod.getRequestByName(DEFAULT_REQUEST_NAME), EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
			WsdlTestStep testStep = testCase.addTestStep(ejecutionTestStepConfig);
			if (validateSchema) {
				addSchemaValidationAssertion(testStep, operation);
			}
		}
		if (!minimalEndpoints) {
			addQueryParamVariantTestCases(restResource, restMethod, testSuite);
		}
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
			Content content = entry.getValue().getContent();
			if (content == null) continue;
			for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
				Schema schema = mediaTypeEntry.getValue().getSchema();
				if (mediaTypeEntry.getKey().toLowerCase().contains(JSON) && schema != null) {
					return refResolver.resolveSchema(schema);
				}
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
	 * @param jsonSchemaDefinition Map produced by buildJsonSchemaDefinition
	 * @return Groovy script source, or null if the schema definition could not be serialized
	 */
	private String buildSchemaValidationScript(Object jsonSchemaDefinition) {
		String schemaJson = mapObjectToJsonString(jsonSchemaDefinition);
		if (schemaJson == null) return null;
		// Escape backslashes and single quotes so the JSON text survives Groovy's own string-literal escaping
		// unchanged (matters for enum values containing backslashes, e.g. Windows-style paths)
		String safeSchemaJson = schemaJson.replace("\\", "\\\\").replace("'", "\\'");
		return String.join("\n",
				"def json = null",
				"try {",
				"    json = new groovy.json.JsonSlurper().parseText(messageExchange.responseContent)",
				"} catch (Exception e) {",
				"    assert false : \"A JSON response was expected\"",
				"}",
				"",
				"def schema = new groovy.json.JsonSlurper().parseText('''" + safeSchemaJson + "''')",
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
	 * Add Query Param Variant Test Cases
	 * For each optional query parameter of the Operation bound to this Method, add a valid and an invalid
	 * test case, each asserting the corresponding HTTP status code (200 or 400)
	 * @param restResource instance of Resource owning the Method
	 * @param restMethod instance of Method to generate variants for
	 * @param testSuite Test Suite to add the variant Test Cases to
	 */
	private void addQueryParamVariantTestCases(RestResource restResource, RestMethod restMethod, WsdlTestSuite testSuite) {
		Operation operation = operationByMethodKey.get(methodKey(restResource.getPath(), restMethod.getMethod().name()));
		if (operation == null) return;
		List<Parameter> queryParams = getQueryParameters(operation.getParameters());
		RestRequest defaultRequest = restMethod.getRequestByName(DEFAULT_REQUEST_NAME);
		getOptionalParameters(queryParams).forEach(param -> {
			addQueryParamVariantTestCase(restMethod, defaultRequest, testSuite, queryParams, param, false);
			addQueryParamVariantTestCase(restMethod, defaultRequest, testSuite, queryParams, param, true);
		});
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
	 * Get Query Parameters
	 * Filter OpenAPI Parameters to keep only the ones in the query
	 * @param parameters list of OpenAPI Parameters to filter
	 * @return list of query Parameters
	 */
	private List<Parameter> getQueryParameters(List<Parameter> parameters) {
		if (parameters == null) return Collections.emptyList();
		return parameters.stream()
				.filter(param -> QUERY.equalsIgnoreCase(param.getIn()))
				.collect(Collectors.toList());
	}

	/**
	 * Get Optional Parameters
	 * Filter Parameters to keep only the ones that are not required
	 * @param parameters list of Parameters to filter
	 * @return list of optional Parameters
	 */
	private List<Parameter> getOptionalParameters(List<Parameter> parameters) {
		return parameters.stream()
				.filter(param -> !Boolean.TRUE.equals(param.getRequired()))
				.collect(Collectors.toList());
	}

	/**
	 * Add Query Param Variant Test Case
	 * Clone the default Request (carrying over its endpoint, auth, media type, body and headers), then
	 * explicitly set every query parameter to a valid value, except the targeted parameter which gets
	 * an invalid value on the "wrong" variant
	 * Add a Test Case with this Request and a "Valid HTTP Status Codes" assertion for the expected status code
	 * @param restMethod instance of Method to add the variant Request to
	 * @param defaultRequest the Method's default Request, cloned as the base for the variant Request
	 * @param testSuite Test Suite to add the variant Test Case to
	 * @param queryParams all query Parameters of the Operation, so every one can be given an explicit valid value
	 * @param targetParam optional query Parameter being varied
	 * @param wrong when true, generates the invalid-value variant (expects 400), otherwise the valid-value variant (expects 200)
	 */
	private void addQueryParamVariantTestCase(RestMethod restMethod, RestRequest defaultRequest, WsdlTestSuite testSuite,
			List<Parameter> queryParams, Parameter targetParam, boolean wrong) {
		String requestName = QUERY_PARAM_VARIANT_PREFIX + targetParam.getName() + (wrong ? QUERY_PARAM_VARIANT_WRONG_SUFFIX : "");
		RestRequest variantRequest = restMethod.cloneRequest(defaultRequest, requestName);

		queryParams.forEach(param -> {
			boolean isTarget = param.getName().equals(targetParam.getName());
			String value = (isTarget && wrong) ? getInvalidQueryParamValue(param) : getValidQueryParamValue(param);
			variantRequest.setPropertyValue(param.getName(), value);
		});

		String testCaseName = requestName + "_" + CASE_SUFFIX;
		WsdlTestCase testCase = testSuite.addNewTestCase(testCaseName);
		TestStepConfig stepConfig = RestRequestStepFactory.createConfig(variantRequest, EJECUTION_TEST_STEP + "_" + STEP_SUFFIX);
		WsdlTestStep testStep = testCase.addTestStep(stepConfig);

		ValidHttpStatusCodesAssertion assertion = (ValidHttpStatusCodesAssertion)
				((RestTestRequestStep) testStep).addAssertion(VALID_HTTP_STATUS_CODES_ASSERTION);
		assertion.setCodes(wrong ? WRONG_STATUS_CODE : SUCCESS_STATUS_CODE);
	}

	/**
	 * Get Valid Query Param Value
	 * Prefer the OpenAPI Parameter example (example/examples/x-example), falling back to a type-aware
	 * generic value (honoring enum values when present) when no example is defined
	 * @param param OpenAPI Parameter to compute a valid value for
	 * @return valid value as String
	 */
	private String getValidQueryParamValue(Parameter param) {
		Object example = getParameterExample(param);
		if (example != null && !example.toString().isBlank()) return example.toString();
		return QueryParamExampleUtils.validValue(param.getSchema(), examples != null ? examples.getSuccessful() : null);
	}

	/**
	 * Get Invalid Query Param Value
	 * Compute a type-aware invalid value for the targeted parameter's schema, used for the "wrong" variant
	 * @param param OpenAPI Parameter to compute an invalid value for
	 * @return invalid value as String
	 */
	private String getInvalidQueryParamValue(Parameter param) {
		return QueryParamExampleUtils.invalidValue(param.getSchema(), examples != null ? examples.getWrong() : null);
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
