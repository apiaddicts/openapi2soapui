package org.apiaddicts.apitools.openapi2soapui.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SoapUIProjectControllerCustomAuthorizationsFileTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${basepath}")
	private String basePath;

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

	private Map<String, Object> baseRequestBody() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("apiName", "TestApi");
		body.put("openApiSpec", Base64.getEncoder().encodeToString(SPEC.getBytes(StandardCharsets.UTF_8)));
		return body;
	}

	private Map<String, Object> validCustomAuthorization(String name) {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("name", name);
		request.put("method", "POST");
		request.put("endpoint", "https://api.example.com/security/token");
		return request;
	}

	@Test
	void validCustomAuthorizationsFile_endToEndGeneratesAuthorizationsSuiteFirst() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of(validCustomAuthorization("Application token")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("authorizations_TestSuite")))
				.andExpect(content().string(containsString("Application token_TestCase")));
	}

	@Test
	void customAuthorizationsFileOmitted_endToEndUnaffected() throws Exception {
		Map<String, Object> body = baseRequestBody();

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("authorizations_TestSuite"))));
	}

	@Test
	void customAuthorizationsFileEmpty_endToEndNoAuthorizationsSuite() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of());

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("authorizations_TestSuite"))));
	}

	@Test
	void customAuthorizationMissingName_returns400WithValidationErrorCode1501() throws Exception {
		Map<String, Object> customAuthorization = validCustomAuthorization("Application token");
		customAuthorization.remove("name");
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of(customAuthorization));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result.errors[0].errorCode").value(1501));
	}

	@Test
	void customAuthorizationMissingMethod_returns400WithValidationErrorCode1502() throws Exception {
		Map<String, Object> customAuthorization = validCustomAuthorization("Application token");
		customAuthorization.remove("method");
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of(customAuthorization));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result.errors[0].errorCode").value(1502));
	}

	@Test
	void customAuthorizationMissingEndpoint_returns400WithValidationErrorCode1503() throws Exception {
		Map<String, Object> customAuthorization = validCustomAuthorization("Application token");
		customAuthorization.remove("endpoint");
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of(customAuthorization));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result.errors[0].errorCode").value(1503));
	}

	@Test
	void customAuthorizationInvalidMethod_returns400WithValidationErrorCode1504() throws Exception {
		Map<String, Object> customAuthorization = validCustomAuthorization("Application token");
		customAuthorization.put("method", "FOOBAR");
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of(customAuthorization));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result.errors[0].errorCode").value(1504));
	}

	@Test
	void customAuthorizationEmptyMethod_returns400() throws Exception {
		Map<String, Object> customAuthorization = validCustomAuthorization("Application token");
		customAuthorization.put("method", "");
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of(customAuthorization));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void customAuthorizationsFileAsString_returns400WithValidationErrorCode1009() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", "not-an-array");

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result.errors[0].errorCode").value(1009));
	}

	@Test
	void customAuthorizationsFileItemsAsStrings_returns400WithValidationErrorCode1010() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of("not-an-object"));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result.errors[0].errorCode").value(1010));
	}

	@Test
	void multipleCustomAuthorizations_endToEndPreserveGivenOrder() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of(
				validCustomAuthorization("First"),
				validCustomAuthorization("Second")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("First_TestCase")))
				.andExpect(content().string(containsString("Second_TestCase")));
	}

	@Test
	void customAuthorizationWithHeadersAndBody_endToEndAppliesThem() throws Exception {
		Map<String, Object> customAuthorization = validCustomAuthorization("Application token");
		customAuthorization.put("mediaType", "application/x-www-form-urlencoded");
		customAuthorization.put("body", "grant_type=client_credentials");
		customAuthorization.put("headers", List.of(Map.of("key", "X-Custom", "value", "abc123")));
		Map<String, Object> body = baseRequestBody();
		body.put("customAuthorizationsFile", List.of(customAuthorization));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("grant_type=client_credentials")))
				.andExpect(content().string(containsString("abc123")));
	}
}
