curl --location --request POST 'http://localhost:8080/api-openapi-to-soapui/v1/soap-ui-projects' \
--header 'Content-Type: application/json' \
--data-raw '{
  "apiName": "Users",
  "oAuth2Profiles": [
    {
      "profileName": "dev",
      "accessToken": "Ty3vo6cGkJu6DoK1GZUSNcgOj5rQ"
    },
    {
      "profileName": "dev",
      "grantType": "AUTHORIZATION_CODE",
      "clientId": "a2354akdjfasdkfj",
      "scope": "openid, secret",
      "accessTokenPosition": "HEADER",
      "clientSecret": "a2354akdjfasdkfj",
      "accessTokenURI": "http://api.cloudappi.net/token",
      "authorizationURI": "http://api.cloudappi.net/auth",
      "redirectURI": "http://api.cloudappi.net/callback"
    }
  ],
  "openApiSpec": "b3BlbmFwaTogMy4wLjAKaW5mbzoKICB0aXRsZTogU2FtcGxlIEFQSQogIGRlc2NyaXB0aW9uOiBPcHRpb25hbCBtdWx0aWxpbmUgb3Igc2luZ2xlLWxpbmUgZGVzY3JpcHRpb24gaW4gW0NvbW1vbk1hcmtdKGh0dHA6Ly9jb21tb25tYXJrLm9yZy9oZWxwLykgb3IgSFRNTC4KICB2ZXJzaW9uOiAwLjEuOQpzZXJ2ZXJzOgogIC0gdXJsOiBodHRwOi8vYXBpLmV4YW1wbGUuY29tL3YxCiAgICBkZXNjcmlwdGlvbjogT3B0aW9uYWwgc2VydmVyIGRlc2NyaXB0aW9uLCBlLmcuIE1haW4gKHByb2R1Y3Rpb24pIHNlcnZlcgogIC0gdXJsOiBodHRwOi8vc3RhZ2luZy1hcGkuZXhhbXBsZS5jb20KICAgIGRlc2NyaXB0aW9uOiBPcHRpb25hbCBzZXJ2ZXIgZGVzY3JpcHRpb24sIGUuZy4gSW50ZXJuYWwgc3RhZ2luZyBzZXJ2ZXIgZm9yIHRlc3Rpbmc=",
  "testCaseNames": [
    "Success"
  ],
  "headers": []
}'