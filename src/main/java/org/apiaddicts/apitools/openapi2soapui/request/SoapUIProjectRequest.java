package org.apiaddicts.apitools.openapi2soapui.request;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Getter;
import lombok.Setter;
import org.apiaddicts.apitools.openapi2soapui.util.SwaggerContentDeserializer;

@Getter
@Setter
public class SoapUIProjectRequest {

	@NotEmpty(message = "{validation.notEmpty.apiName}")
	@JsonProperty("apiName")
	private String apiName;

	@Valid
	@JsonProperty("oAuth2Profiles")
    private List<OAuth2Profile> oAuth2Profiles;

	@JsonProperty("openApiSpec")
	@NotEmpty(message = "{validation.notEmpty.openApiSpec}")
    @JsonDeserialize(using = SwaggerContentDeserializer.class)
    private String openAPIContent;
	
	@Valid
	@JsonProperty("testCaseNames")
	private Set<@NotEmpty(message = "{validation.notEmpty.testCaseNames.item}") String> testCaseNames;
	
	@Valid
	@JsonProperty("headers")
	private List<Header> headers;
}
