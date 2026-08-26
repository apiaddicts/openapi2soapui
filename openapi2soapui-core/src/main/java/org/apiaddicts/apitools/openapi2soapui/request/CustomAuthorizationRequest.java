package org.apiaddicts.apitools.openapi2soapui.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomAuthorizationRequest {

	@NotEmpty(message = "{validation.notEmpty.customAuthorizationsFile.name}")
	@JsonProperty("name")
	private String name;

	@NotEmpty(message = "{validation.notEmpty.customAuthorizationsFile.method}")
	@Pattern(regexp = "(?i)GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS", message = "{validation.invalid.customAuthorizationsFile.method}")
	@JsonProperty("method")
	private String method;

	@NotEmpty(message = "{validation.notEmpty.customAuthorizationsFile.endpoint}")
	@JsonProperty("endpoint")
	private String endpoint;

	@Valid
	@JsonProperty("headers")
	private List<Header> headers;

	@JsonProperty("mediaType")
	private String mediaType;

	@JsonProperty("body")
	private String body;
}
