package org.apiaddicts.apitools.openapi2soapui.request;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamplesConfig {

	@Valid
	@JsonProperty("successful")
	private ExampleValues successful;

	@Valid
	@JsonProperty("wrong")
	private ExampleValues wrong;
}
