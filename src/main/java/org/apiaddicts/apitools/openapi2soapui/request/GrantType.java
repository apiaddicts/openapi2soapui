package org.apiaddicts.apitools.openapi2soapui.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.apiaddicts.apitools.openapi2soapui.error.exceptions.CreateEnumInstanceException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.CreateEnumInstanceException.ErrorType;

public enum GrantType {
	
	AUTHORIZATION_CODE("AUTHORIZATION_CODE"),
	CLIENT_CREDENTIALS("CLIENT_CREDENTIALS"),
	IMPLICIT("IMPLICIT"),
	RESOURCE_OWNER_PASSWORD_CREDENTIALS("RESOURCE_OWNER_PASSWORD_CREDENTIALS");
	
	private String text;
	
	GrantType(String text) {
		this.text = text;
	}
	
	@Override
    public String toString() {
        return text;
    }
	
	@JsonCreator
    public static GrantType create(String value) {
        if (value == null) {
            throw new CreateEnumInstanceException("validation.notNull.oAuth2Profiles.grantType", ErrorType.NOT_NULL, "grantType");
        }
        
        for (GrantType v: values()) {
            if (value.equals(v.getText())) {
                return v;
            }
        }

    	if (value.isBlank()) {
            throw new CreateEnumInstanceException("validation.notBlank.oAuth2Profiles.grantType", ErrorType.NOT_BLANK, "grantType");
        } else {
            throw new CreateEnumInstanceException("validation.invalid.oAuth2Profiles.grantType", ErrorType.INVALID, "grantType", value);
        }
    }

    public String getText() {
        return text;
    }
}
