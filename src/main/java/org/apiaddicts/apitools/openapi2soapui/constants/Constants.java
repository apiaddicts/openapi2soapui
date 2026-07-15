package org.apiaddicts.apitools.openapi2soapui.constants;

public class Constants {
	
	private Constants() {
		// Intentional blank
	}
	
	public static final String SOAP_UI_PROJECT_FILE_NAME = "soapui-project";
	public static final String SOAP_UI_PROJECT_FILE_EXTENSION = ".xml";
	
	public static final String STEP_SUFFIX = "TestStep";

	public static final String AUTHORIZATIONS_TEST_SUITE_NAME = "authorizations";
	
	public static final String EJECUTION_TEST_STEP = "Execution";

	public static final String HEADER = "header";
	public static final String QUERY = "query";
	public static final String PATH = "path";
	
	public static final String DEFAULT = "default";

	public static final String JSON = "json";
	
	public static final String DEFAULT_REQUEST_NAME = "Request 1";

	public static final String HEADERS_KEY = "headers";
	public static final String AUTHENTICATION_PROFILES_KEY = "oAuth2Profiles";
	public static final String TEST_CASE_NAMES_KEY = "testCaseNames";
	public static final String CUSTOM_AUTHORIZATIONS_KEY = "customAuthorizationsFile";

	public static final String VALID_HTTP_STATUS_CODES_ASSERTION = "Valid HTTP Status Codes";
	public static final String WRONG_STATUS_CODE = "400";
	public static final String SCRIPT_ASSERTION = "Script Assertion";
	public static final String HAS_SCOPES_VARIANT_PREFIX = "scope ";
	public static final String APPLICATION_TOKEN_VARIANT_PREFIX = "application_token ";

	public static final String MICROCKS_RESPONSE_NAME_HEADER = "X-Microcks-Response-Name";

	public static final String SELECT_QUERY_PARAM = "$select";
	public static final String EXCLUDE_QUERY_PARAM = "$exclude";

	// Test Suite/Test Case naming convention
	public static final String SERVICE_API_SUITE_SUFFIX = "Suite";
	public static final String SERVICE_API_CASE_OK_ALL_PROPERTIES = "OkAllProperties";
	public static final String SERVICE_API_CASE_OK_REQUIRED_PROPERTIES = "OkRequiredProperties";
	public static final String SERVICE_API_CASE_ERROR_STATUS_CODE_PREFIX = "ErrorStatusCode";
	public static final String SERVICE_API_CASE_ERROR_REQUIRED_PREFIX = "ErrorRequired";
	public static final String SERVICE_API_CASE_OK_SCOPE_PREFIX = "OkScope";
	public static final String SERVICE_API_CASE_OK_APPLICATION_TOKEN_PREFIX = "OkApplicationToken";
	public static final String SERVICE_API_ARRAY_ITEM_SEGMENT = "item";
	public static final String SERVICE_API_CASE_INFIX = "_Case";
	public static final String ARRAY_ITEM_PATH_SUFFIX = "_" + SERVICE_API_ARRAY_ITEM_SEGMENT;
}
