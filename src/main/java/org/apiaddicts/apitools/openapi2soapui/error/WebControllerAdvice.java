package org.apiaddicts.apitools.openapi2soapui.error;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import lombok.extern.slf4j.Slf4j;
import org.apiaddicts.apitools.openapi2soapui.constants.Constants;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.APIVersionNotFoundException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.CreateEnumInstanceException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.DecodeBase64Exception;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.ParseOpenAPIException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.SwaggerContentEmptyException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.SwaggerInvalidContentException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.CreateEnumInstanceException.ErrorType;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.ObjectError;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.apiaddicts.apitools.openapi2soapui.error.ObjectErrorTypes.BAD_REQUEST;
import static org.apiaddicts.apitools.openapi2soapui.error.ObjectErrorTypes.INTERNAL_SERVER_ERROR;

/**
 * Exception handler
 */
@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebControllerAdvice {
	private ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
	
	/**
	 * Handle error when parsing Open API Spec content
	 * @param ex Exception
	 * @return custom error response 
	 */
	@ResponseBody
	@ExceptionHandler(ParseOpenAPIException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result handleParseOpenAPIException(ParseOpenAPIException ex) {
		log.debug("ParseOpenAPIException", ex);
		Result result = new Result();
		result.setResponseCode(0);
		List<ObjectError> errors = Collections.singletonList(new ObjectError(BAD_REQUEST, resourceBundle.getString("validation.format.openApiSpec")));
		result.addValidationError(errors);
		return result;
	}

	/**
	 * Handle error when API version not found in Open API Spec
	 * @param ex Exception
	 * @return custom error response 
	 */
	@ResponseBody
	@ExceptionHandler(APIVersionNotFoundException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result handleAPIVersionNotFoundException(APIVersionNotFoundException ex) {
		log.debug("APIVersionNotFoundException", ex);
		Result result = new Result();
		result.setResponseCode(0);
		List<ObjectError> errors = Collections.singletonList(new ObjectError(BAD_REQUEST, resourceBundle.getString("validation.api.version")));
		result.addValidationError(errors);
		return result;
	}

	/**
	 * Handle error when deserializing any property or reading the content of the request body
	 * @param ex Exception
	 * @return custom error response 
	 */
	@ResponseBody
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		log.warn("HttpMessageNotReadableException", ex);
		if (ex.getRootCause() instanceof CreateEnumInstanceException) {
			return handleCreateEnumInstanceException((CreateEnumInstanceException) ex.getRootCause());
		} else if (ex.getRootCause() instanceof SwaggerContentEmptyException) {
			return handleSwaggerContentEmptyException((SwaggerContentEmptyException) ex.getRootCause());
		} else if (ex.getRootCause() instanceof DecodeBase64Exception || ex.getRootCause() instanceof SwaggerInvalidContentException) {
			return handleSwaggerContentException((RuntimeException) ex.getRootCause());
		} else if (ex.getMostSpecificCause() instanceof MismatchedInputException) {
			return handleMismatchedInputException((MismatchedInputException) ex.getMostSpecificCause());
		} else {
			log.debug("Unhandle HttpMessageNotReadableException", ex);
			Result result = new Result();
			result.setResponseCode(0);
			List<ObjectError> errors = Collections.singletonList(new ObjectError(BAD_REQUEST, resourceBundle.getString("validation.malformedJSON")));
			result.addValidationError(errors);
			return result;
		}
	}

	/**
	 * Handle error when input does not match expected
	 * @param ex Exception
	 * @return custom error response
	 */
	private Result handleMismatchedInputException(MismatchedInputException mostSpecificCause) {
		String pathReference =mostSpecificCause.getPathReference();
		String targetType = mostSpecificCause.getTargetType().getName();

		log.debug("pathReference: {} targetType: {}", pathReference, targetType);

		Result result = new Result();
		result.setResponseCode(0);
		List<ObjectError> errors;
		if (pathReference.contains(Constants.TEST_CASE_NAMES_KEY)) {
			errors = getMismatchedInputErrors(Constants.TEST_CASE_NAMES_KEY, targetType);
		} else if (pathReference.contains(Constants.AUTHENTICATION_PROFILES_KEY)) {
			errors = getMismatchedInputErrors(Constants.AUTHENTICATION_PROFILES_KEY, targetType);
		} else if (pathReference.contains(Constants.HEADERS_KEY)) {
			errors = getMismatchedInputErrors(Constants.HEADERS_KEY, targetType);
		} else {
			errors = getDefaultBadRequestErrors();
		}
		result.addValidationError(errors);
		return result;
	}

	/**
	 * List specific property errors when value of property input does not match expected
	 * @param ex Exception
	 * @return custom error response
	 */
	private List<ObjectError> getMismatchedInputErrors(String field, String targetType) {
		if (targetType.contains("String") || targetType.contains("AuthenticationProfile") || targetType.contains("Header")) {
			return Collections.singletonList(new ObjectError(BAD_REQUEST, resourceBundle.getString("validation.serialization."+ field +".item")));
		} else if (targetType.contains("HashSet") || targetType.contains("ArrayList")) {
			return Collections.singletonList(new ObjectError(BAD_REQUEST, resourceBundle.getString("validation.serialization."+ field +".list")));
		} else {
			return getDefaultBadRequestErrors();
		}
	}

	/**
	 * Add default error to error array in response object
	 * @return error array
	 */
	private List<ObjectError> getDefaultBadRequestErrors() {
		return Collections.singletonList(new ObjectError(BAD_REQUEST, resourceBundle.getString("default.badRequest")));
	}

	/**
	 * Handle error when validation of a property by constraint validator is not successful
	 * @param ex Exception
	 * @return custom error response
	 */
	@ResponseBody
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result handleException(MethodArgumentNotValidException ex) {
		log.warn("MethodArgumentNotValidException", ex);
		Result result = new Result();
		result.setResponseCode(0);
		List<ObjectError> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> new ObjectError(BAD_REQUEST, fe.getDefaultMessage()))
				.collect(Collectors.toList());
		result.addValidationError(errors);
		return result;
	}

	/**
	 * Handle unexpected errors
	 * @param ex Exception
	 * @return custom error response
	 */
	@ResponseBody
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Result handleException(Exception ex) {
		log.error("Unexpected exception", ex);
		Result result = new Result();
		result.setResponseCode(0);
		List<ObjectError> errors = Collections.singletonList(new ObjectError(INTERNAL_SERVER_ERROR, resourceBundle.getString("default.internalServerError")));
		result.addValidationError(errors);
		return result;
	}

	/**
	 * Handle error when Open API Spec is empty
	 * @param ex Exception
	 * @return custom error response
	 */
	private Result handleSwaggerContentEmptyException(SwaggerContentEmptyException ex) {
		log.debug("SwaggerContentEmptyException", ex);
		Result result = new Result();
		result.setResponseCode(0);
		List<ObjectError> errors = Collections.singletonList(new ObjectError(BAD_REQUEST, resourceBundle.getString("validation.notEmpty.openApiSpec")));
		result.addValidationError(errors);
		return result;
	}
	
	/**
	 * Handle when Open API Spec has content errors
	 * @param ex Exception
	 * @return custom error response
	 */
	private Result handleSwaggerContentException(RuntimeException ex) {
		log.debug("SwaggerFormatFormatException", ex);
		Result result = new Result();
		result.setResponseCode(0);
		List<ObjectError> errors = Collections.singletonList(new ObjectError(BAD_REQUEST, resourceBundle.getString("validation.content.openApiSpec")));
		result.addValidationError(errors);
		return result;
		
	}

	/**
	 * Handle error when creating enum instance
	 * @param ex Exception
	 * @return custom error response
	 */
	private Result handleCreateEnumInstanceException(CreateEnumInstanceException ex) {
		log.warn("CreateEnumInstanceException", ex);
		Result result = new Result();
		result.setResponseCode(0);
		
		ObjectError error;
		if (ex.getErrorType().equals(ErrorType.NOT_NULL)) {
			error = new ObjectError(BAD_REQUEST, 
					resourceBundle.getString(ex.getMessage()));
		} else if (ex.getErrorType().equals(ErrorType.NOT_BLANK)) {
			error = new ObjectError(BAD_REQUEST, 
					resourceBundle.getString(ex.getMessage()));
		} else if (ex.getErrorType().equals(ErrorType.INVALID)) {
			error = new ObjectError(BAD_REQUEST, 
					MessageFormat.format(resourceBundle.getString(ex.getMessage()), ex.getValue()));
		} else {
			error = new ObjectError(BAD_REQUEST, resourceBundle.getString("default.badRequest"));
		}
		List<ObjectError> errors = Collections.singletonList(error);
		result.addValidationError(errors);
		return result;
	}
}
