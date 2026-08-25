# Petstore Simplified (GET /pet/findByStatus) - Resultado de Ejecucion

- Spec fuente: `../petstore-simplified.yaml`
- Proyecto generado: `petstore-simplified-soapui-project.xml` (1 operacion, 4 test cases)
- Server: `https://petstore.swagger.io/v2` (absoluto en spec, sin override)
- Comando:
  ```
  testrunner.bat petstore-simplified-soapui-project.xml
  ```

## Totales
- FAILED: 4
- PASSED: 0

## Causas de fallo
- `CaseOkAllProperties` / `CaseOkRequiredProperties`: HTTP 200 ok, pero schema validation falla — data real del server tiene pets sin `name`/`photoUrls`
- `CaseErrorStatusCode400` / `CaseErrorRequiredStatus`: esperaba 400, server responde 200 igual (no valida status ni required param)

No son bugs de la herramienta: comportamiento real y conocido del servidor demo publico `petstore.swagger.io/v2`.

Logs detallados: `*-FAILED.txt` en esta carpeta. Log completo: `run-log.txt`.
