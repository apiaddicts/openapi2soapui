package org.apiaddicts.apitools.openapi2soapui.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExampleSet {

	@JsonProperty("string")
	private String string;

	@JsonProperty("number")
	private Number number;

	@JsonProperty("boolean")
	private Boolean booleanValue;

	@JsonProperty("date")
	private String date;

	@JsonProperty("dateTime")
	private String dateTime;
}
