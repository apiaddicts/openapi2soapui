
# 🛠️ OpenAPI2SoapUI ![Release](https://img.shields.io/badge/release-0.1.0-purple) ![Swagger](https://img.shields.io/badge/-soap-%23Clojure?style=flat&logo=swagger&logoColor=white) ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=openjdk&logoColor=white)  [![License: LGPL v3](https://img.shields.io/badge/license-LGPL_v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0) 

[API](./openapi2soapui-rest/src/main/resources/static/api.yaml) to generate a SoapUI project from an OpenAPI Specification (fka Swagger Specification)

Given an OpenAPI Specification, either v2 or v3, a SoapUI project is generated with the _requests_ for each resource operation and a _test suite_. The response is the content of the SoapUI project in XML format to save as file and import into the SoapUI application.

### This repository is intended for :octocat: **community** use, it can be modified and adapted without commercial use. If you need a version, support or help for your **enterprise** or project, please contact us 📧 devrel@apiaddicts.org

[![Twitter](https://img.shields.io/badge/Twitter-%23000000.svg?style=for-the-badge&logo=x&logoColor=white)](https://twitter.com/APIAddicts) 
[![Discord](https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/ZdbGqMBYy8)
[![LinkedIn](https://img.shields.io/badge/linkedin-%230077B5.svg?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/company/apiaddicts/)
[![Facebook](https://img.shields.io/badge/Facebook-%231877F2.svg?style=for-the-badge&logo=Facebook&logoColor=white)](https://www.facebook.com/apiaddicts)
[![YouTube](https://img.shields.io/badge/YouTube-%23FF0000.svg?style=for-the-badge&logo=YouTube&logoColor=white)](https://www.youtube.com/@APIAddictslmaoo)

# 🙌 Join the **OpenAPI2SoapUI** Adopters list 
📢 If OpenAPI2SoapUI is part of your organization's toolkit, we kindly encourage you to include your company's name in our Adopters list. 🙏 This not only significantly boosts the project's visibility and reputation but also represents a small yet impactful way to give back to the project.

| Organization  | Description of Use / Referenc |
|---|---|
|  [CloudAppi](https://cloudappi.net/)  | Apification and generation of microservices |
| [RSI](https://www.ruralserviciosinformaticos.com/)  | Generation of microservices  |

# 👩🏽‍💻  Contribute to ApiAddicts 

We're an inclusive and open community, welcoming you to join our effort to enhance ApiAddicts, and we're excited to prioritize tasks based on community input, inviting you to review and collaborate through our GitHub issue tracker.

Feel free to drop by and greet us on our GitHub discussion or Discord chat. You can also show your support by giving us some GitHub stars ⭐️, or by following us on Twitter, LinkedIn, and subscribing to our YouTube channel! 🚀

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/apiaddicts)

# ⚙️ Functionalities

[Here](./openapi2soapui-rest/src/main/resources/static/api.yaml) you can check the definition of the API Swagger to SoapUI

- Base64 Decoding of Open API Specification Content
- Parse Open API Specification Content into swagger-core representation as Java POJO
- Create SoapUI Project
  - Add OAuth 2.0 Profiles to SoapUI Project
  - Add new REST Service to SoapUI Project
    - Add Endpoints to REST Service
    - Set basePath to REST Service
    - Add Resources (Paths) to REST Service
      - Add Methods (Verbs / Operations) to each Resource
        - Set REST Request to each Method
          - Set Credentials (OAuth 2.0 Profile)
          - Set Parameters examples to REST Request
          - Set Request Body example to REST Request
          - Set Custom Headers to REST Request
  - Add new TestSuites to SoapUI Project for each Method
    - Add TestCases to TestSuite
      - Add Execution Test Step (REST Request)

Nomenclature used:
- SoapUI Project: {apiName}\_{apiVersion}
- REST Service: {apiName}
- Resource: {path}
- Method: {httpMethodInUppercase}
- Request: {defaultRequestName}
- Test Suite: {path}\_{apiName}\_{apiVersion}-{httpMethodInUppercase}-Suite (run type SEQUENTIAL, abortOnError false)
- Test Case: {httpMethodInUppercase}\_Case{CaseDescription}
- Test Step: Execution\_{httpMethodInUppercase}\_TestStep

The variables are obtained from:
- apiName: property apiName of request body
- apiVersion: version defined in the 'info' section of the OpenAPI Spec
- path: each path defined in the OpenAPI Spec
- httpMethodInUppercase: each HTTP methods of paths defined in OpenAPI Spec

## Technology stack
### Overview

|Technology              |Description                 |
|------------------------|----------------------------|
|Core Framework          |Spring Boot 3               |

### Server - Backend

|Technology                                               |Description                                                                   |
|---------------------------------------------------------|------------------------------------------------------------------------------|
|[JDK 21](https://docs.oracle.com/en/java/javase/21/)                       |Java Development Kit                                                          |
|[Spring Boot 3](https://spring.io/projects/spring-boot)  |Framework to ease the bootstrapping and development of new Spring Applications|
|[Maven 3](https://maven.apache.org)                      |Dependency Management                                                         |
|[Tomcat 10.1+](https://tomcat.apache.org)                |Server deploy WAR (Jakarta EE / Servlet 6)                                    |

###  Libraries and Plugins
|Technology              |Description                 |
|------------------------|----------------------------|
|[Lombok](https://projectlombok.org/) |Never write another getter or equals method again, with one annotation your class has a fully featured builder, Automate your logging variables, and much more.              |
|[Hibernate Validator](https://hibernate.org/validator/)|Express validation rules in a standardized way using annotation-based constraints and benefit from transparent integration with a wide variety of frameworks.|
|[Springdoc OpenAPI UI](https://springdoc.org/)|OpenAPI 3 Library for spring boot projects. Is based on swagger-ui, to display the OpenAPI description.|
|[SoapUI core module](https://www.soapui.org/open-source/)|SoapUI is the world's leading Functional Testing tool for SOAP and REST testing.|
|[Swagger Parser](https://github.com/swagger-api/swagger-parser)|Parses OpenAPI definitions in JSON or YAML format into swagger-core representation as Java POJO, returning any validation warnings/errors.|

# 📑 Getting started 

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* [JDK Installation](https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html)
* [Apache Maven Installation](https://maven.apache.org/install.html)
* [Setting up Lombok](https://projectlombok.org/setup/overview)
  * [Eclipse and its offshoots](https://projectlombok.org/setup/eclipse)
  * [Intellij IDEA](https://projectlombok.org/setup/intellij)
  * [Netbeans](https://projectlombok.org/setup/netbeans)
  * [Microsoft Visual Studio Code](https://projectlombok.org/setup/vscode)

### External dependencies

The project use __"SoapUI Core Module"__ dependency, which is not maven dependency, so you must have internet access to download the dependency from the external repository, below is the repository configuration inside the pom.xml file.

```xml
  ...

  <repositories>
    <repository>
      <id>SmartBearPluginRepository</id>
      <url>https://rapi.tools.ops.smartbear.io/nexus/content/groups/public/</url>			
    </repository>
  </repositories>
  
  ...
```
### Running the application with IDE

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `Openapi2SoapUIApplication` class from your IDE.

#### Example (Eclipse and its offshoots) :
* Download the zip or clone the Git repository.
* Unzip the zip file (if you downloaded one)
* Open IDE
	* File -> Import -> Existing Maven Project -> Navigate to the folder project 
	* Select the project
* Choose the Spring Boot Application file (search for @SpringBootApplication)
* Right Click on the file and Run as Java Application
* URL to access: **http://localhost:8080/api-openapi-to-soapui/v1/soap-ui-projects**

### Running the application locally with Maven

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

* Download the zip or clone the Git repository.
* Unzip the zip file (if you downloaded one)
* Open Command Prompt and Change directory (cd) to folder containing pom.xml
* To build and start the server type

```shell
$ mvn -pl openapi2soapui-rest -am spring-boot:run
```

The HTTP service lives in the `openapi2soapui-rest` module, hence the `-pl`; `-am` also builds the
`openapi2soapui-core` module it depends on.

* URL to access: **http://localhost:8080/api-openapi-to-soapui/v1/soap-ui-projects**

### Running the application in Docker

* Download the zip or clone the Git repository.
* Unzip the zip file (if you downloaded one)
* Open Command Prompt and Change directory (cd) to folder containing pom.xml
* To build and start the docker container type

```shell
$ mvn clean package -Pjar
$ docker-compose up -d
```

* URL to access: **http://localhost:8080/api-openapi-to-soapui/v1/soap-ui-projects**

### Running the application deploying WAR on Tomcat

The code can also be built into a war and then deployed on a Tomcat server.

* 	Download the zip or clone the Git repository.
* 	Unzip the zip file (if you downloaded one)
* 	Open Command Prompt and Change directory (cd) to folder containing pom.xml
* 	To build the war type

```sh
$ mvn clean package
```

* 	Once the war is built, copy the output WAR to Tomcat's webapps directory.

```sh
$CATALINA_HOME/webapps/openapi2soapui-<version>.war
```

The war is produced at `openapi2soapui-rest/target/openapi2soapui.war`.

* Restart Tomcat Server
* URL to access: **http://localhost:8080/openapi2soapui/api-openapi-to-soapui/v1/soap-ui-projects**

## 🖥️ Command line interface (CLI)

The same generation is available as a standalone command, with no server involved: an OpenAPI spec in JSON or
YAML goes in, the SoapUI project XML comes out. It reuses the exact same engine and the same request model as
the HTTP service, so both produce identical projects.

* To build the CLI jar

```shell
$ mvn clean package -DskipTests
```

* The jar is produced at `openapi2soapui-cli/target/openapi2soapui-cli.jar`

```shell
# generate from a spec file into ./output
$ java -jar openapi2soapui-cli.jar -f petstore.yaml

# name the API and pick the exact output file
$ java -jar openapi2soapui-cli.jar -f petstore.yaml -n Petstore -o ./petstore-project.xml

# only GET and OPTIONS test cases, with a custom header
$ java -jar openapi2soapui-cli.jar -f petstore.yaml --read-only -H "X-Api-Key:secret"

# full configuration: the very same JSON body the REST API takes, openApiSpec included as base64
$ java -jar openapi2soapui-cli.jar -c request.json

# config file for everything else, spec as a plain file
$ java -jar openapi2soapui-cli.jar -c request.json -f petstore.yaml

$ java -jar openapi2soapui-cli.jar --help
```

Notes:

* `-f` takes the spec as plain text; `-c` takes the REST API body, where `openApiSpec` is base64 encoded, so
  an existing request such as [demo/petstore-ok-only-run/request.json](demo/petstore-ok-only-run/request.json)
  works as is. When both are given, `-f` provides the spec.
* `oAuth2Profiles`, `customAuthorizationsFile` and `examples` are nested objects and are only reachable
  through `-c`. Every other parameter has a flag, listed by `--help`.
* Defaults match the HTTP API exactly, including `validateSchema` and `schemaPrettyPrint` being enabled unless
  turned off with `--no-validate-schema` / `--no-schema-pretty-print`. `apiName` is the only difference: the
  API requires it, while the CLI derives it from the spec title when neither `-n` nor the config provide one.
* Output defaults to `./output/{apiName}_{apiVersion}-soapui-project.xml`. An `-o` value ending in `.xml` is
  taken as the exact file, anything else as a folder.
* Exit codes: `0` success, `1` generation or validation error, `2` usage error. Errors go to stderr, so stdout
  only ever carries the result line. Add `-v` for stack traces and SoapUI logs.

## Files and Directories Structure

The project directory has a particular directory structure. A representative project is shown below:

### Project Structure

The build is a Maven multi module project: the conversion engine is shared by the HTTP service and the CLI.

```text
.
├── pom.xml                                     parent: packaging pom, shared properties and profiles
├── openapi2soapui-core                         the conversion engine, no Spring and no web
│   └── src/main
│       ├── java/org.apiaddicts.apitools.openapi2soapui
│       │   ├── .constants
│       │   ├── .error.exceptions
│       │   ├── .error.validators
│       │   ├── .model                          SoapUIProject, the engine itself
│       │   ├── .request                        request model shared by both front ends
│       │   └── .util
│       └── resources
│           └── messages.properties
├── openapi2soapui-rest                         the HTTP service (war by default, jar with -Pjar)
│   └── src/main
│       ├── java/org.apiaddicts.apitools.openapi2soapui
│       │   ├── (Openapi2SoapUIApplication)
│       │   ├── .config
│       │   ├── .controller
│       │   ├── .error                          HTTP error payload and controller advice
│       │   ├── .service
│       │   └── .util
│       └── resources
│           ├── static/api.yaml
│           ├── application.properties
│           ├── banner.txt
│           └── log4j.properties
├── openapi2soapui-cli                          the standalone command line jar
│   └── src/main
│       ├── java/org.apiaddicts.apitools.openapi2soapui.cli
│       └── resources
│           ├── cli.properties                  version reported by the version flag
│           ├── logback.xml
│           └── soapui-cli-log4j.xml            keeps SoapUI from flooding stdout
├── demo                                        sample specs, requests and generated projects
├── Dockerfile
├── docker-compose.yml
├── lombok.config
├── mvnw
├── mvnw.cmd
└── README.md
```

### Modules

* 	`openapi2soapui-core` - the conversion engine and the request model. Deliberately free of Spring and of any
	web dependency, so the CLI can embed it without booting an application context;
* 	`openapi2soapui-rest` - the HTTP service. Produces `openapi2soapui.war` by default and
	`openapi2soapui.jar` with `-Pjar`;
* 	`openapi2soapui-cli` - the command line front end. Produces `openapi2soapui-cli.jar`;

### Packages

* 	`config` - app configurations;
* 	`constants` - app contants;
* 	`controller` - listen to the client;
* 	`cli` - command line front end;
* 	`error` - manage errors;
* 	`exceptions` - custom exception handling;
* 	`validators` - custom validations;
* 	`model` - entities;
* 	`request` - body request model/entities;
* 	`service` - business logic;
* 	`util` - utility classes;


### Resources
* 	`resources/` - contains all the static resources, templates and property files.
* 	`resources/static` - contains static resources.
* 	`resources/static/api.yaml` - contains Open API Specification.
* 	`resources/application.properties` - contains application-wide properties. Spring reads the properties defined in this file to configure your application. You can define server’s default port, server’s context path, database URLs etc, in this file.
* 	`resources/log4j.properties` - contains contains the entire runtime configuration used by log4j. This file will contain log4j appenders information, log level information and output file names for file appenders.
* 	`resources/messages.properties` - contains the error messages used in the application. It lives in the core module because both the HTTP service and the CLI report the same validation messages from it.
* mvnw / mvnw.cmd - This allows you to run the Maven project without having Maven installed and present in the path. Download the correct version of Maven if it can't be found (as far as I know by default in your user home directory). The mvnw file is for Linux (bash) and mvnw.cmd is for the Windows environment.
* 	`pom.xml` - contains all the project dependencies

## Deploy

* 	Build the war type

```sh
$ mvn clean package
```

* 	Once the war is built, copy the output WAR to Tomcat's webapps directory.

```sh
$CATALINA_HOME/webapps/openapi2soapui.war
```

* Restart Tomcat Server
* URL to access: **\<protocol\>://\<host\>:\<port\>/openapi2soapui/api-openapi-to-soapui/v1/soap-ui-projects**

## Documentation

- [cURL Example](example.sh)
- [Open API Specification](./openapi2soapui-rest/src/main/resources/static/api.yaml)
- [Swagger UI](http://localhost:8080/swagger-ui.html) - `http://localhost:8080/swagger-ui.html`
- Find Java Doc in **javadoc** folder
- Java Doc is generated in ./target/site/apidocs` folder using the Maven command 

```sh
mvn javadoc:javadoc
```

## 💛 Sponsors
<p align="center">
	<a href="https://apiaddicts.org/">
    	<img src="https://apiaddicts.cloudappi.net/web/image/4248/LOGOCloudappi2020Versiones-01.png" alt="cloudappi" width="150"/>
        <img src="https://apiaddicts-web.s3.eu-west-1.amazonaws.com/wp-content/uploads/2022/03/17155736/cropped-APIAddicts-logotipo_rojo.png" height = "75">
	</a>
</p>

