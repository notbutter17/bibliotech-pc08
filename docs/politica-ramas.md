# Política de ramas y revisión · BiblioTech (Git Flow)

Equipo PC08: Ana Belen Arriaga Poquioma (`belenarriagap`), Jose Guillermo Ticona Lipa (`notbutter17`), Jannys Graciela Navarro Acrota (`Stephmarc`) y Willim Yemis Parqui Vilca (`WILLIMPARQUI`).

## Ramas

| Rama | Propósito | Quién escribe | Protección |
|---|---|---|---|
| `main` | Producción. Cada integración dispara `cd.yml` (imágenes + despliegue en Render) | Solo por PR desde `develop` | Protegida: PR obligatorio, **1 aprobación**, checks `Backend` y `Frontend` en verde y rama al día, sin force-push ni borrado, aplica también a administradores |
| `develop` | Integración de funcionalidades terminadas | Por PR desde `feature/*` | CI obligatorio en cada PR |
| `feature/<tema>` | Trabajo aislado de una funcionalidad | Un integrante | — |
| `fix/<tema>` | Corrección puntual | Un integrante | — |

Flujo: `feature/*` → PR a `develop` → revisión de un compañero → merge → PR de release `develop` → `main` → despliegue automático.
Versionado semántico en el archivo `VERSION` (`MAJOR.MINOR.PATCH`); `cd.yml` etiqueta `vX.Y.Z` al integrar en `main`.

## Mensajes de commit

Convencional: `feat:`, `fix:`, `ci:`, `docs:`, `test:`, `perf:`, `chore:` + descripción en presente.

## Criterios para aprobar un PR

1. El CI está en verde: backend (`mvn verify`, JaCoCo ≥ 80 %), frontend (lint, `ng test`, build), SonarCloud (quality gate) y seguridad (Trivy sin HIGH/CRITICAL).
2. Toda regla nueva o modificada tiene su prueba (unitaria o de integración).
3. No hay secretos, contraseñas ni URLs con credenciales en el diff (se usan `.env`, GitHub Secrets o variables de Render).
4. La descripción del PR explica qué cambia y cómo probarlo.
5. Las conversaciones de revisión quedan resueltas antes del merge.
6. Revisa un integrante distinto del autor; el autor no aprueba su propio PR.
