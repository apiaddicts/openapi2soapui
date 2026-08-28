package org.apiaddicts.apitools.openapi2soapui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;

import org.apiaddicts.apitools.openapi2soapui.request.ExampleValues;

class QueryParamExampleUtilsTest {

	@Test
	void invalidValue_returnsGenericValue_whenSchemaIsNull() {
		assertEquals("badvalue", QueryParamExampleUtils.invalidValue(null, null));
	}

	@Test
	void invalidValue_padsToMaxLengthPlusOne_whenStringHasMaxLength() {
		StringSchema schema = new StringSchema();
		schema.setMaxLength(5);
		assertEquals(6, QueryParamExampleUtils.invalidValue(schema, null).length());
	}

	@Test
	void invalidValue_returnsGenericString_whenStringHasNoMaxLength() {
		StringSchema schema = new StringSchema();
		assertEquals("badstring", QueryParamExampleUtils.invalidValue(schema, null));
	}

	@Test
	void invalidValue_exceedsMaximum_whenIntegerHasMaximum() {
		IntegerSchema schema = new IntegerSchema();
		schema.setMaximum(BigDecimal.TEN);
		assertEquals("11", QueryParamExampleUtils.invalidValue(schema, null));
	}

	@Test
	void invalidValue_goesBelowMinimum_whenNumberHasOnlyMinimum() {
		NumberSchema schema = new NumberSchema();
		schema.setMinimum(BigDecimal.ZERO);
		assertEquals("-1", QueryParamExampleUtils.invalidValue(schema, null));
	}

	@Test
	void invalidValue_returnsGenericNumber_whenNoBoundsDefined() {
		IntegerSchema schema = new IntegerSchema();
		assertEquals("badnumber", QueryParamExampleUtils.invalidValue(schema, null));
	}

	@Test
	void invalidValue_returnsBadBoolean_forBooleanSchema() {
		assertEquals("badboolean", QueryParamExampleUtils.invalidValue(new BooleanSchema(), null));
	}

	@Test
	void invalidValue_returnsBadArray_forArraySchema() {
		assertEquals("badarray", QueryParamExampleUtils.invalidValue(new ArraySchema(), null));
	}

	@Test
	void invalidValue_returnsBadObject_forObjectSchema() {
		assertEquals("badobject", QueryParamExampleUtils.invalidValue(new ObjectSchema(), null));
	}

	@Test
	void invalidValue_usesConfiguredOverride_forArraySchema() {
		ExampleValues wrong = new ExampleValues();
		wrong.setArray("[1,2,3]");
		assertEquals("[1,2,3]", QueryParamExampleUtils.invalidValue(new ArraySchema(), wrong));
	}

	@Test
	void invalidValue_usesConfiguredOverride_forObjectSchema() {
		ExampleValues wrong = new ExampleValues();
		wrong.setObject("{\"bad\":true}");
		assertEquals("{\"bad\":true}", QueryParamExampleUtils.invalidValue(new ObjectSchema(), wrong));
	}

	@Test
	void validValue_returnsEmptyArrayLiteral_forArraySchemaByDefault() {
		assertEquals("[]", QueryParamExampleUtils.validValue(new ArraySchema(), null));
	}

	@Test
	void validValue_usesConfiguredOverride_forArraySchema() {
		ExampleValues successful = new ExampleValues();
		successful.setArray("[\"a\",\"b\"]");
		assertEquals("[\"a\",\"b\"]", QueryParamExampleUtils.validValue(new ArraySchema(), successful));
	}

	@Test
	void validValue_returnsEmptyObjectLiteral_forObjectSchemaByDefault() {
		assertEquals("{}", QueryParamExampleUtils.validValue(new ObjectSchema(), null));
	}

	@Test
	void validValue_usesConfiguredOverride_forObjectSchema() {
		ExampleValues successful = new ExampleValues();
		successful.setObject("{\"id\":1}");
		assertEquals("{\"id\":1}", QueryParamExampleUtils.validValue(new ObjectSchema(), successful));
	}

	@Test
	void invalidValue_setsInvalidMonth_forDateSchema() {
		DateSchema schema = new DateSchema();
		schema.setExample("2024-01-01");
		String[] parts = QueryParamExampleUtils.invalidValue(schema, null).split("-");
		assertEquals(3, parts.length);
		assertEquals("50", parts[1]);
	}

	@Test
	void invalidValue_setsInvalidMonth_forDateSchemaWithoutExample() {
		String[] parts = QueryParamExampleUtils.invalidValue(new DateSchema(), null).split("-");
		assertEquals(3, parts.length);
		assertEquals("50", parts[1]);
	}

	@Test
	void validValue_returnsGenericValue_whenSchemaIsNull() {
		assertEquals("value", QueryParamExampleUtils.validValue(null, null));
	}

	@Test
	void validValue_returnsExample_whenSchemaHasExample() {
		StringSchema schema = new StringSchema();
		schema.setExample("foo");
		assertEquals("foo", QueryParamExampleUtils.validValue(schema, null));
	}

	@Test
	void validValue_returnsTypedDefault_forEachSchemaType() {
		assertEquals("string", QueryParamExampleUtils.validValue(new StringSchema(), null));
		assertTrue(QueryParamExampleUtils.validValue(new IntegerSchema(), null).matches("\\d+"));
		assertEquals("true", QueryParamExampleUtils.validValue(new BooleanSchema(), null));
	}

	@Test
	void validValue_returnsFirstEnumValue_whenSchemaHasEnumAndNoExample() {
		StringSchema schema = new StringSchema();
		schema.setEnum(java.util.List.of("asc", "desc"));
		assertEquals("asc", QueryParamExampleUtils.validValue(schema, null));
	}

	@Test
	void validValue_prefersExampleOverEnum() {
		StringSchema schema = new StringSchema();
		schema.setEnum(java.util.List.of("asc", "desc"));
		schema.setExample("desc");
		assertEquals("desc", QueryParamExampleUtils.validValue(schema, null));
	}



	@Test
	void invalidValue_setsInvalidMonth_forDateTimeFormattedStringWithExample() {
		StringSchema schema = new StringSchema();
		schema.setFormat("date-time");
		schema.setExample("2025-06-15T10:30:00Z");
		assertEquals("2025-50-15T10:30:00Z", QueryParamExampleUtils.invalidValue(schema, null));
	}


	@Test
	void invalidValue_hasNoAtSign_forEmailFormat() {
		StringSchema schema = new StringSchema();
		schema.setFormat("email");
		assertTrue(!QueryParamExampleUtils.invalidValue(schema, null).contains("@"));
	}







	@Test
	void formatAwareness_stillYieldsToExplicitExample() {
		StringSchema schema = new StringSchema();
		schema.setFormat("email");
		schema.setExample("custom@example.org");
		assertEquals("custom@example.org", QueryParamExampleUtils.validValue(schema, null));
	}

	@Test
	void validValue_usesConfiguredSuccessfulExample_whenGenericStringHasNoFormat() {
		ExampleValues successful = new ExampleValues();
		successful.setString("goodstring");
		assertEquals("goodstring", QueryParamExampleUtils.validValue(new StringSchema(), successful));
	}

	@Test
	void invalidValue_usesConfiguredWrongExample_whenGenericStringHasNoMaxLength() {
		ExampleValues wrong = new ExampleValues();
		wrong.setString("badstring-configured");
		assertEquals("badstring-configured", QueryParamExampleUtils.invalidValue(new StringSchema(), wrong));
	}

	@Test
	void invalidValue_ignoresConfiguredWrongExample_whenMaxLengthDefined() {
		StringSchema schema = new StringSchema();
		schema.setMaxLength(3);
		ExampleValues wrong = new ExampleValues();
		wrong.setString("badstring-configured");
		assertEquals(4, QueryParamExampleUtils.invalidValue(schema, wrong).length());
	}

	@Test
	void validValue_usesConfiguredSuccessfulNumber() {
		ExampleValues successful = new ExampleValues();
		successful.setNumber(BigDecimal.valueOf(6));
		assertEquals("6", QueryParamExampleUtils.validValue(new IntegerSchema(), successful));
	}

	@Test
	void invalidValue_usesConfiguredWrongNumber_whenNoBoundsDefined() {
		ExampleValues wrong = new ExampleValues();
		wrong.setNumber(BigDecimal.valueOf(-6));
		assertEquals("-6", QueryParamExampleUtils.invalidValue(new IntegerSchema(), wrong));
	}

	@Test
	void validValue_usesConfiguredSuccessfulBoolean() {
		ExampleValues successful = new ExampleValues();
		successful.setBooleanValue(Boolean.FALSE);
		assertEquals("false", QueryParamExampleUtils.validValue(new BooleanSchema(), successful));
	}

	@Test
	void validValue_usesConfiguredSuccessfulDate() {
		ExampleValues successful = new ExampleValues();
		successful.setDate("2020-01-01");
		assertEquals("2020-01-01", QueryParamExampleUtils.validValue(new DateSchema(), successful));
	}

	@Test
	void invalidValue_usesConfiguredWrongDateDirectly_whenNoSchemaExample() {
		ExampleValues wrong = new ExampleValues();
		wrong.setDate("2020-40-40");
		assertEquals("2020-40-40", QueryParamExampleUtils.invalidValue(new DateSchema(), wrong));
	}

	@Test
	void validValue_usesConfiguredSuccessfulDateTime() {
		ExampleValues successful = new ExampleValues();
		successful.setDateTime("2020-01-01T23:59:59");
		StringSchema schema = new StringSchema();
		schema.setFormat("date-time");
		assertEquals("2020-01-01T23:59:59", QueryParamExampleUtils.validValue(schema, successful));
	}

	@Test
	void invalidValue_usesConfiguredWrongDateTimeDirectly_whenNoSchemaExample() {
		ExampleValues wrong = new ExampleValues();
		wrong.setDateTime("2020-40-40T00:00:00");
		StringSchema schema = new StringSchema();
		schema.setFormat("date-time");
		assertEquals("2020-40-40T00:00:00", QueryParamExampleUtils.invalidValue(schema, wrong));
	}

	@Test
	void validValue_returnsIsoDateTime_forDateTimeSchemaSubtype() {
		assertEquals("2024-01-01T00:00:00Z", QueryParamExampleUtils.validValue(new DateTimeSchema(), null));
	}

	@Test
	void validValue_usesConfiguredSuccessfulDateTime_forDateTimeSchemaSubtype() {
		ExampleValues successful = new ExampleValues();
		successful.setDateTime("2020-01-01T23:59:59");
		assertEquals("2020-01-01T23:59:59", QueryParamExampleUtils.validValue(new DateTimeSchema(), successful));
	}

	@Test
	void invalidValue_setsInvalidMonth_forDateTimeSchemaSubtype() {
		assertEquals("2024-50-01T00:00:00Z", QueryParamExampleUtils.invalidValue(new DateTimeSchema(), null));
	}

	@Test
	void invalidValue_usesConfiguredWrongDateTime_forDateTimeSchemaSubtype() {
		ExampleValues wrong = new ExampleValues();
		wrong.setDateTime("2020-40-40T00:00:00");
		assertEquals("2020-40-40T00:00:00", QueryParamExampleUtils.invalidValue(new DateTimeSchema(), wrong));
	}

	@Test
	void validValue_returnsRealisticValue_forEmailSchemaSubtype() {
		assertEquals("user@example.com", QueryParamExampleUtils.validValue(new EmailSchema(), null));
	}

	@Test
	void invalidValue_hasNoAtSign_forEmailSchemaSubtype() {
		assertTrue(!QueryParamExampleUtils.invalidValue(new EmailSchema(), null).contains("@"));
	}

	@Test
	void validValue_returnsRealisticValue_forUuidSchemaSubtype() {
		assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa6", QueryParamExampleUtils.validValue(new UUIDSchema(), null));
	}

	@Test
	void invalidValue_isNotUuidShaped_forUuidSchemaSubtype() {
		assertEquals("not-a-valid-uuid", QueryParamExampleUtils.invalidValue(new UUIDSchema(), null));
	}

	@ParameterizedTest
	@CsvSource({
			"date-time, 2024-01-01T00:00:00Z",
			"email,     user@example.com",
			"uri,       https://example.com",
			"uuid,      3fa85f64-5717-4562-b3fc-2c963f66afa6",
			"ipv4,      192.0.2.1",
			"byte,      SGVsbG8gV29ybGQ="
	})
	void validValue_returnsRealisticValue_forFormat(String format, String expected) {
		StringSchema schema = new StringSchema();
		schema.setFormat(format);
		assertEquals(expected, QueryParamExampleUtils.validValue(schema, null));
	}

	@ParameterizedTest
	@CsvSource({
			"date-time, 2024-50-01T00:00:00Z",
			"uuid,      not-a-valid-uuid",
			"ipv4,      999.999.999.999"
	})
	void invalidValue_returnsRecognizablyInvalidValue_forFormat(String format, String expected) {
		StringSchema schema = new StringSchema();
		schema.setFormat(format);
		assertEquals(expected, QueryParamExampleUtils.invalidValue(schema, null));
	}

}
