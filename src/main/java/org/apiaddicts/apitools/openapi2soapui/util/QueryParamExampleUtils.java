package org.apiaddicts.apitools.openapi2soapui.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public final class QueryParamExampleUtils {

	private QueryParamExampleUtils() {
		// Intentional blank
	}

	private static final String DATE_TIME_FORMAT = "date-time";
	private static final String EMAIL_FORMAT = "email";
	private static final String URI_FORMAT = "uri";
	private static final String URL_FORMAT = "url";
	private static final String UUID_FORMAT = "uuid";
	private static final String HOSTNAME_FORMAT = "hostname";
	private static final String IPV4_FORMAT = "ipv4";
	private static final String IPV6_FORMAT = "ipv6";
	private static final String BYTE_FORMAT = "byte";

	public static String validValue(Schema<?> schema) {
		if (schema == null) return "value";
		if (schema instanceof DateSchema) return formatDateExample((DateSchema) schema, "2024-01-01");
		if (schema.getExample() != null) return schema.getExample().toString();
		if (schema.getEnum() != null && !schema.getEnum().isEmpty()) return schema.getEnum().get(0).toString();
		if (schema instanceof StringSchema) return validStringValue((StringSchema) schema);
		if (schema instanceof IntegerSchema || schema instanceof NumberSchema) return "1";
		if (schema instanceof BooleanSchema) return "true";
		if (schema instanceof ArraySchema) return "[]";
		if (schema instanceof ObjectSchema) return "{}";
		return "value";
	}

	public static String invalidValue(Schema<?> schema) {
		if (schema == null) return "badvalue";
		if (schema instanceof StringSchema) return invalidString((StringSchema) schema);
		if (schema instanceof DateSchema) return invalidDate((DateSchema) schema);
		if (schema instanceof IntegerSchema || schema instanceof NumberSchema) return invalidNumber(schema);
		if (schema instanceof BooleanSchema) return "badboolean";
		if (schema instanceof ArraySchema) return "badarray";
		if (schema instanceof ObjectSchema) return "badobject";
		return "badvalue";
	}

	private static String validStringValue(StringSchema schema) {
		String format = schema.getFormat();
		if (DATE_TIME_FORMAT.equalsIgnoreCase(format)) return "2024-01-01T00:00:00Z";
		if (EMAIL_FORMAT.equalsIgnoreCase(format)) return "user@example.com";
		if (URI_FORMAT.equalsIgnoreCase(format) || URL_FORMAT.equalsIgnoreCase(format)) return "https://example.com";
		if (UUID_FORMAT.equalsIgnoreCase(format)) return "3fa85f64-5717-4562-b3fc-2c963f66afa6";
		if (HOSTNAME_FORMAT.equalsIgnoreCase(format)) return "example.com";
		if (IPV4_FORMAT.equalsIgnoreCase(format)) return "192.168.0.1";
		if (IPV6_FORMAT.equalsIgnoreCase(format)) return "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
		if (BYTE_FORMAT.equalsIgnoreCase(format)) return "SGVsbG8gV29ybGQ=";
		return "string";
	}

	private static String invalidString(StringSchema schema) {
		String format = schema.getFormat();
		if (DATE_TIME_FORMAT.equalsIgnoreCase(format)) return invalidDateTime(schema);
		if (EMAIL_FORMAT.equalsIgnoreCase(format)) return "not-an-email";
		if (URI_FORMAT.equalsIgnoreCase(format) || URL_FORMAT.equalsIgnoreCase(format)) return "not a uri";
		if (UUID_FORMAT.equalsIgnoreCase(format)) return "not-a-valid-uuid";
		if (HOSTNAME_FORMAT.equalsIgnoreCase(format)) return "invalid_hostname!";
		if (IPV4_FORMAT.equalsIgnoreCase(format)) return "999.999.999.999";
		if (IPV6_FORMAT.equalsIgnoreCase(format)) return "not:a:valid:ipv6:zzzz";
		if (BYTE_FORMAT.equalsIgnoreCase(format)) return "not_base64!!!";
		Integer maxLength = schema.getMaxLength();
		return (maxLength != null && maxLength > 0) ? "z".repeat(maxLength + 1) : "badstring";
	}

	private static String invalidDateTime(StringSchema schema) {
		Object example = schema.getExample();
		String base = (example != null) ? example.toString() : "2024-01-01T00:00:00Z";
		String withInvalidMonth = base.replaceFirst("^(\\d{4})-\\d{2}", "$1-50");
		return withInvalidMonth.equals(base) ? "baddatetime" : withInvalidMonth;
	}

	private static String invalidNumber(Schema<?> schema) {
		if (schema.getMaximum() != null) return schema.getMaximum().add(BigDecimal.ONE).toString();
		if (schema.getMinimum() != null) return schema.getMinimum().subtract(BigDecimal.ONE).toString();
		return "badnumber";
	}

	private static String invalidDate(DateSchema schema) {
		String base = formatDateExample(schema, "2024-01-01");
		String[] parts = base.split("-");
		if (parts.length == 3) {
			parts[1] = "50";
			return String.join("-", parts);
		}
		return "baddate";
	}

	private static String formatDateExample(DateSchema schema, String fallback) {
		Object example = schema.getExample();
		if (example instanceof Date) return new SimpleDateFormat("yyyy-MM-dd").format((Date) example);
		return (example != null) ? example.toString() : fallback;
	}
}
