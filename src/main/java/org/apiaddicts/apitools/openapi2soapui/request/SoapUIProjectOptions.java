package org.apiaddicts.apitools.openapi2soapui.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoapUIProjectOptions {

	@JsonProperty("readOnly")
	private boolean readOnly = false;

	@JsonProperty("serverPattern")
	private String serverPattern;

	@JsonProperty("minimalEndpoints")
	private boolean minimalEndpoints = false;

	@JsonProperty("microcksHeaders")
	private boolean microcksHeaders = false;

	@JsonProperty("generateOneOfAnyOf")
	private boolean generateOneOfAnyOf = false;

	@Valid
	@JsonProperty("examples")
	private ExampleValues examples;

	@JsonProperty("validateSchema")
	private boolean validateSchema = false;
}
