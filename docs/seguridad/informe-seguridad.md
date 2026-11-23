---
title: "Evaluación de riesgos de seguridad en BiblioTech V2"
subtitle: "A01, A02, A03, A05 y A10"
author: "Estudiante: ______________________________"
date: "22 de noviembre de 2026"
lang: es-PE
---

# 1. Objetivo y alcance

El objetivo de esta revisión fue identificar y comprobar riesgos de seguridad en BiblioTech V2, explicar su posible impacto sobre la biblioteca y reconocer los controles presentes en el proyecto. Se revisó la rama `develop` del repositorio `dayvis80/bibliotech`, commit `1145567` del 19 de noviembre de 2026. El ZIP entregado por el docente coincide con ese commit.

La revisión comprendió el backend Spring Boot, el frontend Angular, los archivos de Docker y GitHub Actions y las dependencias del frontend. Para la prueba dinámica se levantó, en una red local y con datos desechables, la imagen oficial `ghcr.io/dayvis80/bibliotech-backend:1.0.0` indicada en `docker-compose.release.yml`. No se probaron sistemas de terceros.

Se aplicó el criterio explicado en clase: **reportado no significa necesariamente explotable**. Por ello, cada conclusión indica si el problema fue comprobado dinámicamente, observado en el código o solamente reportado por una herramienta.

| Riesgo | Resultado de la revisión | Severidad propuesta |
|---|---|---|
| A01 – Control de acceso roto | Comprobado: lectura y registro de préstamos sin autenticación | Alta |
| A02 – Configuración de seguridad incorrecta | Comprobado: contraseña débil escrita y publicada; controles parciales con variables de entorno | Alta |
| A03 – Cadena de suministro de software | Reportado: 3 alertas altas en dependencias de producción; imágenes y Actions sin digest/SHA | Alta, pendiente de validar explotabilidad |
| A05 – Inyección | No comprobada en los endpoints revisados; existen consultas parametrizadas y validación de tipo | Baja/residual |
| A10 – Manejo de condiciones excepcionales | Comprobado: recurso inexistente devuelve 500; la respuesta no expone traza | Media |

# 2. A01 – Control de acceso roto

## Explicación

Este riesgo aparece cuando una persona puede ejecutar funciones o consultar recursos sin que el sistema compruebe su identidad y sus permisos. En BiblioTech, el impacto principal sería que cualquier usuario con acceso a la API podría validar o registrar préstamos en nombre de cualquier estudiante. Esto altera el inventario, reduce ejemplares disponibles y genera registros que no pueden atribuirse a un operador autenticado.

## Ubicación

- `backend/pom.xml`, líneas 32–72: no existe dependencia de Spring Security.
- `backend/src/main/java/upeu/bibliotech_v2/controller/PrestamoController.java`, líneas 18–37.
- Endpoint de lectura: `GET /api/prestamos/validar`.
- Endpoint que modifica datos: `POST /api/prestamos`.

## Control

**Protección encontrada:** no se encontró autenticación, autorización por roles ni identificación del estudiante u operador. `@CrossOrigin(origins = "http://localhost:4200")` limita el origen permitido en navegadores, pero CORS no es un control de autenticación: clientes como `curl`, Postman u otro servidor pueden llamar directamente a la API.

**Control recomendado:** agregar Spring Security; exigir autenticación para toda la API y autorización por rol para operaciones de escritura. Por ejemplo, un estudiante podría consultar su propia situación y solo un bibliotecario podría registrar un préstamo. La prueba de integración debe esperar `401` sin credenciales, `403` con un rol insuficiente y `200/201` con el rol correcto.

## Verificación

**Esperado si existiera el control:** una solicitud sin credenciales debía devolver `401 Unauthorized` o `403 Forbidden`, y no debía cambiar la base de datos.

**Ocurrido:** el `GET` sin cabecera `Authorization` devolvió `200` y `true`. Luego, un `POST` sin credenciales devolvió `200` y creó el préstamo con identificador `1`, además de reducir de 3 a 2 los ejemplares del libro 2.

**Evidencia:**

```text
GET /api/prestamos/validar?estudianteId=1&libroId=1
HTTP/1.1 200
true

POST /api/prestamos?estudianteId=2&libroId=2
HTTP/1.1 200
{"fechaPrestamo":"2026-11-23","estado":"ACTIVO","id":1,...}
```

Conclusión: es una vulnerabilidad **comprobada**, no solo hipotética.

# 3. A02 – Configuración de seguridad incorrecta

## Explicación

Este riesgo ocurre cuando el sistema utiliza valores inseguros, secretos incrustados, funciones de depuración o servicios expuestos innecesariamente. En una biblioteca, una contraseña de base de datos publicada podría permitir leer o modificar estudiantes, libros y préstamos si la base de datos fuera accesible. Mostrar SQL en producción también puede revelar estructura interna en los registros y facilitar el reconocimiento del sistema.

## Ubicación

- `backend/src/main/resources/application.properties`, línea 5: `spring.datasource.password=123456`.
- El mismo archivo, línea 8: `spring.jpa.show-sql=true`.
- `docker-compose.yml` y `docker-compose.release.yml`: las credenciales se reciben mediante variables de entorno.
- `.gitignore`, líneas 17–23: excluye `.env` y configuraciones locales.
- Historial Git: la búsqueda `git log --all -S'123456' -- ...` ubica el valor desde el commit `1019775`.

## Control

**Protecciones encontradas:** los archivos Compose usan `${POSTGRES_PASSWORD}` y `SPRING_DATASOURCE_PASSWORD`, `.env` está ignorado por Git y el perfil de CI recibe la contraseña desde el entorno. Estos controles reducen el riesgo durante el despliegue.

**Debilidad restante:** `application.properties` contiene una contraseña débil en texto claro y el archivo forma parte del repositorio público. Aunque Compose la sobrescriba al desplegar, el valor ya debe considerarse comprometido. Además, `show-sql=true` no es apropiado para producción.

**Control recomendado:** cambiar la propiedad a `spring.datasource.password=${DB_PASSWORD}` (sin valor por defecto real), rotar la contraseña, desactivar `show-sql` en producción y suministrar secretos desde variables protegidas o un gestor de secretos. La base de datos no debe publicar el puerto 5432 fuera de la red interna.

## Verificación

**Esperado:** no encontrar claves reales en archivos rastreados; `.env` no debía aparecer en el historial.

**Ocurrido:** `git ls-files .env` y `git log --all -- .env` no devolvieron resultados, lo cual confirma el control sobre `.env`. Sin embargo, la búsqueda de `password` encontró `spring.datasource.password=123456` en un archivo rastreado y el historial confirmó que el valor fue publicado.

**Evidencia:** archivo `application.properties`, líneas 3–8, y commit `1019775`. El resultado es una configuración insegura **comprobada**, con mitigación parcial en Docker/CI.

# 4. A03 – Fallas en la cadena de suministro de software

## Explicación

La cadena de suministro incluye bibliotecas, imágenes de contenedor, acciones de CI/CD y herramientas usadas para compilar o publicar el sistema. Una dependencia vulnerable o reemplazada puede introducir denegación de servicio, código malicioso o una compilación no reproducible. En BiblioTech esto podría afectar la disponibilidad del catálogo y los préstamos o comprometer las imágenes publicadas en GHCR.

## Ubicación

- `frontend/package.json` y `frontend/package-lock.json`.
- `frontend/package-lock.json`: `@angular/router` 22.2.0, `@angular/forms` 22.2.0 y la dependencia transitiva `zod` 4.6.5.
- `backend/pom.xml` para dependencias Maven.
- `backend/Dockerfile` y `frontend/Dockerfile`: imágenes base por etiqueta (`eclipse-temurin:21-jdk`, `node:22-alpine`, `nginx:alpine`) sin digest.
- `.github/workflows/ci.yml` y `publish.yml`: acciones referidas como `@v3`, `@v4` y `@v6`, no por SHA de commit.

## Control

**Protecciones encontradas:** existe `package-lock.json` con hashes de integridad; CI instala con `npm ci`; Maven hereda versiones administradas desde Spring Boot; el workflow tiene `permissions: contents: read`; y la publicación usa el token temporal `GITHUB_TOKEN` en vez de una contraseña escrita en el repositorio.

**Debilidades encontradas:** no existe un paso de `npm audit` o Dependency-Check en CI, las imágenes base no están fijadas por digest y las Actions no están fijadas a un SHA inmutable. Además, el workflow `demo_secreto.yml` ejecuta `echo` de un secreto; GitHub normalmente lo enmascara, pero imprimir secretos es una práctica insegura que debe eliminarse.

**Control recomendado:** actualizar Angular/Zod a versiones corregidas compatibles, agregar análisis SCA al pipeline, fijar imágenes por digest y Actions por SHA, revisar Dependabot/Renovate y generar un SBOM. Antes de actualizar debe ejecutarse la batería de pruebas.

## Verificación

**Esperado:** cero vulnerabilidades altas o críticas en dependencias de producción.

**Ocurrido:** `npm audit --omit=dev --json` terminó con código 1 y reportó 3 hallazgos de severidad alta: dos avisos para `@angular/router` 22.2.0 y uno para `zod` 4.6.5 (GHSA-mrhx-c7xc-m3w5, CVSS 7.5), transitivo desde `@angular/forms`.

Los dos avisos del router describen denegación de servicio en renderizado del lado del servidor (SSR). BiblioTech se compila como SPA y se sirve con Nginx, por lo que se consideran **reportados, pero no comprobados como explotables en esta arquitectura**. El aviso de Zod también está reportado; no se observó en el código una validación de colecciones grandes que permita afirmar explotación. Aun así, las versiones deben actualizarse porque la herramienta demuestra que la cadena contiene componentes afectados.

No se obtuvo un informe NVD de Maven con Dependency-Check porque la ejecución requiere la clave NVD indicada por el docente. No se inventaron CVE del backend.

# 5. A05 – Inyección

## Explicación

Una inyección ocurre cuando datos controlados por el usuario se interpretan como parte de una consulta o comando. Una inyección SQL en BiblioTech podría permitir consultar, alterar o eliminar información de estudiantes, libros y préstamos.

## Ubicación

- Parámetros `estudianteId` y `libroId` en `PrestamoController.java`, líneas 18–35.
- `PrestamoService.java`, líneas 30–50 y 65–69.
- `PrestamoRepository.java`, líneas 6–7.
- Endpoints revisados: `GET /api/prestamos/validar` y `POST /api/prestamos`.

## Control

**Protección encontrada:** los parámetros del controlador son de tipo `Long`. Spring rechaza texto que no puede convertirse a número. El acceso a datos se realiza con `JpaRepository.findById` y el método derivado `countByEstudianteIdAndEstado`; Spring Data JPA genera consultas con parámetros enlazados. No se encontraron concatenaciones SQL, `nativeQuery`, `EntityManager`, `JdbcTemplate` ni `Statement` en el backend revisado.

**Mejora recomendada:** conservar consultas parametrizadas, añadir validación positiva para identificadores mayores que cero y mantener una prueba de regresión con cargas de inyección. Si se agregan búsquedas por texto, nunca concatenar el término dentro de SQL o JPQL.

## Verificación

**Esperado:** una carga como `1 OR 1=1` no debía ejecutarse como SQL ni devolver datos ajenos; debía rechazarse con `400 Bad Request`.

**Ocurrido:** la solicitud codificada con `estudianteId=1 OR 1=1` devolvió `400 Bad Request` y no produjo un préstamo ni una consulta manipulada. Un valor vacío también devolvió `400`.

**Evidencia:** respuesta HTTP y revisión estática de los tres repositorios. En el alcance revisado, la inyección **no fue comprobada** y existe un control técnico adecuado. El riesgo residual reaparecería si en el futuro se agregan consultas construidas por concatenación.

# 6. A10 – Mal manejo de condiciones excepcionales

## Explicación

Este riesgo se presenta cuando una condición inesperada genera códigos incorrectos, filtra información interna o deja el sistema en un estado inconsistente. Para la biblioteca, respuestas `500` ante errores previsibles dificultan que el frontend informe correctamente al usuario, ensucian los registros y pueden ocultar fallas reales. Una traza o mensaje SQL expuesto también facilitaría ataques.

## Ubicación

- `PrestamoService.java`, líneas 32–34, 48–50 y 61–69.
- Se lanzan excepciones genéricas `RuntimeException` y `orElseThrow()` sin excepción de dominio.
- No existe una clase `@RestControllerAdvice` o un manejador global de errores.
- Endpoint probado: `GET /api/prestamos/validar?estudianteId=999999&libroId=1`.

## Control

**Protección encontrada:** la respuesta HTTP predeterminada de Spring Boot no mostró traza Java, SQL, contraseña ni mensaje interno. Solo incluyó fecha, estado, texto genérico y ruta.

**Debilidad encontrada:** “estudiante no existe” es una condición prevista, pero se representa como `RuntimeException`; por eso termina como error interno `500`. También `registrarPrestamo` actualiza el libro y luego guarda el préstamo sin una anotación `@Transactional`, de modo que una falla en el segundo guardado podría dejar el stock reducido sin préstamo registrado.

**Control recomendado:** crear excepciones de dominio (`RecursoNoEncontradoException`, `ReglaNegocioException`), un `@RestControllerAdvice` que produzca respuestas uniformes y seguras, y devolver `404` para recursos inexistentes, `400` para parámetros inválidos y `409` para reglas de negocio. Añadir `@Transactional` al registro del préstamo y un identificador de correlación para los logs.

## Verificación

**Esperado:** un estudiante inexistente debía devolver `404 Not Found` con un mensaje seguro; una carga no numérica debía devolver `400`; ninguna respuesta debía incluir trazas, SQL o claves.

**Ocurrido:** el identificador `999999` devolvió `500 Internal Server Error`. La entrada no numérica devolvió `400`. En ambos casos el cuerpo fue genérico y no filtró detalles internos.

```json
{
  "timestamp": "2026-11-23T03:43:19.989Z",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/prestamos/validar"
}
```

Conclusión: el manejo es **parcialmente seguro respecto a la exposición de información**, pero incorrecto en semántica HTTP y consistencia transaccional.

# 7. Mejoras priorizadas y conclusión

1. **Prioridad alta – implementar autenticación y autorización.** Es el hallazgo de mayor impacto y fue explotado en la prueba local: una solicitud anónima modificó el inventario.
2. **Prioridad alta – retirar y rotar la contraseña publicada.** Usar variables o un gestor de secretos y desactivar el SQL visible en producción.
3. **Prioridad alta – actualizar y vigilar dependencias.** Corregir los paquetes reportados, integrar SCA en CI y fijar imágenes/Actions por identificadores inmutables.
4. **Prioridad media – normalizar excepciones y transacciones.** Devolver códigos HTTP correctos, respuestas seguras y garantizar que libro y préstamo se guarden de manera atómica.
5. **Prioridad preventiva – conservar consultas parametrizadas.** Mantener pruebas negativas de inyección y validación positiva de entradas.

La revisión demuestra tres problemas relevantes: ausencia total de control de acceso, una credencial publicada y manejo incorrecto de condiciones previsibles. También demuestra que no todo hallazgo de un escáner equivale a una explotación: las alertas de dependencias requieren análisis de uso y el intento de inyección fue rechazado por los controles actuales. Esta distinción permite priorizar primero los riesgos comprobados que afectan directamente la integridad y disponibilidad de BiblioTech.

# Anexo A. Comandos principales de evidencia

```bash
git rev-parse --short HEAD
git ls-files .env
git log --all --oneline -- .env
git log --all -S'123456' --oneline -- backend/src/main/resources/application.properties

npm audit --omit=dev --json

curl -i "http://localhost:18080/api/prestamos/validar?estudianteId=1&libroId=1"
curl -i -X POST "http://localhost:18080/api/prestamos?estudianteId=2&libroId=2"
curl -i "http://localhost:18080/api/prestamos/validar?estudianteId=999999&libroId=1"
curl -i --get "http://localhost:18080/api/prestamos/validar" \
  --data-urlencode "estudianteId=1 OR 1=1" --data-urlencode "libroId=1"
```

# Anexo B. Limitaciones

- Las pruebas dinámicas usaron datos y contenedores locales desechables.
- El backend dinámico fue la imagen oficial `1.0.0` declarada por el proyecto; la lectura estática se hizo sobre el commit `1145567`.
- No se ejecutó Dependency-Check contra NVD por no disponer de una clave NVD. El resultado de `npm audit` sí fue obtenido en la revisión.
- Las fechas HTTP se muestran en UTC; corresponden a la noche del 22 de noviembre de 2026 en Perú.
