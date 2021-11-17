package org.apiaddicts.apitools.openapi2soapui.util;

import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Helper class for resolving open api references
 */
public class RefResolver {
	private Set<String> resolvedRefs = new HashSet<>();
	private OpenAPI openAPI;
	
	public RefResolver(OpenAPI openAPI) {
		this.openAPI = openAPI;
	}

	/**
	 * Resolve schema reference
	 * @param schema with $ref
	 * @return schema resolved
	 */
	@SuppressWarnings("rawtypes")
	public Schema resolveSchema(Schema schema) {
		String ref = schema.get$ref();
		if (ref != null && !resolvedRefs.contains(ref)) {
			String[] refParts = ref.split("/");
			String key = refParts[refParts.length - 1];
			schema = openAPI.getComponents().getSchemas().get(key);
			resolvedRefs.add(ref);
		}
		return schema;
	}
}
