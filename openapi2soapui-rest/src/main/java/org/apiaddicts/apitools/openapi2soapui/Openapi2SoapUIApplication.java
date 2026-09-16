package org.apiaddicts.apitools.openapi2soapui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class Openapi2SoapUIApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Openapi2SoapUIApplication.class, args);
    }

}
