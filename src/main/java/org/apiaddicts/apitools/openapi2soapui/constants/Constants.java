package org.apiaddicts.apitools.openapi2soapui.constants;

public class Constants {
	
	private Constants() {
		// Intentional blank
	}
	
	public static final String SOAP_UI_PROJECT_FILE_NAME = "soapui-project";
	public static final String SOAP_UI_PROJECT_FILE_EXTENSION = ".xml";
	
	public static final String SUITE_SUFFIX = "TestSuite";
	public static final String CASE_SUFFIX = "TestCase";
	public static final String STEP_SUFFIX = "TestStep";
	
	public static final String SUCCESS_TEST_CASE = "Success";
	
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

	// Feature: microcksHeaders
	public static final String MICROCKS_RESPONSE_HEADER_KEY = "X-Microcks-Response-Name";

	// Feature: validateSchema — Groovy test step
	public static final String VALIDATE_SCHEMA_GROOVY_TYPE = "groovy";
	public static final String VALIDATE_SCHEMA_STEP_NAME = "Validation_TestStep";
	public static final String VALIDATE_SCHEMA_GROOVY_SCRIPT =
		"def results = testRunner.getResults()\n" +
		"if (results != null && !results.isEmpty()) {\n" +
		"    def response = results.last().getResponse()\n" +
		"    if (response != null) {\n" +
		"        def statusCode = response.getStatusCode() as int\n" +
		"        assert statusCode >= 200 && statusCode < 300 :\n" +
		"            'Expected 2xx status code but got: ' + statusCode\n" +
		"    }\n" +
		"}";
}
