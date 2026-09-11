package org.apiaddicts.apitools.openapi2soapui.util;

import java.util.Base64;

import lombok.extern.slf4j.Slf4j;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.DecodeBase64Exception;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.ParseOpenAPIException;

import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;

@Slf4j
public class SerializedDataUtils {

	private SerializedDataUtils() {
		// Intentional blank
	}

	/**
	 * @param value to decoded
	 * @return content of string decoded
	 */
	public static String decodeBase64(String value) {
		try {
	        Base64.Decoder decoder = Base64.getDecoder();
	        byte[] decodedValue = decoder.decode(value);
	    	return new String(decodedValue);
		} catch (Exception e) {
			log.info("Error deconding B64", e);
			throw new DecodeBase64Exception(e.getMessage());
		}
	}

	/**
	 * @param content string to validate
	 * @return result of validation
	 */
	public static boolean isJSONValid(String content) {
	    try {
	        new JSONObject(content);
		    return true;
	    } catch (JSONException e) {
			log.info("Invalid JSON", e);
	    }
		return false;
	}

	/**
	 * @param content string to validate
	 * @return reuslt of validation
	 */
	public static boolean isYAMLValid(String content) {
		try {
			Yaml yaml = new Yaml();
			yaml.load(content);
            return true;
        } catch (Exception e) {
			log.info("Invalid YAML", e);
        }
		return false;
	}

	/**
	 * @param openAPIContent openAPIContent as string
	 * @return OpenAPI as Java POJO
	 */
	public static OpenAPI parseOpenAPIContent(String openAPIContent) {
		try {
			ParseOptions parseOptions = new ParseOptions();
			parseOptions.setResolve(true);
			parseOptions.setResolveFully(true);
			OpenAPI openAPI = new OpenAPIParser().readContents(openAPIContent, null, parseOptions).getOpenAPI();
			validateRequiredOpenAPIProperties(openAPI);
			return openAPI;
		} catch (Exception e) {
			log.info("Error Parsing OpenAPI", e);
			throw new ParseOpenAPIException(e.getMessage());
		}
	}

	/**
	 * @param openAPI instance of OpenAPI
	 */
	private static void validateRequiredOpenAPIProperties(OpenAPI openAPI) {
		if (openAPI == null || openAPI.getInfo() == null) throw new ParseOpenAPIException("It's not an OpenAPI Spec");
	}
}