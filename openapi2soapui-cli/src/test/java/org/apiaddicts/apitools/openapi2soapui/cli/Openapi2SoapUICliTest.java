package org.apiaddicts.apitools.openapi2soapui.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Openapi2SoapUICliTest {

	private static final String SPEC = "petstore.yaml";
	private static final String CONFIG = "request.json";

	@TempDir
	Path outputDir;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();
	private PrintStream originalOut;
	private PrintStream originalErr;

	@BeforeEach
	void captureConsole() {
		originalOut = System.out;
		originalErr = System.err;
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
		System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
	}

	@AfterEach
	void restoreConsole() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	void generatesProjectFromSpecFile() throws Exception {
		int exitCode = run("-f", resource(SPEC), "-n", "Petstore", "-o", outputDir.toString());

		assertEquals(Openapi2SoapUICli.EXIT_OK, exitCode, stderr());
		Path project = outputDir.resolve("Petstore_1.0.0-soapui-project.xml");
		assertTrue(Files.exists(project), "expected the project at " + project);
		String xml = Files.readString(project);
		assertTrue(xml.contains("name=\"Petstore_1.0.0\""), "project name missing from " + project);
		assertTrue(xml.contains("/pet/findByStatus_Petstore_1.0.0-GET-Suite"), "test suite missing");
		assertTrue(xml.contains("GET_CaseOkAllProperties"), "test case missing");
		assertEquals(1, stdout().lines().count(), "unexpected stdout: " + stdout());
	}

	@Test
	void generatesProjectFromConfigFileWithBase64Spec() throws Exception {
		int exitCode = run("-c", resource(CONFIG), "-o", outputDir.resolve("project.xml").toString());

		assertEquals(Openapi2SoapUICli.EXIT_OK, exitCode, stderr());
		String xml = Files.readString(outputDir.resolve("project.xml"));
		assertTrue(xml.contains("name=\"Petstore_1.0.0\""), "project name missing");
	}

	@Test
	void derivesApiNameFromSpecTitleWhenNotGiven() throws Exception {
		int exitCode = run("-f", resource(SPEC), "-o", outputDir.toString());

		assertEquals(Openapi2SoapUICli.EXIT_OK, exitCode, stderr());
		assertTrue(Files.exists(outputDir.resolve("SwaggerPetstore_1.0.0-soapui-project.xml")),
				"expected the derived name, found " + Arrays.toString(outputDir.toFile().list()));
	}

	@Test
	void appliesFlagsOverConfigFile() throws Exception {
		int exitCode = run("-f", resource(SPEC), "-n", "Flagged", "--read-only", "--no-validate-schema",
				"-H", "X-Api-Key:secret", "-o", outputDir.toString());

		assertEquals(Openapi2SoapUICli.EXIT_OK, exitCode, stderr());
		String xml = Files.readString(outputDir.resolve("Flagged_1.0.0-soapui-project.xml"));
		assertTrue(xml.contains("X-Api-Key"), "the header passed with -H is missing");
		assertTrue(xml.contains("Valid HTTP Status Codes"), "the status code assertion is always expected");
		assertFalse(xml.contains("GroovyScriptAssertion"), "no-validate-schema should drop the schema assertion");
	}

	@Test
	void missingSpecFileIsAnError() {
		int exitCode = run("-f", outputDir.resolve("nope.yaml").toString(), "-o", outputDir.toString());

		assertEquals(Openapi2SoapUICli.EXIT_ERROR, exitCode);
		assertTrue(stderr().contains("OpenAPI file not found"), "unexpected stderr: " + stderr());
	}

	@Test
	void unknownOptionIsAUsageError() {
		int exitCode = run("--nope");

		assertEquals(Openapi2SoapUICli.EXIT_USAGE, exitCode);
		assertTrue(stderr().contains("unknown option: --nope"), "unexpected stderr: " + stderr());
		assertTrue(stderr().contains("Usage:"), "the usage text should follow the error");
	}

	@Test
	void noInputIsAUsageError() {
		int exitCode = run("-o", outputDir.toString());

		assertEquals(Openapi2SoapUICli.EXIT_USAGE, exitCode);
		assertTrue(stderr().contains("no input given"), "unexpected stderr: " + stderr());
	}

	@Test
	void helpAndVersionSucceed() {
		assertEquals(Openapi2SoapUICli.EXIT_OK, run("--help"));
		assertTrue(stdout().contains("Usage:"));

		out.reset();
		assertEquals(Openapi2SoapUICli.EXIT_OK, run("--version"));
		assertTrue(stdout().startsWith("openapi2soapui "), "unexpected version output: " + stdout());
	}

	private int run(String... args) {
		return new Openapi2SoapUICli().run(args);
	}

	private String resource(String name) throws Exception {
		return Path.of(getClass().getClassLoader().getResource(name).toURI()).toString();
	}

	private String stdout() {
		return out.toString(StandardCharsets.UTF_8);
	}

	private String stderr() {
		return err.toString(StandardCharsets.UTF_8);
	}
}
