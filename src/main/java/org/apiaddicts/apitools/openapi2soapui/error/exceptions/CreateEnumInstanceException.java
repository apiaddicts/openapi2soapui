package org.apiaddicts.apitools.openapi2soapui.error.exceptions;

import lombok.Getter;

@Getter
public class CreateEnumInstanceException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1954631250809380389L;
	
	public enum ErrorType {NOT_NULL, NOT_BLANK, INVALID}
	
	private final ErrorType errorType;
	private final String param;
	private final String value;

	public CreateEnumInstanceException(String errorMessage, ErrorType errorType, String param) {
        super(errorMessage);
		this.errorType = errorType;
		this.param = param;
		this.value = null;
	}

	public CreateEnumInstanceException(String errorMessage, ErrorType errorType, String param, String value) {
        super(errorMessage);
		this.errorType = errorType;
		this.param = param;
		this.value = value;
	}
}
