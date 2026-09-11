package org.apiaddicts.apitools.openapi2soapui.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;

@SuppressWarnings({"java:S106", "java:S5443"})
final class SoapUILogging {

	private static final String LOG4J_CONFIG_PROPERTY = "soapui.log4j.config";

	static final String LOG_LEVEL_PROPERTY = "openapi2soapui.cli.logLevel";

	static final String PARSER_LOG_LEVEL_PROPERTY = "openapi2soapui.cli.parserLogLevel";

	private static final String CONFIG_RESOURCE = "/soapui-cli-log4j.xml";

	private SoapUILogging() {
	}

	static void install() throws IOException {
		if (System.getProperty(LOG_LEVEL_PROPERTY) == null) System.setProperty(LOG_LEVEL_PROPERTY, "WARN");
		if (System.getProperty(PARSER_LOG_LEVEL_PROPERTY) == null) System.setProperty(PARSER_LOG_LEVEL_PROPERTY, "ERROR");

		if (System.getProperty(LOG4J_CONFIG_PROPERTY) != null) return;

		try (InputStream config = SoapUILogging.class.getResourceAsStream(CONFIG_RESOURCE)) {
			if (config == null) return;
			Path target = Files.createTempFile("soapui-cli-log4j", ".xml");
			target.toFile().deleteOnExit();
			Files.copy(config, target, StandardCopyOption.REPLACE_EXISTING);
			System.setProperty(LOG4J_CONFIG_PROPERTY, target.toAbsolutePath().toString());
		}
	}

	static <T> T withoutStdout(Callable<T> action) throws Exception {
		PrintStream original = System.out;
		System.setOut(new PrintStream(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8));
		try {
			return action.call();
		} finally {
			System.setOut(original);
		}
	}
}
