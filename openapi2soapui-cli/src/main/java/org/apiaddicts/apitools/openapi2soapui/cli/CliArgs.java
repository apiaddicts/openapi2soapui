package org.apiaddicts.apitools.openapi2soapui.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apiaddicts.apitools.openapi2soapui.request.Header;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectRequest;

final class CliArgs {

	static final String DEFAULT_OUTPUT = "./out";

	static final String USAGE = """
		openapi2soapui - generates a SoapUI project (XML) from an OpenAPI specification

		Usage: java -jar openapi2soapui-cli.jar [options]

		Input (at least one of -f, -c is required):
		  -f, --file <path>           OpenAPI spec, JSON or YAML, as plain text (not base64)
		  -c, --config <path>         JSON file with the same body as the REST API, where
		                              openApiSpec is base64 encoded. It is the only way to pass
		                              oAuth2Profiles, customAuthorizationsFile and examples.
		                              When -f is also given, -f provides the spec.

		Output:
		  -o, --output <path>         Folder, or a path ending in .xml for an exact file name
		                              (default: ./out)

		Generation options, they override the config file:
		  -n, --api-name <name>       apiName (default: the spec title, or the spec file name)
		  -H, --header <key:value>    Request header, repeatable
		      --server-pattern <text> Pick the spec server whose URL contains this text
		      --test-case-names <a,b> Extra test cases, comma separated
		      --number-of-scopes <n>  numberOfScopes, only relevant with --has-scopes
		      --read-only             Generate only GET and OPTIONS test cases
		      --minimal-endpoints     Collapse the ErrorRequired test cases into one
		      --microcks-headers      Add the X-Microcks-Response-Name header to every request
		      --generate-one-of-any-of  Resolve oneOf/anyOf using their first candidate
		      --schema-is-inline      Embed the response schema instead of using a project property
		      --is-inline             Embed body example values instead of project properties
		      --has-scopes            One extra test case per oAuth2Profiles entry
		      --application-token     Extra test case per CLIENT_CREDENTIALS profile
		      --no-validate-schema    Do not add the schema assertion (on by default)
		      --no-schema-pretty-print  Serialize the schema compactly (pretty by default)

		Other:
		  -h, --help                  Show this help
		  -V, --version               Show the version
		""";

	private static final String OUTPUT_FILE_SUFFIX = "-soapui-project.xml";

	private static final Pattern UNSAFE_NAME_CHARS = Pattern.compile("[^\\p{L}\\p{N}._-]+");

	private static final Pattern LEADING_DASHES = Pattern.compile("^-+");

	private static final Pattern TRAILING_DASHES = Pattern.compile("-+$");

	private String specFile;
	private String configFile;
	private String output;
	private String apiName;
	private String serverPattern;
	private Set<String> testCaseNames;
	private final List<Header> headers = new ArrayList<>();
	private Boolean readOnly;
	private Boolean minimalEndpoints;
	private Boolean microcksHeaders;
	private Boolean generateOneOfAnyOf;
	private Boolean validateSchema;
	private Boolean schemaIsInline;
	private Boolean schemaPrettyPrint;
	private Boolean isInline;
	private Boolean hasScopes;
	private Boolean applicationToken;
	private Integer numberOfScopes;
	private boolean help;
	private boolean version;

	private CliArgs() {
	}

	static CliArgs parse(String[] args) {
		CliArgs parsed = new CliArgs();
		Tokens tokens = new Tokens(normalize(args));
		while (tokens.hasNext()) {
			String option = tokens.next();
			switch (option) {
				case "-h", "--help" -> parsed.help = true;
				case "-V", "--version" -> parsed.version = true;
				case "-f", "--file" -> parsed.specFile = tokens.value(option);
				case "-c", "--config" -> parsed.configFile = tokens.value(option);
				case "-o", "--output" -> parsed.output = tokens.value(option);
				case "-n", "--api-name" -> parsed.apiName = tokens.value(option);
				case "-H", "--header" -> parsed.headers.add(header(tokens.value(option)));
				case "--server-pattern" -> parsed.serverPattern = tokens.value(option);
				case "--test-case-names" -> parsed.testCaseNames = testCaseNames(tokens.value(option));
				case "--number-of-scopes" -> parsed.numberOfScopes = integer(tokens.rawValue(option), option);
				case "--read-only" -> parsed.readOnly = Boolean.TRUE;
				case "--minimal-endpoints" -> parsed.minimalEndpoints = Boolean.TRUE;
				case "--microcks-headers" -> parsed.microcksHeaders = Boolean.TRUE;
				case "--generate-one-of-any-of" -> parsed.generateOneOfAnyOf = Boolean.TRUE;
				case "--schema-is-inline" -> parsed.schemaIsInline = Boolean.TRUE;
				case "--is-inline" -> parsed.isInline = Boolean.TRUE;
				case "--has-scopes" -> parsed.hasScopes = Boolean.TRUE;
				case "--application-token" -> parsed.applicationToken = Boolean.TRUE;
				case "--no-validate-schema" -> parsed.validateSchema = Boolean.FALSE;
				case "--no-schema-pretty-print" -> parsed.schemaPrettyPrint = Boolean.FALSE;
				default -> throw new UsageException("unknown option: " + option);
			}
		}
		return parsed;
	}

	private static List<String> normalize(String[] args) {
		List<String> tokens = new ArrayList<>(args.length);
		for (String arg : args) {
			int equals = arg.startsWith("--") ? arg.indexOf('=') : -1;
			if (equals > 0) {
				tokens.add(arg.substring(0, equals));
				tokens.add(arg.substring(equals + 1));
			} else {
				tokens.add(arg);
			}
		}
		return tokens;
	}

	private static Header header(String value) {
		int separator = value.indexOf(':');
		if (separator < 1 || separator == value.length() - 1) {
			throw new UsageException("header must be given as key:value, found " + value);
		}
		Header header = new Header();
		header.setKey(value.substring(0, separator).trim());
		header.setValue(value.substring(separator + 1).trim());
		return header;
	}

	private static Set<String> testCaseNames(String value) {
		Set<String> names = new LinkedHashSet<>();
		Arrays.stream(value.split(",")).map(String::trim).filter(name -> !name.isEmpty()).forEach(names::add);
		if (names.isEmpty()) throw new UsageException("option --test-case-names requires at least one name");
		return names;
	}

	private static Integer integer(String value, String option) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			throw new UsageException("option " + option + " requires a number, found " + value);
		}
	}

	SoapUIProjectRequest toRequest() throws IOException {
		SoapUIProjectRequest request = (configFile != null) ? readConfig(configFile) : new SoapUIProjectRequest();

		if (specFile != null) request.setOpenAPIContent(readSpec(specFile));
		if (!headers.isEmpty()) request.setHeaders(mergeHeaders(request.getHeaders()));

		apply(apiName, request::setApiName);
		apply(serverPattern, request::setServerPattern);
		apply(testCaseNames, request::setTestCaseNames);
		apply(readOnly, request::setReadOnly);
		apply(minimalEndpoints, request::setMinimalEndpoints);
		apply(microcksHeaders, request::setMicrocksHeaders);
		apply(generateOneOfAnyOf, request::setGenerateOneOfAnyOf);
		apply(validateSchema, request::setValidateSchema);
		apply(schemaIsInline, request::setSchemaIsInline);
		apply(schemaPrettyPrint, request::setSchemaPrettyPrint);
		apply(isInline, request::setIsInline);
		apply(hasScopes, request::setHasScopes);
		apply(applicationToken, request::setApplicationToken);
		apply(numberOfScopes, request::setNumberOfScopes);

		return request;
	}

	private static <T> void apply(T value, Consumer<T> setter) {
		if (value != null) setter.accept(value);
	}

	private static SoapUIProjectRequest readConfig(String path) throws IOException {
		ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		return mapper.readValue(requireFile(path, "config"), SoapUIProjectRequest.class);
	}

	private static String readSpec(String path) throws IOException {
		String spec;
		try {
			spec = Files.readString(requireFile(path, "OpenAPI").toPath());
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("OpenAPI file is not valid UTF-8: " + path);
		}
		if (spec.isBlank()) throw new IllegalArgumentException("OpenAPI file is empty: " + path);
		return spec;
	}

	private static File requireFile(String path, String description) {
		File file = new File(path);
		if (!file.isFile()) throw new IllegalArgumentException(description + " file not found: " + path);
		return file;
	}

	private List<Header> mergeHeaders(List<Header> configured) {
		List<Header> merged = new ArrayList<>();
		if (configured != null) merged.addAll(configured);
		merged.addAll(headers);
		return merged;
	}

	Path resolveOutput(String apiName, String apiVersion) {
		String target = (output != null) ? output : DEFAULT_OUTPUT;
		if (target.toLowerCase().endsWith(".xml")) return Path.of(target);
		return Path.of(target).resolve(sanitize(apiName) + "_" + sanitize(apiVersion) + OUTPUT_FILE_SUFFIX);
	}

	private static String sanitize(String value) {
		if (value == null || value.isBlank()) return "project";
		String safe = UNSAFE_NAME_CHARS.matcher(value).replaceAll("-");
		safe = LEADING_DASHES.matcher(safe).replaceAll("");
		safe = TRAILING_DASHES.matcher(safe).replaceAll("");
		return safe.isEmpty() ? "project" : safe;
	}

	String defaultApiName(String title) {
		String fromTitle = UNSAFE_NAME_CHARS.matcher(title == null ? "" : title).replaceAll("");
		if (!fromTitle.isEmpty()) return fromTitle;
		if (specFile != null) {
			String fileName = Path.of(specFile).getFileName().toString().replaceFirst("\\.[^.]+$", "");
			String fromFile = UNSAFE_NAME_CHARS.matcher(fileName).replaceAll("");
			if (!fromFile.isEmpty()) return fromFile;
		}
		return "api";
	}

	String getSpecFile() {
		return specFile;
	}

	String getConfigFile() {
		return configFile;
	}

	boolean isHelp() {
		return help;
	}

	boolean isVersion() {
		return version;
	}

	private static final class Tokens {

		private final List<String> tokens;

		private int index;

		Tokens(List<String> tokens) {
			this.tokens = tokens;
		}

		boolean hasNext() {
			return index < tokens.size();
		}

		String next() {
			return tokens.get(index++);
		}

		String value(String option) {
			String value = rawValue(option);
			if (value.length() > 1 && value.startsWith("-")) {
				throw new UsageException("option " + option + " requires a value, found " + value);
			}
			return value;
		}

		String rawValue(String option) {
			if (!hasNext()) throw new UsageException("option " + option + " requires a value");
			return next();
		}
	}

	static final class UsageException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		UsageException(String message) {
			super(message);
		}
	}
}
