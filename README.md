
# 🛠️ OpenAPI2SoapUI ![Release](https://img.shields.io/badge/release-0.1.0-purple) ![Swagger](https://img.shields.io/badge/-soap-%23Clojure?style=flat&logo=swagger&logoColor=white) ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=openjdk&logoColor=white)  [![License: LGPL v3](https://img.shields.io/badge/license-LGPL_v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0) 

[API](./src/main/resources/static/api.yaml) to generate a SoapUI project from an OpenAPI Specification (fka Swagger Specification)

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

[Here](./src/main/resources/static/api.yaml) you can check the definition of the API Swagger to SoapUI

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
- Test Suite: {path}\_{httpMethodInUppercase}\_TestSuite
- Test Case (Default): Success\_TestCase
- Test Case: {testCaseName}\_TestCase
- Test Step: Execution\_{httpMethodInUppercase}\_TestStep

The variables are obtained from:
- apiName: property apiName of request body
- apiVersion: version defined in the 'info' section of the OpenAPI Spec
- path: each path defined in the OpenAPI Spec
- httpMethodInUppercase: each HTTP methods of paths defined in OpenAPI Spec
- testCaseName: each test case name defined in the property testCaseNames of request body

## Technology stack
### Overview

|Technology              |Description                 |
|------------------------|----------------------------|
|Core Framework          |Spring Boot 2               |

### Server - Backend

|Technology                                               |Description                                                                   |
|---------------------------------------------------------|------------------------------------------------------------------------------|
|[JDK 11](https://docs.oracle.com/en/java/javase/11/)                       |Java Development Kit                                                          |
|[Spring Boot 2](https://spring.io/projects/spring-boot)  |Framework to ease the bootstrapping and development of new Spring Applications|
|[Maven 3](https://maven.apache.org)                      |Dependency Management                                                         |
|[Tomcat 9](https://tomcat.apache.org)                    |Server deploy WAR                                                             |

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

* [JDK Installation](https://docs.oracle.com/en/java/javase/11/install/overview-jdk-installation.html#GUID-8677A77F-231A-40F7-98B9-1FD0B48C346A)
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
$ mvn spring-boot:run
```

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

* Restart Tomcat Server
* URL to access: **http://localhost:8080/openapi2soapui/api-openapi-to-soapui/v1/soap-ui-projects**
## Files and Directories Structure

The project directory has a particular directory structure. A representative project is shown below:

### Project Structure

```text
.
├── src
│   └── main
│       └── java
│           ├── org.apiaddicts.apitools.openapi2soapui
│           │ 
│           ├── org.apiaddicts.apitools.openapi2soapui.config
│           │  
│           ├── org.apiaddicts.apitools.openapi2soapui.constants
│           │ 
│           ├── org.apiaddicts.apitools.openapi2soapui.controller
│           │ 
│           ├── org.apiaddicts.apitools.openapi2soapui.error
│           ├── org.apiaddicts.apitools.openapi2soapui.error.exceptions
│           ├── org.apiaddicts.apitools.openapi2soapui.error.validators
│           │     
│           ├── org.apiaddicts.apitools.openapi2soapui.model
│           │
│           ├── org.apiaddicts.apitools.openapi2soapui.request
│           │
│           ├── org.apiaddicts.apitools.openapi2soapui.service
│           │
│           └── org.apiaddicts.apitools.openapi2soapui.util
├── src
│   └── main
│       └── resources
│           ├── static
│           │   └── api.yaml
│           │   
│           ├── application.properties
│           ├── log4j.properties
│           └── messages.properties
├── JRE System Library
├── Maven Dependencies
├── src
├── target
│   └──openapi2soapui-1.0.2
├── .gitlab-ci.yaml
├── lombok.config
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

### Packages

* 	`config` - app configurations;
* 	`constants` - app contants;
* 	`controller` - listen to the client;
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
* 	`resources/messages.properties` - contains the error messages used in the application.
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
- [Open API Specification](./src/main/resources/static/api.yaml)
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

