package org.apiaddicts.apitools.openapi2soapui.request;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Header {
	
	@NotEmpty(message = "{validation.notEmpty.headers.key}")
	@JsonProperty("key")
	private String key;
	
	@NotEmpty(message = "{validation.notEmpty.headers.value}")
	@JsonProperty("value")
	private String value;
}
