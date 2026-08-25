package org.apiaddicts.apitools.openapi2soapui.service;

import java.io.IOException;

import com.eviware.soapui.support.SoapUIException;
import io.swagger.v3.oas.models.OpenAPI;
import org.apiaddicts.apitools.openapi2soapui.model.SoapUIProject;
import org.apiaddicts.apitools.openapi2soapui.request.SoapUIProjectRequest;
import org.apache.xmlbeans.XmlException;

public interface SoapUIProjectService {
    SoapUIProject createSoapUIProject(SoapUIProjectRequest request, OpenAPI openAPI) throws IOException, XmlException, SoapUIException;
}
