package org.apiaddicts.apitools.openapi2soapui.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.eviware.soapui.support.SoapUIException;
import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import org.apiaddicts.apitools.openapi2soapui.request.OAuth2Profile;
import org.apiaddicts.apitools.openapi2soapui.request.Header;
import org.apache.xmlbeans.XmlException;

public interface SoapUIProjectService {
    SoapUIProject createSoapUIProject(String apiName, OpenAPI openAPI, List<OAuth2Profile> oAuth2Profiles, List<Header> headers, Set<String> testCases) throws IOException, XmlException, SoapUIException;
}
