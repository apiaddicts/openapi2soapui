package org.apiaddicts.apitools.openapi2soapui.cli;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import io.swagger.v3.oas.models.OpenAPI;

import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectRequest;
import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;

@SuppressWarnings("java:S106")
public final class Openapi2SoapUICli {

	static final int EXIT_OK = 0;

	static final int EXIT_ERROR = 1;

	static final int EXIT_USAGE = 2;

	private static final String VERSION_RESOURCE = "/cli.properties";

	private static final String UNKNOWN_VERSION = "unknown";

	public static void main(String[] args) {
		System.exit(new Openapi2SoapUICli().run(args));
	}

	public int run(String[] args) {
		CliArgs cli;
		try {
			cli = CliArgs.parse(args);
		} catch (CliArgs.UsageException e) {
			return usageError(e.getMessage());
		}

		if (cli.isHelp()) {
			System.out.println(CliArgs.USAGE);
			return EXIT_OK;
		}
		if (cli.isVersion()) {
			System.out.println("openapi2soapui " + version());
			return EXIT_OK;
		}
		if (cli.getSpecFile() == null && cli.getConfigFile() == null) {
			return usageError("no input given, pass -f <openapi file> and/or -c <config json>");
		}

		try {
			SoapUILogging.install();
			return generate(cli);
		} catch (Exception | LinkageError e) {
			return error(describe(e));
		}
	}

	private int generate(CliArgs cli) throws Exception {
		SoapUIProjectRequest request = cli.toRequest();

		String spec = request.getOpenAPIContent();
		if (spec == null || spec.isBlank()) {
			return error("no OpenAPI spec given, pass -f <file> or set openApiSpec in the config file");
		}

		OpenAPI openAPI = SerializedDataUtils.parseOpenAPIContent(spec);
		if (openAPI.getInfo() == null || openAPI.getInfo().getVersion() == null) {
			return error("Version not found in OpenAPI");
		}

		if (request.getApiName() == null || request.getApiName().isBlank()) {
			request.setApiName(cli.defaultApiName(openAPI.getInfo().getTitle()));
		}

		List<String> violations = CliValidator.validate(request);
		if (!violations.isEmpty()) {
			System.err.println("error: invalid request");
			violations.forEach(violation -> System.err.println("  " + violation));
			return EXIT_ERROR;
		}

		SoapUIProject project = SoapUILogging.withoutStdout(
				() -> new SoapUIProject(request.getApiName(), openAPI, request.getOAuth2Profiles(),
						request.getHeaders(), request.getTestCaseNames(), request.getReadOnly(),
						request.getServerPattern(), request.getMinimalEndpoints(), request.getMicrocksHeaders(),
						request.getGenerateOneOfAnyOf(), request.getValidateSchema(), request.getSchemaIsInline(),
						request.getIsInline(), request.getSchemaPrettyPrint(), request.getHasScopes(),
						request.getApplicationToken(), request.getNumberOfScopes(), request.getExamples(),
						request.getCustomAuthorizationsFile()));
		try {
			Path target = write(project.getFileContent(),
					cli.resolveOutput(request.getApiName(), openAPI.getInfo().getVersion()));
			System.out.println("SoapUI project generated successfully in " + target);
			return EXIT_OK;
		} finally {
			project.deleteTemporaryFile();
		}
	}

	private static Path write(String xml, Path target) throws IOException {
		Path absolute = target.toAbsolutePath().normalize();
		Path parent = absolute.getParent();
		if (parent != null) Files.createDirectories(parent);
		Files.writeString(absolute, xml);
		return absolute;
	}

	private static int usageError(String message) {
		System.err.println("error: " + message);
		System.err.println();
		System.err.println(CliArgs.USAGE);
		return EXIT_USAGE;
	}

	private static int error(String message) {
		System.err.println("error: " + message);
		return EXIT_ERROR;
	}

	private static String describe(Throwable failure) {
		String message = failure.getMessage();
		if (message == null || message.isBlank()) return failure.getClass().getSimpleName();
		return (failure instanceof Error) ? failure.getClass().getSimpleName() + ": " + message : message;
	}

	private static String version() {
		try (InputStream stream = Openapi2SoapUICli.class.getResourceAsStream(VERSION_RESOURCE)) {
			if (stream == null) return UNKNOWN_VERSION;
			Properties properties = new Properties();
			properties.load(stream);
			return properties.getProperty("version", UNKNOWN_VERSION);
		} catch (IOException e) {
			return UNKNOWN_VERSION;
		}
	}
}
