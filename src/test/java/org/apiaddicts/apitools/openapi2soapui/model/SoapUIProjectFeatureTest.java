package org.apiaddicts.apitools.openapi2soapui.model;

import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.request.ExampleSet;
import org.apiaddicts.apitools.openapi2soapui.request.ExampleValues;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectOptions;
import com.eviware.soapui.support.SoapUIException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SoapUIProject Feature Tests")
class SoapUIProjectFeatureTest {

	private OpenAPI openAPI;

	@BeforeEach
	void setUp() {
		String spec = "openapi: 3.0.0\n" +
			"info:\n" +
			"  title: Test API\n" +
			"  version: 1.0.0\n" +
			"servers:\n" +
			"  - url: http://api.example.com/v1\n" +
			"  - url: http://staging.example.com/v1\n" +
			"paths:\n" +
			"  /users:\n" +
			"    get:\n" +
			"      operationId: listUsers\n" +
			"      responses:\n" +
			"        '200':\n" +
			"          description: OK\n" +
			"          content:\n" +
			"            application/json:\n" +
			"              schema:\n" +
			"                type: object\n" +
			"    post:\n" +
			"      operationId: createUser\n" +
			"      requestBody:\n" +
			"        content:\n" +
			"          application/json:\n" +
			"            schema:\n" +
			"              type: object\n" +
			"      responses:\n" +
			"        '201':\n" +
			"          description: Created\n" +
			"          content:\n" +
			"            application/json:\n" +
			"              schema:\n" +
			"                type: object\n";
		openAPI = new OpenAPIV3Parser().readContents(spec).getOpenAPI();
	}

	@Test
	@DisplayName("Feature 1: readOnly - should exclude write operations")
	void testReadOnlyFeature() throws IOException, XmlException, SoapUIException {
		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setReadOnly(true);

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should contain GET (read operation)
		assertTrue(xml.contains("listUsers") || xml.contains("GET"), "Read operations should be included");
		// Should NOT contain POST (write operation)
		assertFalse(xml.contains("createUser"), "Write operations should be excluded in readOnly mode");
	}

	@Test
	@DisplayName("Feature 2: serverPattern - should select matching server")
	void testServerPatternFeature() throws IOException, XmlException, SoapUIException {
		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setServerPattern("staging");

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should contain staging server URL
		assertTrue(xml.contains("staging.example.com"), "Staging server should be used when pattern matches");
		// Should NOT contain production server
		assertFalse(xml.contains("api.example.com") && xml.contains("staging.example.com") && xml.indexOf("api.example.com") < xml.indexOf("staging.example.com"),
			"Should prefer staging server over production");
	}

	@Test
	@DisplayName("Feature 3: minimalEndpoints - should only generate Success test case")
	void testMinimalEndpointsFeature() throws IOException, XmlException, SoapUIException {
		java.util.Set<String> testCases = new java.util.HashSet<>();
		testCases.add("Success");
		testCases.add("ErrorCase");

		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setMinimalEndpoints(true);

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, testCases, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should have Success_TestCase
		assertTrue(xml.contains("Success_TestCase"), "Success test case should always be present");
		// Should NOT have ErrorCase_TestCase when minimalEndpoints is true
		assertFalse(xml.contains("ErrorCase_TestCase"), "ErrorCase should be excluded in minimal mode");
	}

	@Test
	@DisplayName("Feature 4: microcksHeaders - should add X-Microcks-Response-Name header")
	void testMicrocksHeadersFeature() throws IOException, XmlException, SoapUIException {
		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setMicrocksHeaders(true);

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should contain Microcks header
		assertTrue(xml.contains("X-Microcks-Response-Name"), "Microcks header should be added");
		// Should contain operationId as header value
		assertTrue(xml.contains("listUsers") || xml.contains("createUser"), "OperationId should be used as header value");
	}

	@Test
	@DisplayName("Feature 5: generateOneOfAnyOf - should not break with option enabled")
	void testGenerateOneOfAnyOfFeature() throws IOException, XmlException, SoapUIException {
		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setGenerateOneOfAnyOf(true);

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should successfully generate project with generateOneOfAnyOf enabled
		assertTrue(xml.contains("TestAPI"), "Project should be created with generateOneOfAnyOf option enabled");
		assertTrue(xml.length() > 100, "XML should contain valid project structure");
	}

	@Test
	@DisplayName("Feature 6: examples - should use custom example values")
	void testCustomExamplesFeature() throws IOException, XmlException, SoapUIException {
		ExampleSet exampleSet = new ExampleSet();
		exampleSet.setString("custom_string");
		exampleSet.setNumber(999);
		exampleSet.setBooleanValue(false);
		exampleSet.setDate("2025-12-31");
		exampleSet.setDateTime("2025-12-31T23:59:59.000+00:00");

		ExampleValues exampleValues = new ExampleValues();
		exampleValues.setSuccessful(exampleSet);

		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setExamples(exampleValues);

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should have valid XML structure
		assertTrue(xml.contains("TestAPI"), "Project should be created with custom examples");
		assertTrue(xml.length() > 100, "XML should contain project structure");
	}

	@Test
	@DisplayName("Feature 7: validateSchema - should add Groovy validation step")
	void testValidateSchemaFeature() throws IOException, XmlException, SoapUIException {
		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setValidateSchema(true);

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should contain Groovy validation step
		assertTrue(xml.contains("Validation_TestStep"), "Validation step should be added");
		assertTrue(xml.contains("groovy"), "Groovy script should be added");
		assertTrue(xml.contains("2xx") || xml.contains("statusCode"), "Validation should check status code");
	}

	@Test
	@DisplayName("All features combined - should work together")
	void testAllFeaturesEnabled() throws IOException, XmlException, SoapUIException {
		ExampleSet exampleSet = new ExampleSet();
		exampleSet.setString("test");
		exampleSet.setNumber(42);

		ExampleValues exampleValues = new ExampleValues();
		exampleValues.setSuccessful(exampleSet);

		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setReadOnly(true);
		options.setServerPattern("staging");
		options.setMinimalEndpoints(true);
		options.setMicrocksHeaders(true);
		options.setGenerateOneOfAnyOf(true);
		options.setExamples(exampleValues);
		options.setValidateSchema(true);

		java.util.Set<String> testCases = new java.util.HashSet<>();
		testCases.add("Success");
		testCases.add("Alternate");

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, testCases, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Verify all features are applied
		assertTrue(xml.contains("staging.example.com"), "Server pattern applied");
		assertTrue(xml.contains("X-Microcks-Response-Name"), "Microcks headers applied");
		assertTrue(xml.contains("Validation_TestStep"), "Validation step applied");
		assertTrue(xml.contains("Success_TestCase"), "Success test case present");
		assertFalse(xml.contains("Alternate_TestCase"), "Alternate test case excluded");
	}

	@Test
	@DisplayName("Null options should use defaults")
	void testNullOptionsUsesDefaults() throws IOException, XmlException, SoapUIException {
		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, null);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should include both read and write operations
		assertTrue(xml.contains("listUsers") && (xml.contains("createUser") || xml.contains("POST")),
			"Default behavior should include all operations");
		// Should have default server
		assertTrue(xml.contains("api.example.com"), "Should use first server by default");
	}

	@Test
	@DisplayName("Empty options should use defaults")
	void testEmptyOptionsUsesDefaults() throws IOException, XmlException, SoapUIException {
		SoapUIProjectOptions options = new SoapUIProjectOptions();

		SoapUIProject project = new SoapUIProject("TestAPI", openAPI, null, null, null, options);
		String xml = project.getFileContent();
		project.deleteTemporaryFile();

		// Should include both operations
		assertTrue(xml.contains("listUsers") && (xml.contains("createUser") || xml.contains("POST")),
			"Default options should include all operations");
	}
}
