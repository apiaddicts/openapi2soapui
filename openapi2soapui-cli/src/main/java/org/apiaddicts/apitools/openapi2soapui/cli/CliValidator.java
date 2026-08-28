package org.apiaddicts.apitools.openapi2soapui.cli;

import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectRequest;

final class CliValidator {

	private static final String MESSAGE_BUNDLE = "messages";

	private static final Map<String, String> JSON_NAMES = Map.of("openAPIContent", "openApiSpec");

	private CliValidator() {
	}

	static List<String> validate(SoapUIProjectRequest request) {
		ResourceBundleLocator bundleLocator = new PlatformResourceBundleLocator(MESSAGE_BUNDLE);
		try (ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
				.configure()
				.messageInterpolator(new ResourceBundleMessageInterpolator(bundleLocator))
				.buildValidatorFactory()) {
			return factory.getValidator().validate(request).stream()
					.map(CliValidator::describe)
					.sorted()
					.toList();
		}
	}

	private static String describe(ConstraintViolation<?> violation) {
		String property = violation.getPropertyPath().toString();
		property = JSON_NAMES.getOrDefault(property, property);
		String message = violation.getMessage();
		String[] parts = message.split("\\|", 2);
		String text = (parts.length > 1) ? "[" + parts[0] + "] " + parts[1] : message;
		return property.isEmpty() ? text : property + ": " + text;
	}
}
