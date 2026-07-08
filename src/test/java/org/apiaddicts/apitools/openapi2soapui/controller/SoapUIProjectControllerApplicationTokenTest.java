package org.apiaddicts.apitools.openapi2soapui.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class SoapUIProjectControllerApplicationTokenTest {

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

	private Map<String, Object> clientCredentialsProfile(String profileName) {
		Map<String, Object> profile = new LinkedHashMap<>();
		profile.put("profileName", profileName);
		profile.put("grantType", "CLIENT_CREDENTIALS");
		profile.put("clientId", "clientId");
		profile.put("clientSecret", "clientSecret");
		profile.put("accessTokenURI", "http://api.example.com/token");
		profile.put("accessTokenPosition", "HEADER");
		return profile;
	}

	private Map<String, Object> authorizationCodeProfile(String profileName) {
		Map<String, Object> profile = new LinkedHashMap<>();
		profile.put("profileName", profileName);
		profile.put("grantType", "AUTHORIZATION_CODE");
		profile.put("clientId", "clientId");
		profile.put("clientSecret", "clientSecret");
		profile.put("accessTokenURI", "http://api.example.com/token");
		profile.put("authorizationURI", "http://api.example.com/authorize");
		profile.put("redirectURI", "http://localhost/callback");
		profile.put("accessTokenPosition", "HEADER");
		return profile;
	}

	private Map<String, Object> baseRequestBody() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("apiName", "TestApi");
		body.put("openApiSpec", Base64.getEncoder().encodeToString(SPEC.getBytes(StandardCharsets.UTF_8)));
		return body;
	}

	@Test
	void hasScopesAndApplicationTokenTrue_endToEndGeneratesBothVariants() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("applicationToken", true);
		body.put("numberOfScopes", 2);
		body.put("oAuth2Profiles", List.of(clientCredentialsProfile("dev"), authorizationCodeProfile("user")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("application_token dev_TestCase")))
				.andExpect(content().string(not(containsString("application_token user_TestCase"))))
				.andExpect(content().string(containsString("scope dev_TestCase")))
				.andExpect(content().string(containsString("scope user_TestCase")));
	}

	@Test
	void applicationTokenTrueWithHasScopesFalse_endToEndGeneratesNoApplicationTokenVariant() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("applicationToken", true);
		body.put("oAuth2Profiles", List.of(clientCredentialsProfile("dev")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("application_token "))));
	}

	@Test
	void applicationTokenOmitted_endToEndGeneratesOnlyScopeVariants() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("oAuth2Profiles", List.of(clientCredentialsProfile("dev")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("application_token "))))
				.andExpect(content().string(containsString("scope dev_TestCase")));
	}
}
