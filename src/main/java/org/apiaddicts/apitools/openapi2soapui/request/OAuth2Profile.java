package org.apiaddicts.apitools.openapi2soapui.request;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import org.apiaddicts.apitools.openapi2soapui.error.validators.AuthenticationConditional;

@Getter
@Setter
@AuthenticationConditional(
		selected = "grantType",
		values = {"AUTHORIZATION_CODE"},
		required = {"clientId", "clientSecret", "accessTokenURI", "authorizationURI", "redirectURI", "accessTokenPosition"},
		message="{validation.notEmpty.oAuth2Profiles.attribute}")
@AuthenticationConditional(
		selected = "grantType",
		values = {"CLIENT_CREDENTIALS"},
		required = {"clientId", "clientSecret", "accessTokenURI", "accessTokenPosition"},
		message="{validation.notEmpty.oAuth2Profiles.attribute}")
@AuthenticationConditional(
		selected = "grantType",
		values = {"RESOURCE_OWNER_PASSWORD_CREDENTIALS"},
		required = {"clientId", "clientSecret", "username", "password", "accessTokenURI", "accessTokenPosition"},
		message="{validation.notEmpty.oAuth2Profiles.attribute}")
@AuthenticationConditional(
		selected = "grantType",
		values = {"IMPLICIT"},
		required = {"clientId", "authorizationURI", "redirectURI", "accessTokenPosition"},
		message="{validation.notEmpty.oAuth2Profiles.attribute}")
		
public class OAuth2Profile {
	
	@NotEmpty(message = "{validation.notEmpty.oAuth2Profiles.profileName}")
	@JsonProperty("profileName")
	private String profileName;

	@JsonProperty("grantType")
	private GrantType grantType;
	
	@JsonProperty("clientId")
	private String clientId;
	
	@JsonProperty("clientSecret")
	private String clientSecret;
	
	@JsonProperty("accessTokenURI")
	private String accessTokenURI;
	
	@JsonProperty("authorizationURI")
	private String authorizationURI;
	
	@JsonProperty("redirectURI")
	private String redirectURI;
	
	@JsonProperty("accessToken")
	private String accessToken;
	
	@JsonProperty("username")
	private String username;
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("accessTokenPosition")
	private AccessTokenPosition accessTokenPosition;
	
	@JsonProperty("scope")
	private String scope;
}
