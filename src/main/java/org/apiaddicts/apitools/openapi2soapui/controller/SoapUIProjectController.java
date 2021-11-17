package org.apiaddicts.apitools.openapi2soapui.controller;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.models.OpenAPI;

import org.springframework.beans.factory.annotation.Autowired;

import org.apiaddicts.apitools.openapi2soapui.error.exceptions.APIVersionNotFoundException;
import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectRequest;
import org.apiaddicts.apitools.openapi2soapui.service.SoapUIProjectService;
import org.apiaddicts.apitools.openapi2soapui.util.SerializedDataUtils;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("${basepath}")
public class SoapUIProjectController {

	@Autowired
    private SoapUIProjectService soapUIProjectService;
	
	@PostMapping(value = "soap-ui-projects", consumes = "application/json", produces = "application/xml")
    public String newSoapUIProject(@Valid @RequestBody SoapUIProjectRequest newSoapUIProject) throws Exception {
		OpenAPI openAPI = SerializedDataUtils.parseOpenAPIContent(newSoapUIProject.getOpenAPIContent());
		if (openAPI != null && openAPI.getInfo() != null && openAPI.getInfo().getVersion() == null) {
			throw new APIVersionNotFoundException("Version not found in OpenAPI");
		}
    	SoapUIProject soapUIProject = soapUIProjectService.createSoapUIProject(newSoapUIProject.getApiName(), openAPI, 
    			newSoapUIProject.getOAuth2Profiles(), newSoapUIProject.getHeaders(), newSoapUIProject.getTestCaseNames());
    	String projectContent = soapUIProject.getFileContent();
    	soapUIProject.deleteTemporaryFile();
    	return projectContent;
    }
	
}
