---
name: generar-proyecto-soapui
description: Enseña cómo llamar la API propia de este repo (openapi2soapui) para generar un proyecto SoapUI en XML a partir de un spec OpenAPI, incluyendo el contrato completo del request (parámetros, defaults, validaciones). Úsala cuando el usuario pida generar el proyecto/colección SoapUI con la API, llamar al endpoint de openapi2soapui, crear la colección vía API, o necesite saber qué parámetros/configuración acepta la generación (oAuth2Profiles, headers, customAuthorizationsFile, testCaseNames, flags como readOnly/hasScopes/validateSchema, etc.), incluso si no menciona el nombre exacto del endpoint. NO cubre ejecutar las pruebas generadas con SoapUI TestRunner ni levantar el servicio con Docker — para eso no uses esta skill.
---

# Generar proyecto SoapUI vía API de openapi2soapui

Esta skill cubre **solo** cómo llamar el endpoint de este repo que genera un proyecto SoapUI a partir de un spec OpenAPI, y qué configuración acepta. No cubre ejecutar el proyecto generado (SoapUI TestRunner) ni levantar el servicio (Docker/Maven) — si el usuario pide eso, es trabajo aparte.

## Paso 0 — obtener la URL base (obligatorio)

La URL base del servicio (host:puerto, ej. `http://localhost:8080`) **nunca se asume**. Si no está ya confirmada en la conversación actual, pregúntala al usuario antes de construir cualquier request. No uses `localhost:8080` por defecto sin que el usuario lo confirme — puede estar corriendo en otro puerto, en Docker con otro mapeo, o en un host remoto.

El basepath del endpoint sí es fijo (viene de `application.properties`): `/api-openapi-to-soapui/v1`.

## Construir el request

1. **Codificar el spec OpenAPI a base64.**

   Bash:
   ```bash
   SPEC_B64=$(base64 -w0 archivo.yaml)
   ```

   PowerShell:
   ```powershell
   $SpecB64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("archivo.yaml"))
   ```

2. **Armar el JSON body.** Mínimo viable:
   ```json
   {
     "apiName": "MiApi",
     "openApiSpec": "<base64>",
     "headers": []
   }
   ```
   Ver la tabla completa de parámetros más abajo para agregar configuración opcional.

3. **Llamar el endpoint:**
   ```
   POST {baseUrl}/api-openapi-to-soapui/v1/soap-ui-projects
   Content-Type: application/json
   ```
   La respuesta (`produces: application/xml`) es el **XML crudo del proyecto SoapUI** — no viene envuelto en JSON. Guarda el body de la respuesta directo a un archivo `.xml`.

   Ejemplo bash:
   ```bash
   curl -s -X POST "$BASE_URL/api-openapi-to-soapui/v1/soap-ui-projects" \
     -H "Content-Type: application/json" \
     --data @request.json \
     -o proyecto-soapui.xml \
     -w "HTTP %{http_code}\n"
   ```

## Referencia completa de parámetros (`SoapUIProjectRequest`)

Solo `apiName` y `openApiSpec` son requeridos. Todo lo demás es opcional.

| Parámetro | Tipo | Default | Efecto |
|---|---|---|---|
| `apiName` | String | — (requerido) | Nombre base del proyecto/servicio generado |
| `openApiSpec` | String (base64) | — (requerido) | Contenido del spec OpenAPI v2 o v3, codificado en base64 |
| `oAuth2Profiles` | Lista de OAuth2Profile | — | Perfiles de autenticación OAuth2 a agregar al proyecto (ver sección abajo) |
| `testCaseNames` | Set de String | ninguno | Nombres de test cases custom adicionales; cada nombre debe ser no-vacío |
| `headers` | Lista de `{key,value}` | ninguno | Headers aplicados a todos los recursos generados |
| `customAuthorizationsFile` | Lista de CustomAuthorizationRequest | — | Requests de bootstrap de auth previos a las pruebas (ver sección abajo) |
| `readOnly` | Boolean | `false` | Solo genera test cases para métodos GET/OPTIONS |
| `serverPattern` | String (ej. `"%dev%"`) | primer server del spec | Filtra qué `server` del spec usar, por substring envuelto en `%`; si no matchea o se omite, usa el primero declarado |
| `minimalEndpoints` | Boolean | `false` | Colapsa la generación de `CaseErrorRequired{Field}` a como máximo uno por operación en vez de uno por campo requerido |
| `microcksHeaders` | Boolean | `false` | Agrega header `X-Microcks-Response-Name`; si el usuario ya manda un header custom con ese mismo nombre, se preserva el del usuario |
| `generateOneOfAnyOf` | Boolean | `false` | Resuelve `oneOf`/`anyOf` al primer candidato al generar ejemplos. `allOf` siempre se mergea, sin importar este flag |
| `validateSchema` | Boolean | `true` | Agrega el Script Assertion que valida el JSON Schema de la respuesta. El assertion de status code se agrega siempre, sin importar este flag |
| `schemaIsInline` | Boolean | `false` | `false` = schema como SoapUI Project Property referenciada vía `context.expand`; `true` = schema literal embebido en el script |
| `schemaPrettyPrint` | Boolean | `true` | Schema con indentación (`true`) vs compacto (`false`) |
| `isInline` | Boolean | `false` | Controla si los valores de ejemplo del **body** van como Project Property o literal. Los valores de **query params siempre son literales**, sin importar este flag |
| `hasScopes` | Boolean | `false` | Genera test cases `OkScope{profileName}` extra por perfil OAuth2. No tiene efecto si `oAuth2Profiles` tiene 0 o 1 entradas |
| `applicationToken` | Boolean | `false` | Solo relevante si `hasScopes=true`; genera casos `OkApplicationToken{profileName}` para perfiles con `grantType=CLIENT_CREDENTIALS` |
| `numberOfScopes` | Integer | `1` | Solo relevante si `hasScopes=true`; valores menores a 1 se tratan como 1 |
| `examples` | ExamplesConfig | — | Overrides de valores de ejemplo (ver sección abajo) |

### `examples` (ExamplesConfig)

```json
{
  "examples": {
    "successful": { "string": "...", "number": 1, "boolean": true, "date": "2020-01-01", "dateTime": "2020-01-01T23:59:59", "array": "[1,2,3]", "object": "{\"id\":1}" },
    "wrong": { "string": "...", "number": -1, "boolean": false, "date": "invalid", "dateTime": "invalid", "array": "[]", "object": "{}" }
  }
}
```
- `successful` sobreescribe los valores usados en los casos positivos (`CaseOkAllProperties`/`CaseOkRequiredProperties`).
- `wrong` sobreescribe los valores usados en los negativos `CaseErrorRequired{Field}`, en vez de omitir/vaciar el campo.
- Ambos solo sustituyen valores escalares hoja (string/number/boolean/date/dateTime/array/object) — no afectan cómo se resuelve `oneOf`/`anyOf`/`allOf`.
- En query params, si el formato es reconocido (ej. `email`), la herramienta genera una muestra realista que tiene precedencia sobre el `"string"` configurado.

## OAuth2Profiles en detalle

Dos formas de definir un perfil:

**Ya tengo el token:**
```json
{ "profileName": "prod", "accessToken": "abc123..." }
```

**Necesito que se obtenga el token** (agrega `grantType` y los campos que ese grant type requiere):
```json
{
  "profileName": "dev",
  "grantType": "AUTHORIZATION_CODE",
  "clientId": "...",
  "clientSecret": "...",
  "scope": "openid, secret",
  "accessTokenPosition": "HEADER",
  "accessTokenURI": "https://api.example.com/token",
  "authorizationURI": "https://api.example.com/auth",
  "redirectURI": "https://api.example.com/callback"
}
```

`profileName` siempre es requerido. Los demás campos son condicionalmente requeridos según `grantType` — si falta uno, el error 1208 indica cuál:

| `grantType` | Campos que se vuelven obligatorios |
|---|---|
| `AUTHORIZATION_CODE` | `clientId`, `clientSecret`, `accessTokenURI`, `authorizationURI`, `redirectURI`, `accessTokenPosition` |
| `CLIENT_CREDENTIALS` | `clientId`, `clientSecret`, `accessTokenURI`, `accessTokenPosition` |
| `RESOURCE_OWNER_PASSWORD_CREDENTIALS` | `clientId`, `clientSecret`, `username`, `password`, `accessTokenURI`, `accessTokenPosition` |
| `IMPLICIT` | `clientId`, `authorizationURI`, `redirectURI`, `accessTokenPosition` |

`accessTokenPosition` es uno de: `HEADER`, `BODY`, `QUERY`.

## customAuthorizationsFile en detalle

Cada entrada define un request de bootstrap de autenticación (ej. un fetch de token) que se ejecuta antes de las pruebas normales:

```json
{
  "name": "GetToken",
  "method": "POST",
  "endpoint": "https://api.example.com/security/token",
  "mediaType": "application/x-www-form-urlencoded",
  "body": "grant_type=client_credentials&client_id={{client_id}}&client_secret={{client_secret}}",
  "headers": []
}
```

- `name`, `method`, `endpoint` son requeridos. `method` es case-insensitive pero debe matchear `GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS`.
- `headers`, `mediaType`, `body` son opcionales.
- Genera una **TestSuite separada** llamada `authorizations_{apiName}_{apiVersion}-Suite`, ubicada antes de las test suites normales por endpoint, con un TestCase `{method}_Case{name}` por cada entrada.

## Diagnosticar un 400 rápido

| Código | Causa |
|---|---|
| 1001 | `apiName` vacío o faltante |
| 1002 | `openApiSpec` vacío o faltante |
| 1100 | El contenido de `openApiSpec` no es YAML/JSON válido tras decodificar el base64 |
| 1101 | El contenido no cumple la estructura de OpenAPI v2/v3 |
| 1102 | No se encontró `info.version` en el spec |
| 1208 | `oAuth2Profiles` — falta un campo condicionalmente requerido según el `grantType` (el mensaje indica cuál) |
| 1301 / 1302 | `headers.key` o `headers.value` vacío |
| 1401 | `testCaseNames` tiene un item vacío |
| 1501 / 1502 / 1503 | `customAuthorizationsFile` — falta `name`/`method`/`endpoint` |
| 1504 | `customAuthorizationsFile.method` no matchea los verbos HTTP permitidos |

## Nota sobre el spec propio publicado

El propio `api.yaml` del servicio (`src/main/resources/static/api.yaml`) tiene un typo conocido en el discriminator `oneOf` de `OAuth2ProfileToGetToken`: el mapping entre `IMPLICIT` y `RESOURCE_OWNER_PASSWORD_CREDENTIALS` está invertido. No afecta la validación real (que corre en Java vía `AuthenticationConditionalValidator`), solo es ruido en esa documentación — no te confundas si lo comparás contra ese YAML.

## Fuera de alcance

Esta skill no cubre:
- Ejecutar el proyecto generado con SoapUI TestRunner
- Levantar el servicio (Docker Compose / Maven)
- Modificar el XML del proyecto ya generado
