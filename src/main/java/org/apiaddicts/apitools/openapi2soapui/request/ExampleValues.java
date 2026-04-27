package org.apiaddicts.apitools.openapi2soapui.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExampleValues {

	@Valid
	@JsonProperty("successful")
	private ExampleSet successful;

	@Valid
	@JsonProperty("wrong")
	private ExampleSet wrong;
}
