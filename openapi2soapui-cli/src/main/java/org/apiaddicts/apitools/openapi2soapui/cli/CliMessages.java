package org.apiaddicts.apitools.openapi2soapui.cli;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

import com.fasterxml.jackson.core.JacksonException;

import org.apiaddicts.apitools.openapi2soapui.error.exceptions.APIVersionNotFoundException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.CreateEnumInstanceException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.CreateEnumInstanceException.ErrorType;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.DecodeBase64Exception;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.ParseOpenAPIException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.SwaggerContentEmptyException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.SwaggerInvalidContentException;

final class CliMessages {

	private static final String BUNDLE = "messages";

	private static final String DEFAULT_BAD_REQUEST = "default.badRequest";

	private CliMessages() {
	}

	static String of(String key) {
		return format(lookup(key));
	}

	static Optional<String> catalogued(Throwable failure) {
		for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
			Optional<String> message = forCause(cause);
			if (message.isPresent()) return message;
		}
		return (failure instanceof JacksonException)
				? Optional.of(of("validation.malformedJSON"))
				: Optional.empty();
	}

	static String format(String message, Object... arguments) {
		String[] parts = message.split("\\|", 2);
		if (parts.length < 2) return message;
		String text = (arguments.length > 0) ? MessageFormat.format(parts[1], arguments) : parts[1];
		return "[" + parts[0] + "] " + text;
	}

	private static Optional<String> forCause(Throwable cause) {
		if (cause instanceof ParseOpenAPIException) return Optional.of(of("validation.format.openApiSpec"));
		if (cause instanceof APIVersionNotFoundException) return Optional.of(of("validation.api.version"));
		if (cause instanceof SwaggerContentEmptyException) return Optional.of(of("validation.notEmpty.openApiSpec"));
		if (cause instanceof DecodeBase64Exception || cause instanceof SwaggerInvalidContentException) {
			return Optional.of(of("validation.content.openApiSpec"));
		}
		if (cause instanceof CreateEnumInstanceException enumFailure) return Optional.of(forEnum(enumFailure));
		return Optional.empty();
	}

	private static String forEnum(CreateEnumInstanceException failure) {
		String message = lookup(failure.getMessage());
		return (failure.getErrorType() == ErrorType.INVALID)
				? format(message, failure.getValue())
				: format(message);
	}

	private static String lookup(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE);
		if (key == null) return bundle.getString(DEFAULT_BAD_REQUEST);
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return bundle.getString(DEFAULT_BAD_REQUEST);
		}
	}
}
