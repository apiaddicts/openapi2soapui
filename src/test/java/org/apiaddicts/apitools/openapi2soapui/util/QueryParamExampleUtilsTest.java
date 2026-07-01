package org.apiaddicts.apitools.openapi2soapui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;

class QueryParamExampleUtilsTest {

	@Test
	void invalidValue_returnsGenericValue_whenSchemaIsNull() {
		assertEquals("badvalue", QueryParamExampleUtils.invalidValue(null));
	}

	@Test
	void invalidValue_padsToMaxLengthPlusOne_whenStringHasMaxLength() {
		StringSchema schema = new StringSchema();
		schema.setMaxLength(5);
		assertEquals(6, QueryParamExampleUtils.invalidValue(schema).length());
	}

	@Test
	void invalidValue_returnsGenericString_whenStringHasNoMaxLength() {
		StringSchema schema = new StringSchema();
		assertEquals("badstring", QueryParamExampleUtils.invalidValue(schema));
	}

	@Test
	void invalidValue_exceedsMaximum_whenIntegerHasMaximum() {
		IntegerSchema schema = new IntegerSchema();
		schema.setMaximum(BigDecimal.TEN);
		assertEquals("11", QueryParamExampleUtils.invalidValue(schema));
	}

	@Test
	void invalidValue_goesBelowMinimum_whenNumberHasOnlyMinimum() {
		NumberSchema schema = new NumberSchema();
		schema.setMinimum(BigDecimal.ZERO);
		assertEquals("-1", QueryParamExampleUtils.invalidValue(schema));
	}

	@Test
	void invalidValue_returnsGenericNumber_whenNoBoundsDefined() {
		IntegerSchema schema = new IntegerSchema();
		assertEquals("badnumber", QueryParamExampleUtils.invalidValue(schema));
	}

	@Test
	void invalidValue_returnsBadBoolean_forBooleanSchema() {
		assertEquals("badboolean", QueryParamExampleUtils.invalidValue(new BooleanSchema()));
	}

	@Test
	void invalidValue_returnsBadArray_forArraySchema() {
		assertEquals("badarray", QueryParamExampleUtils.invalidValue(new ArraySchema()));
	}

	@Test
	void invalidValue_returnsBadObject_forObjectSchema() {
		assertEquals("badobject", QueryParamExampleUtils.invalidValue(new ObjectSchema()));
	}

	@Test
	void invalidValue_setsInvalidMonth_forDateSchema() {
		DateSchema schema = new DateSchema();
		schema.setExample("2024-01-01");
		String[] parts = QueryParamExampleUtils.invalidValue(schema).split("-");
		assertEquals(3, parts.length);
		assertEquals("50", parts[1]);
	}

	@Test
	void invalidValue_setsInvalidMonth_forDateSchemaWithoutExample() {
		String[] parts = QueryParamExampleUtils.invalidValue(new DateSchema()).split("-");
		assertEquals(3, parts.length);
		assertEquals("50", parts[1]);
	}

	@Test
	void validValue_returnsGenericValue_whenSchemaIsNull() {
		assertEquals("value", QueryParamExampleUtils.validValue(null));
	}

	@Test
	void validValue_returnsExample_whenSchemaHasExample() {
		StringSchema schema = new StringSchema();
		schema.setExample("foo");
		assertEquals("foo", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void validValue_returnsTypedDefault_forEachSchemaType() {
		assertEquals("string", QueryParamExampleUtils.validValue(new StringSchema()));
		assertTrue(QueryParamExampleUtils.validValue(new IntegerSchema()).matches("\\d+"));
		assertEquals("true", QueryParamExampleUtils.validValue(new BooleanSchema()));
	}

	@Test
	void validValue_returnsFirstEnumValue_whenSchemaHasEnumAndNoExample() {
		StringSchema schema = new StringSchema();
		schema.setEnum(java.util.List.of("asc", "desc"));
		assertEquals("asc", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void validValue_prefersExampleOverEnum() {
		StringSchema schema = new StringSchema();
		schema.setEnum(java.util.List.of("asc", "desc"));
		schema.setExample("desc");
		assertEquals("desc", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void validValue_returnsIsoDateTime_forDateTimeFormattedString() {
		StringSchema schema = new StringSchema();
		schema.setFormat("date-time");
		assertEquals("2024-01-01T00:00:00Z", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void invalidValue_setsInvalidMonth_forDateTimeFormattedString() {
		StringSchema schema = new StringSchema();
		schema.setFormat("date-time");
		assertEquals("2024-50-01T00:00:00Z", QueryParamExampleUtils.invalidValue(schema));
	}

	@Test
	void invalidValue_setsInvalidMonth_forDateTimeFormattedStringWithExample() {
		StringSchema schema = new StringSchema();
		schema.setFormat("date-time");
		schema.setExample("2025-06-15T10:30:00Z");
		assertEquals("2025-50-15T10:30:00Z", QueryParamExampleUtils.invalidValue(schema));
	}

	@Test
	void validValue_returnsRealisticValue_forEmailFormat() {
		StringSchema schema = new StringSchema();
		schema.setFormat("email");
		assertEquals("user@example.com", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void invalidValue_hasNoAtSign_forEmailFormat() {
		StringSchema schema = new StringSchema();
		schema.setFormat("email");
		assertTrue(!QueryParamExampleUtils.invalidValue(schema).contains("@"));
	}

	@Test
	void validValue_returnsRealisticValue_forUriFormat() {
		StringSchema schema = new StringSchema();
		schema.setFormat("uri");
		assertEquals("https://example.com", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void validValue_returnsRealisticValue_forUuidFormat() {
		StringSchema schema = new StringSchema();
		schema.setFormat("uuid");
		assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa6", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void invalidValue_isNotUuidShaped_forUuidFormat() {
		StringSchema schema = new StringSchema();
		schema.setFormat("uuid");
		assertEquals("not-a-valid-uuid", QueryParamExampleUtils.invalidValue(schema));
	}

	@Test
	void validValue_returnsRealisticValue_forIpv4Format() {
		StringSchema schema = new StringSchema();
		schema.setFormat("ipv4");
		assertEquals("192.168.0.1", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void invalidValue_hasOutOfRangeOctets_forIpv4Format() {
		StringSchema schema = new StringSchema();
		schema.setFormat("ipv4");
		assertEquals("999.999.999.999", QueryParamExampleUtils.invalidValue(schema));
	}

	@Test
	void validValue_returnsRealisticValue_forByteFormat() {
		StringSchema schema = new StringSchema();
		schema.setFormat("byte");
		assertEquals("SGVsbG8gV29ybGQ=", QueryParamExampleUtils.validValue(schema));
	}

	@Test
	void formatAwareness_stillYieldsToExplicitExample() {
		StringSchema schema = new StringSchema();
		schema.setFormat("email");
		schema.setExample("custom@example.org");
		assertEquals("custom@example.org", QueryParamExampleUtils.validValue(schema));
	}
}
