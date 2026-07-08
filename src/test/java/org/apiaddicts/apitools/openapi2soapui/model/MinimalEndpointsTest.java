package org.apiaddicts.apitools.openapi2soapui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apiaddicts.apitools.openapi2soapui.request.ExampleValues;
import org.apiaddicts.apitools.openapi2soapui.request.ExamplesConfig;
import org.junit.jupiter.api.Test;

/**
 * minimalEndpoints targets the JSON request body (missing required properties and invalid property
 * values), not query parameters
 */
class MinimalEndpointsTest {

	private static final String FLAT_REQUIRED_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    post:",
			"      operationId: createItem",
			"      requestBody:",
			"        content:",
			"          application/json:",
			"            schema:",
			"              type: object",
			"              required: [name, age]",
			"              properties:",
			"                name:",
			"                  type: string",
			"                age:",
			"                  type: integer",
			"                nickname:",
			"                  type: string",
			"      responses:",
			"        '200':",
			"          description: OK"
	);

	private static final String NESTED_REQUIRED_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    post:",
			"      operationId: createItem",
			"      requestBody:",
			"        content:",
			"          application/json:",
			"            schema:",
			"              type: object",
			"              required: [address]",
			"              properties:",
			"                address:",
			"                  type: object",
			"                  required: [street]",
			"                  properties:",
			"                    street:",
			"                      type: string",
			"      responses:",
			"        '200':",
			"          description: OK"
	);

	private static final String NO_BODY_SPEC = String.join("\n",
			"openapi: 3.0.0",
			"info:",
			"  title: Test",
			"  version: '1.0'",
			"paths:",
			"  /items:",
			"    get:",
			"      operationId: getItems",
			"      responses:",
			"        '200':",
			"          description: OK"
	);

	private OpenAPI parseSpec(String yaml) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(yaml, null, null);
		assertTrue(result.getMessages().isEmpty(), "Spec should parse without errors: " + result.getMessages());
		return result.getOpenAPI();
	}

	private int countOccurrences(String haystack, String needle) {
		int count = 0;
		int index = 0;
		while ((index = haystack.indexOf(needle, index)) != -1) {
			count++;
			index += needle.length();
		}
		return count;
	}

	private String decode(String xml) {
		String d = xml.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&amp;", "&");
		// Collapse the pretty-printed JSON body's newlines+indentation only; leaves genuine inline
		// spaces (e.g. inside "missing age", or between XML attributes) untouched
		return d.replaceAll("\\r?\\n\\s*", "");
	}

	@Test
	void minimalEndpointsFalse_generatesOneMissingVariantPerRequiredPropertyAndOneWrongVariantPerProperty() throws Exception {
		OpenAPI openAPI = parseSpec(FLAT_REQUIRED_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("missing name_TestCase"), xml);
		assertTrue(xml.contains("missing age_TestCase"), xml);
		assertTrue(xml.contains("wrong name_TestCase"), xml);
		assertTrue(xml.contains("wrong age_TestCase"), xml);
		assertTrue(xml.contains("wrong nickname_TestCase"), "nickname is not required, but must still get a wrong-value variant: " + xml);
		// Default + 2 missing + 3 wrong
		assertEquals(6, countOccurrences(xml, "<con:testCase"));
		assertEquals(5, countOccurrences(xml, "<codes>400</codes>"), "Each of the 5 variants must carry a status-code assertion expecting 400");
	}

	@Test
	void minimalEndpointsTrue_collapsesToAtMostOneMissingVariantAndNoWrongVariants() throws Exception {
		OpenAPI openAPI = parseSpec(FLAT_REQUIRED_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, null);
		String xml = soapUIProject.getFileContent();

		// Exactly one of the two required properties gets a variant (the first found in declared order);
		// which one survives is an implementation detail, so this asserts the count rather than the name
		boolean missingName = xml.contains("missing name_TestCase");
		boolean missingAge = xml.contains("missing age_TestCase");
		assertTrue(missingName ^ missingAge, "Exactly one missing-required variant must be generated, not zero or both: " + xml);
		assertFalse(xml.contains("wrong "), "minimalEndpoints=true generates zero wrong-value variants: " + xml);
		assertEquals(2, countOccurrences(xml, "<con:testCase"), "Default + 1 missing-required variant");
	}

	@Test
	void noRequestBody_generatesNoBodyPropertyVariantsRegardlessOfMinimalEndpoints() throws Exception {
		OpenAPI openAPI = parseSpec(NO_BODY_SPEC);

		SoapUIProject falseVariant = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, null);
		assertFalse(falseVariant.getFileContent().contains("missing "), falseVariant.getFileContent());
		assertFalse(falseVariant.getFileContent().contains("wrong "), falseVariant.getFileContent());

		SoapUIProject trueVariant = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, true, false, false, false, false, false, null);
		assertEquals(1, countOccurrences(trueVariant.getFileContent(), "<con:testCase"), "Only the default test case should exist");
	}

	@Test
	void nestedRequiredObject_generatesMissingAndWrongVariantsForBothLevels() throws Exception {
		OpenAPI openAPI = parseSpec(NESTED_REQUIRED_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, false, false, null);
		String xml = soapUIProject.getFileContent();

		assertTrue(xml.contains("missing address_TestCase"), xml);
		assertTrue(xml.contains("missing address_street_TestCase"), "A required property nested inside a required object must also get its own variant: " + xml);
		assertTrue(xml.contains("wrong address_TestCase"), "The object property itself is also a wrong-value candidate: " + xml);
		assertTrue(xml.contains("wrong address_street_TestCase"), xml);
	}

	@Test
	void missingVariant_omitsOnlyTheTargetedPropertyFromTheBody() throws Exception {
		OpenAPI openAPI = parseSpec(FLAT_REQUIRED_SPEC);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, true, true, null);
		String decoded = decode(soapUIProject.getFileContent());

		// The default request body has all three properties
		assertTrue(decoded.contains("\"name\": \"\""), decoded);
		assertTrue(decoded.contains("\"age\": 0"), decoded);

		int missingAgeStart = decoded.indexOf("name=\"missing age\"");
		assertTrue(missingAgeStart >= 0, decoded);
		// The inner <con:request>{json}</con:request> tag holds the body; stop at its closing tag so the
		// block doesn't spill into the next sibling <con:request name="...">...</con:request> element
		int bodyCloseTag = decoded.indexOf("</con:request>", missingAgeStart);
		assertTrue(bodyCloseTag > missingAgeStart, decoded);
		String missingAgeBlock = decoded.substring(missingAgeStart, bodyCloseTag);
		assertFalse(missingAgeBlock.contains("\"age\""), "The 'missing age' variant must omit the age property entirely: " + missingAgeBlock);
		assertTrue(missingAgeBlock.contains("\"name\""), "Every other property should keep its normal value: " + missingAgeBlock);
	}

	@Test
	void wrongVariant_usesConfiguredExampleOverride() throws Exception {
		OpenAPI openAPI = parseSpec(FLAT_REQUIRED_SPEC);
		ExampleValues wrong = new ExampleValues();
		wrong.setString("custombadstring");
		ExamplesConfig examples = new ExamplesConfig();
		examples.setWrong(wrong);

		SoapUIProject soapUIProject = new SoapUIProject("TestApi", openAPI, null, null, null,
				false, null, false, false, false, false, true, true, examples);
		String decoded = decode(soapUIProject.getFileContent());

		int wrongNameStart = decoded.indexOf("name=\"wrong name\"");
		assertTrue(wrongNameStart >= 0, decoded);
		String wrongNameBlock = decoded.substring(wrongNameStart, Math.min(decoded.length(), wrongNameStart + 2000));
		assertTrue(wrongNameBlock.contains("custombadstring"), "The configured examples.wrong.string value should be used for the 'wrong name' variant: " + wrongNameBlock);
	}
}
