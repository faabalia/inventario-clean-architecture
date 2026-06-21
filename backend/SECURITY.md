# Política de Seguridad

## Alcance

Este es un proyecto académico (Trabajo de Fin de Máster). No está destinado a uso en producción. Las medidas de seguridad aplicadas reflejan buenas prácticas del mundo real con fines educativos.

## Hardening Aplicado

- **CORS**: restringido a los orígenes configurados (`app.cors.allowed-origins`)
- **Cabeceras de seguridad**: `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy`, `Content-Security-Policy` aplicadas mediante filtro de servlet
- **Hardening de paginación**: tamaño máximo de página limitado a 100 (OWASP API4:2023)
- **Validación de entrada**: Jakarta Bean Validation en todos los cuerpos de petición
- **Respuestas de error uniformes**: no se exponen stack traces ni detalles internos a los clientes

## Vulnerabilidades Conocidas en Dependencias Transitivas

Estos CVEs afectan a dependencias transitivas incluidas por Spring Boot. Se documentan aquí como riesgos conocidos y evaluados.

### commons-lang3 3.17.0 — CVE-2025-48924 (CVSS 5.3)

| Campo      | Detalle |
|------------|---------|
| Severidad  | Media (5.3) |
| Estado     | Información insuficiente — sin corrección disponible a fecha 2026-06-21 |
| Versión    | 3.17.0 (última versión estable) |
| Acción     | No existe ruta de actualización. Riesgo aceptado para el alcance académico. |

### commons-compress — CVE-2024-25710 (CVSS 8.1) / CVE-2024-26308 (CVSS 5.5)

| Campo      | Detalle |
|------------|---------|
| Severidad  | Alta (8.1) / Media (5.5) |
| Afectado   | commons-compress < 1.26.0 |
| Resolución | Versión fijada a 1.27.1 en `pom.xml` mediante la propiedad `<commons-compress.version>` |
| Estado     | Mitigado |

## Notificación de Problemas

Este es un proyecto académico y no dispone de un proceso formal de divulgación de vulnerabilidades. Si encuentras algún problema relevante, abre un issue en el repositorio de GitHub.
