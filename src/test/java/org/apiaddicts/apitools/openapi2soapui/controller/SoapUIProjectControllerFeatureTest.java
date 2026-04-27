package org.apiaddicts.apitools.openapi2soapui.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class SoapUIProjectControllerFeatureTest {

	@Autowired
	private MockMvc mockMvc;

	private static final String SIMPLE_OPENAPI = "openapi: 3.0.0\n" +
		"info:\n" +
		"  title: Test API\n" +
		"  version: 1.0.0\n" +
		"servers:\n" +
		"  - url: http://api.example.com/v1\n" +
		"    description: Production\n" +
		"  - url: http://staging.example.com/v1\n" +
		"    description: Staging\n" +
		"paths:\n" +
		"  /users:\n" +
		"    get:\n" +
		"      operationId: listUsers\n" +
		"      responses:\n" +
		"        '200':\n" +
		"          description: Success\n" +
		"          content:\n" +
		"            application/json:\n" +
		"              schema:\n" +
		"                type: array\n" +
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

	@Test
	void testReadOnlyFeature() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\",\n" +
			"  \"options\": {\n" +
			"    \"readOnly\": true\n" +
			"  }\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
			.andExpect(result -> {
				String response = result.getResponse().getContentAsString();
				// POST method should not be included when readOnly is true
				assert response.contains("GET");
				assert !response.contains("POST");
			});
	}

	@Test
	void testServerPatternFeature() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\",\n" +
			"  \"options\": {\n" +
			"    \"serverPattern\": \"staging\"\n" +
			"  }\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
			.andExpect(result -> {
				String response = result.getResponse().getContentAsString();
				// Should contain staging server
				assert response.contains("staging.example.com");
			});
	}

	@Test
	void testMinimalEndpointsFeature() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\",\n" +
			"  \"testCaseNames\": [\"Success\", \"ErrorCase\"],\n" +
			"  \"options\": {\n" +
			"    \"minimalEndpoints\": true\n" +
			"  }\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
			.andExpect(result -> {
				String response = result.getResponse().getContentAsString();
				// Should only have Success_TestCase, not ErrorCase_TestCase
				assert response.contains("Success_TestCase");
				assert !response.contains("ErrorCase_TestCase");
			});
	}

	@Test
	void testMicrocksHeadersFeature() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\",\n" +
			"  \"options\": {\n" +
			"    \"microcksHeaders\": true\n" +
			"  }\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
			.andExpect(result -> {
				String response = result.getResponse().getContentAsString();
				// Should contain X-Microcks-Response-Name header
				assert response.contains("X-Microcks-Response-Name");
				assert response.contains("listUsers") || response.contains("createUser");
			});
	}

	@Test
	void testValidateSchemaFeature() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\",\n" +
			"  \"options\": {\n" +
			"    \"validateSchema\": true\n" +
			"  }\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
			.andExpect(result -> {
				String response = result.getResponse().getContentAsString();
				// Should contain Validation_TestStep for Groovy script
				assert response.contains("Validation_TestStep");
				assert response.contains("groovy");
			});
	}

	@Test
	void testCustomExamplesFeature() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\",\n" +
			"  \"options\": {\n" +
			"    \"examples\": {\n" +
			"      \"successful\": {\n" +
			"        \"string\": \"custom_value\",\n" +
			"        \"number\": 99,\n" +
			"        \"boolean\": false,\n" +
			"        \"date\": \"2025-12-31\",\n" +
			"        \"dateTime\": \"2025-12-31T23:59:59.000+00:00\"\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
	}

	@Test
	void testMultipleOptionsEnabled() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\",\n" +
			"  \"options\": {\n" +
			"    \"readOnly\": true,\n" +
			"    \"serverPattern\": \"staging\",\n" +
			"    \"microcksHeaders\": true,\n" +
			"    \"validateSchema\": true\n" +
			"  }\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
			.andExpect(result -> {
				String response = result.getResponse().getContentAsString();
				// Should have all features
				assert response.contains("staging.example.com");
				assert !response.contains("POST");
				assert response.contains("X-Microcks-Response-Name");
				assert response.contains("Validation_TestStep");
			});
	}

	@Test
	void testNullOptionsHandling() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\"\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
			.andExpect(result -> {
				String response = result.getResponse().getContentAsString();
				// Should behave like default (includes POST, etc.)
				assert response.contains("POST");
				assert response.contains("GET");
			});
	}

	@Test
	void testEmptyOptionsHandling() throws Exception {
		String encodedSpec = Base64.getEncoder().encodeToString(SIMPLE_OPENAPI.getBytes());
		String requestBody = "{\n" +
			"  \"apiName\": \"TestAPI\",\n" +
			"  \"openApiSpec\": \"" + encodedSpec + "\",\n" +
			"  \"options\": {}\n" +
			"}";

		mockMvc.perform(post("/api-openapi-to-soapui/v1/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
			.andExpect(result -> {
				String response = result.getResponse().getContentAsString();
				// Should behave like default
				assert response.contains("POST");
				assert response.contains("GET");
			});
	}
}
