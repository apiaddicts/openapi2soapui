package org.apiaddicts.apitools.openapi2soapui.error.exceptions;

public class ParseOpenAPIException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4746462920951999746L;

	public ParseOpenAPIException(String errorMessage) {
        super(errorMessage);
	}

}
