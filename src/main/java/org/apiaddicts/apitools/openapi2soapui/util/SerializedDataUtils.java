package org.apiaddicts.apitools.openapi2soapui.util;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.DecodeBase64Exception;
import org.apiaddicts.apitools.openapi2soapui.error.exceptions.ParseOpenAPIException;

import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;

/**
 * Helper class for data serialization
 */
@Slf4j
public class SerializedDataUtils {
	private static final String QUERY = "query";
	private static final String GET = "get";
	
	private SerializedDataUtils() {
		// Intentional blank
	}
	
	/**
	 * Decode string in base64
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
	 * Validate if a string in json format is valid
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
	 * Validate if a string in yaml format is valid
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
	 * Parses OpenAPI definitions in JSON or YAML format into swagger-core representation as Java POJO
	 * @param openAPIContent openAPIContent as string
	 * @return OpenAPI as Java POJO
	 */
	public static OpenAPI parseOpenAPIContent(String openAPIContent) {
		try {
			String normalizedContent = normalizeOpenAPI32Content(openAPIContent);
			ParseOptions parseOptions = new ParseOptions();
			parseOptions.setResolve(true);
			parseOptions.setResolveFully(true);
			OpenAPI openAPI = new OpenAPIParser().readContents(normalizedContent, null, parseOptions).getOpenAPI();
			validateRequiredOpenAPIProperties(openAPI);
			return openAPI;
		} catch (Exception e) {
			log.info("Error Parsing OpenAPI", e);
			throw new ParseOpenAPIException(e.getMessage());
		}
	}

	/**
	 * Normalize OpenAPI 3.2 content so it can be parsed by the current parser stack.
	 * This keeps the runtime compatible while parser-level 3.2 support evolves.
	 * @param openAPIContent OpenAPI content as string
	 * @return normalized content for parser consumption
	 */
	private static String normalizeOpenAPI32Content(String openAPIContent) {
		try {
			Yaml yaml = new Yaml();
			Object parsed = yaml.load(openAPIContent);
			if (!(parsed instanceof Map)) {
				return openAPIContent;
			}

			Map<String, Object> root = (Map<String, Object>) parsed;
			Object version = root.get("openapi");
			if (!(version instanceof String versionStr) || !versionStr.startsWith("3.2")) {
				return openAPIContent;
			}

			root.put("openapi", "3.1.0");
			normalizeNode(root);
			return yaml.dump(root);
		} catch (Exception e) {
			log.debug("OpenAPI 3.2 normalization skipped", e);
			return openAPIContent;
		}
	}

	/**
	 * Recursively normalize known OpenAPI 3.2-only fields to 3.1-compatible fields.
	 * @param node current structure node
	 */
	@SuppressWarnings("unchecked")
	private static void normalizeNode(Object node) {
		if (node instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) node;
			normalizeParameterLocation(map);
			normalizeTopLevel32Fields(map);
			normalizeQueryOperation(map);
			normalizeComponentsMediaTypes(map);
			map.values().forEach(SerializedDataUtils::normalizeNode);
		} else if (node instanceof List) {
			((List<?>) node).forEach(SerializedDataUtils::normalizeNode);
		}
	}

	private static void normalizeParameterLocation(Map<String, Object> map) {
		Object inValue = map.get("in");
		if (inValue instanceof String inStr && "querystring".equalsIgnoreCase(inStr)) {
			map.put("in", QUERY);
		}
	}

	private static void normalizeTopLevel32Fields(Map<String, Object> map) {
		if (map.containsKey("$self")) {
			map.put("x-oas32-self", map.remove("$self"));
		}

		if (map.containsKey("additionalOperations")) {
			map.put("x-oas32-additionalOperations", map.remove("additionalOperations"));
		}
	}

	private static void normalizeQueryOperation(Map<String, Object> map) {
		if (map.containsKey(QUERY)) {
			Object queryOp = map.remove(QUERY);
			if (!map.containsKey(GET)) {
				map.put(GET, queryOp);
			} else {
				map.put("x-oas32-query-operation", queryOp);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void normalizeComponentsMediaTypes(Map<String, Object> map) {
		Object componentsObj = map.get("components");
		if (componentsObj instanceof Map) {
			Map<String, Object> components = (Map<String, Object>) componentsObj;
			if (components.containsKey("mediaTypes")) {
				components.put("x-oas32-mediaTypes", components.remove("mediaTypes"));
			}
		}
	}

	/**
	 * Validates the mandatory properties of an Open API Spec 
	 * @param openAPI instance of OpenAPI
	 */
	private static void validateRequiredOpenAPIProperties(OpenAPI openAPI) {
		if (openAPI == null || openAPI.getInfo() == null) throw new ParseOpenAPIException("It's not an OpenAPI Spec");
	}
}