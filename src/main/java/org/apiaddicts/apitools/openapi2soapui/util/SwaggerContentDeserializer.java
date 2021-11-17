package org.apiaddicts.apitools.openapi2soapui.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.apiaddicts.apitools.openapi2soapui.error.exceptions.SwaggerContentEmptyException;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.SwaggerInvalidContentException;

/**
 * openApiSpec request body property deserializer
 */
public class SwaggerContentDeserializer extends JsonDeserializer<Object> {

	/**
	 * Deserialize openApiSpec
	 * @return openApiSpec deserialized
	 */
	@Override
	public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser.getValueAsString().isEmpty()) throw new SwaggerContentEmptyException("Swagger content empty");
		String decodeBase64Result = SerializedDataUtils.decodeBase64(parser.getValueAsString());
		if (decodeBase64Result != null && !decodeBase64Result.isBlank()) {
			if (SerializedDataUtils.isYAMLValid(decodeBase64Result) || SerializedDataUtils.isJSONValid(decodeBase64Result)) {
				return decodeBase64Result;
			} else {
				throw new SwaggerInvalidContentException("Invalid swagger content format");
			}
		} else {
			throw new SwaggerInvalidContentException("Invalid swagger content format");
		}
	}
}
