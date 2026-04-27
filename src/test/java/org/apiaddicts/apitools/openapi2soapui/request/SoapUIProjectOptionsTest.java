package org.apiaddicts.apitools.openapi2soapui.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoapUIProjectOptionsTest {

	@Test
	void testDefaultValues() {
		SoapUIProjectOptions options = new SoapUIProjectOptions();

		assertFalse(options.isReadOnly());
		assertNull(options.getServerPattern());
		assertFalse(options.isMinimalEndpoints());
		assertFalse(options.isMicrocksHeaders());
		assertFalse(options.isGenerateOneOfAnyOf());
		assertNull(options.getExamples());
		assertFalse(options.isValidateSchema());
	}

	@Test
	void testSetAllOptions() {
		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setReadOnly(true);
		options.setServerPattern("staging");
		options.setMinimalEndpoints(true);
		options.setMicrocksHeaders(true);
		options.setGenerateOneOfAnyOf(true);
		options.setValidateSchema(true);

		assertTrue(options.isReadOnly());
		assertEquals("staging", options.getServerPattern());
		assertTrue(options.isMinimalEndpoints());
		assertTrue(options.isMicrocksHeaders());
		assertTrue(options.isGenerateOneOfAnyOf());
		assertTrue(options.isValidateSchema());
	}

	@Test
	void testExamplesConfiguration() {
		ExampleSet exampleSet = new ExampleSet();
		exampleSet.setString("test");
		exampleSet.setNumber(42);
		exampleSet.setBooleanValue(false);
		exampleSet.setDate("2026-01-01");
		exampleSet.setDateTime("2026-01-01T00:00:00.000+00:00");

		ExampleValues exampleValues = new ExampleValues();
		exampleValues.setSuccessful(exampleSet);

		SoapUIProjectOptions options = new SoapUIProjectOptions();
		options.setExamples(exampleValues);

		assertNotNull(options.getExamples());
		assertEquals("test", options.getExamples().getSuccessful().getString());
		assertEquals(42, options.getExamples().getSuccessful().getNumber());
		assertFalse(options.getExamples().getSuccessful().getBooleanValue());
	}
}
