# Escaneo de dependencias del backend · Trivy

Herramienta: Trivy (base de datos NVD/GHSA), sobre el jar empaquetado `bibliotech_v2-0.0.1-SNAPSHOT.jar` (versiones exactas de cada librería).
Se ejecuta en cada push en el job **Seguridad** de `ci.yml` (falla si hay HIGH/CRITICAL con parche) y se complementa con Dependabot semanal.
Secretos: el escaneo de secretos de Trivy sobre todo el repositorio no encontró credenciales (0 hallazgos).

## Hallazgos (escaneo inicial, PR #7) y acción tomada

| # | Librería (versión) | CVE | Severidad | Riesgo para BiblioTech | Versión corregida | Acción |
|---|---|---|---|---|---|---|
| 1 | tomcat-embed-core 11.0.24 | CVE-2026-65182 | CRITICAL | Evasión de restricciones de seguridad | 11.0.25 | `tomcat.version=11.0.25` en `pom.xml` |
| 2 | tomcat-embed-core 11.0.24 | CVE-2026-65637 | CRITICAL | Validación de entrada HTTP/2 | 11.0.25 | idem |
| 3 | tomcat-embed-core 11.0.24 | CVE-2026-65905 | CRITICAL | Bypass de autenticación DIGEST (no se usa, pero el código está presente) | 11.0.25 | idem |
| 4 | tomcat-embed-core 11.0.24 | CVE-2026-68525 | CRITICAL | Acceso no autorizado vía FORM auth | 11.0.25 | idem |
| 5 | jackson-core 3.1.5 | CVE-2026-89407 | HIGH | DoS por backtracking de expresiones regulares en JSON de entrada | 3.1.7 | `jackson-bom.version=3.1.7` |
| 6 | jackson-core 3.1.5 | CVE-2026-89425 | HIGH | DoS por crecimiento ilimitado de StringBuilder | 3.1.7 | idem |
| 7 | jackson-databind 3.1.5 | CVE-2026-68497 | HIGH | DoS de CPU al parsear números (los endpoints POST reciben JSON) | 3.1.7 | idem |
| 8 | jackson-databind 3.1.5 | CVE-2026-91776 | HIGH | DoS por caché ilimitada | 3.1.7 | idem |
| 9 | jackson-databind 3.1.5 | CVE-2026-91777 | HIGH | DoS por referencias adelantadas | 3.1.7 | idem |
| 10 | jackson-databind 3.1.5 | CVE-2026-19032 | MEDIUM | Resolución de esquemas URI en `Path` | 3.1.7 | idem |
| 11 | jackson-databind 3.1.5 | CVE-2026-83557 | MEDIUM | — | 3.1.7 | idem |

**Resultado tras la corrección:** 0 vulnerabilidades (todas las severidades) — `docs/evidencias-u2/trivy-backend-jar-despues.txt`.
Las 61 pruebas del backend siguen en verde con las nuevas versiones.
