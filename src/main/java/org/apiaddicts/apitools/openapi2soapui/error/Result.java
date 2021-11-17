package org.apiaddicts.apitools.openapi2soapui.error;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import lombok.Getter;
import lombok.Setter;
import org.apiaddicts.apitools.openapi2soapui.util.LowerCaseClassNameResolver;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom error response format
 */
@Getter
@Setter
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, property = "error", visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class Result {
	private int responseCode;
    private List<Error> errors;

    private void addSubError(Error subError) {
        if (errors == null) {
        	errors = new ArrayList<>();
        }
        errors.add(subError);
    }

    private void addValidationError(String message) {
        addSubError(new ValidationError(message));
    }

    private void addValidationError(FieldError fieldError) {
        this.addValidationError(fieldError.getDefaultMessage());
    }

    public void addValidationErrors(List<FieldError> fieldErrors) {
        fieldErrors.forEach(this::addValidationError);
    }

    private void addValidationError(ObjectError objectError) {
        this.addValidationError(objectError.getDefaultMessage());
    }

    public void addValidationError(List<ObjectError> globalErrors) {
        globalErrors.forEach(this::addValidationError);
    }

}
