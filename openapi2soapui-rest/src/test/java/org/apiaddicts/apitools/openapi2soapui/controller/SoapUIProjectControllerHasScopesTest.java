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
class SoapUIProjectControllerHasScopesTest {

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

	private Map<String, Object> validProfile(String profileName) {
		Map<String, Object> profile = new LinkedHashMap<>();
		profile.put("profileName", profileName);
		profile.put("grantType", "CLIENT_CREDENTIALS");
		profile.put("clientId", "clientId");
		profile.put("clientSecret", "clientSecret");
		profile.put("accessTokenURI", "http://api.example.com/token");
		profile.put("accessTokenPosition", "HEADER");
		profile.put("scope", "openid");
		return profile;
	}

	private Map<String, Object> baseRequestBody() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("apiName", "TestApi");
		body.put("openApiSpec", Base64.getEncoder().encodeToString(SPEC.getBytes(StandardCharsets.UTF_8)));
		return body;
	}

	@Test
	void hasScopesTrueWithMultipleProfiles_numberOfScopesTwo_endToEndGeneratesScopeVariants() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", 2);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("GET_CaseOkScopeDev"))))
				.andExpect(content().string(containsString("GET_CaseOkScopeAdmin")));
	}

	@Test
	void hasScopesOmitted_endToEndGeneratesNoScopeVariants() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("oAuth2Profiles", List.of(validProfile("dev")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("scope "))));
	}

	@Test
	void numberOfScopesCapsScopeVariantsEndToEnd() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", 2);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin"), validProfile("qa")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("GET_CaseOkScopeDev"))))
				.andExpect(content().string(containsString("GET_CaseOkScopeAdmin")))
				.andExpect(content().string(not(containsString("GET_CaseOkScopeQa"))));
	}

	@Test
	void numberOfScopesOmitted_endToEndGeneratesNoExtraVariant() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("GET_CaseOkScopeDev"))))
				.andExpect(content().string(not(containsString("GET_CaseOkScopeAdmin"))));
	}

	@Test
	void numberOfScopesAsNumericString_isCoercedAndCapsCorrectly() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", "1");
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("GET_CaseOkScopeDev"))))
				.andExpect(content().string(not(containsString("GET_CaseOkScopeAdmin"))));
	}

	@Test
	void numberOfScopesAsDecimal_isTruncatedAndCapsCorrectly() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", 1.7);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("GET_CaseOkScopeDev"))))
				.andExpect(content().string(not(containsString("GET_CaseOkScopeAdmin"))));
	}

	@Test
	void numberOfScopesExplicitNull_endToEndGeneratesNoExtraVariant() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", null);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("GET_CaseOkScopeDev"))))
				.andExpect(content().string(not(containsString("GET_CaseOkScopeAdmin"))));
	}

	@Test
	void numberOfScopesNegative_endToEndGeneratesNoExtraVariant() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", -1);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("GET_CaseOkScopeDev"))))
				.andExpect(content().string(not(containsString("GET_CaseOkScopeAdmin"))));
	}

	@Test
	void numberOfScopesVeryLarge_endToEndUsesAllProfiles() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", 999999999);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("GET_CaseOkScopeDev"))))
				.andExpect(content().string(containsString("GET_CaseOkScopeAdmin")));
	}

	@Test
	void numberOfScopesWithHasScopesExplicitFalse_endToEndIgnored() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", false);
		body.put("numberOfScopes", 1);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("scope "))));
	}

	@Test
	void numberOfScopesWithoutHasScopesKeyAtAll_endToEndIgnored() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("numberOfScopes", 1);
		body.put("oAuth2Profiles", List.of(validProfile("dev"), validProfile("admin")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("scope "))));
	}

	@Test
	void numberOfScopesNonNumericString_returns400Gracefully() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", "abc");
		body.put("oAuth2Profiles", List.of(validProfile("dev")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void numberOfScopesBoolean_returns400Gracefully() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", true);
		body.put("oAuth2Profiles", List.of(validProfile("dev")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void numberOfScopesArray_returns400Gracefully() throws Exception {
		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("numberOfScopes", List.of(1, 2));
		body.put("oAuth2Profiles", List.of(validProfile("dev")));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void oAuth2ProfileMissingProfileName_returns400WithValidationErrorCode1201() throws Exception {
		Map<String, Object> profile = validProfile("dev");
		profile.remove("profileName");

		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("oAuth2Profiles", List.of(profile));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result.errors[0].errorCode").value(1201));
	}

	@Test
	void oAuth2ProfileMissingGrantTypeRequiredField_returns400WithValidationErrorCode1208() throws Exception {
		Map<String, Object> profile = validProfile("dev");
		profile.remove("accessTokenURI");

		Map<String, Object> body = baseRequestBody();
		body.put("hasScopes", true);
		body.put("oAuth2Profiles", List.of(profile));

		mockMvc.perform(post(basePath + "/soap-ui-projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result.errors[0].errorCode").value(1208));
	}
}
