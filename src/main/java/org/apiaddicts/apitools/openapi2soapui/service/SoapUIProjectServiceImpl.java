package org.apiaddicts.apitools.openapi2soapui.service;

import java.io.IOException;

import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectRequest;

@Service
public class SoapUIProjectServiceImpl implements SoapUIProjectService {

	@Override
	public SoapUIProject createSoapUIProject(SoapUIProjectRequest request, OpenAPI openAPI) throws IOException, XmlException, SoapUIException {
		return new SoapUIProject(request.getApiName(), openAPI, request.getOAuth2Profiles(), request.getHeaders(),
				request.getTestCaseNames(), request.getReadOnly(), request.getServerPattern(), request.getMinimalEndpoints(),
				request.getMicrocksHeaders(), request.getGenerateOneOfAnyOf(), request.getValidateSchema(),
				request.getSchemaIsInline(), request.getIsInline(), request.getSchemaPrettyPrint(), request.getHasScopes(),
				request.getApplicationToken(), request.getExamples());
	}

}
