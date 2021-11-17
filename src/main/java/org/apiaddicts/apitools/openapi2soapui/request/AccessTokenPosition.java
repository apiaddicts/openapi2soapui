package org.apiaddicts.apitools.openapi2soapui.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.apiaddicts.apitools.openapi2soapui.error.exceptions.CreateEnumInstanceException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.CreateEnumInstanceException.ErrorType;

public enum AccessTokenPosition {
	
	HEADER("HEADER"),
	BODY("BODY"),
	QUERY("QUERY");
	
	private String text;
	
	AccessTokenPosition(String text) {
		this.text = text;
	}
	
	@Override
    public String toString() {
        return text;
    }
	
	@JsonCreator
    public static AccessTokenPosition create(String value) {
        if (value == null) {
            throw new CreateEnumInstanceException("validation.notNull.oAuth2Profiles.accessTokenPosition", ErrorType.NOT_NULL, "accessTokenPosition");
        }
        
        for (AccessTokenPosition v: values()) {
            if (value.equals(v.getText())) {
                return v;
            }
        }

    	if (value.isBlank()) {
            throw new CreateEnumInstanceException("validation.notBlank.oAuth2Profiles.accessTokenPosition", ErrorType.NOT_BLANK, "accessTokenPosition");
        } else {
            throw new CreateEnumInstanceException("validation.invalid.oAuth2Profiles.accessTokenPosition", ErrorType.INVALID, "accessTokenPosition", value);
        }
    }

    public String getText() {
        return text;
    }
}
