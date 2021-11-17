package org.apiaddicts.apitools.openapi2soapui.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile;
import org.apiaddicts.apitools.openapi2soapui.request.Header;

@Service
public class SoapUIProjectServiceImpl implements SoapUIProjectService {

	@Override
	public SoapUIProject createSoapUIProject(String apiName, OpenAPI openAPI, List<OAuth2Profile> credentials, List<Header> headers,
			Set<String> testCaseNames) throws IOException, XmlException, SoapUIException {
		return new SoapUIProject(apiName, openAPI, credentials, headers, testCaseNames);
	}
	
}
