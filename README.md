# BiblioTech V2 — PC08

Sistema de préstamos de biblioteca (Spring Boot 4 + Angular 22 + PostgreSQL) y su **estrategia integral de pruebas** (Producto Académico U1).

## Estructura

```
backend/   Spring Boot  · src/test/java/upeu/bibliotech_v2/{unitarias, mockito, integracion}
frontend/  Angular      · src/app/libros (Pantalla 1, /libros) · src/app/prestamos (Pantalla 2, /prestamos)
e2e/       Cypress      · cypress/e2e/01..03-*.cy.ts
postman/   coleccion.json y entorno.json
docs/      informe PDF, camino-basico.md, evidencias/ (capturas y salidas)
database/  init.sql (esquema con isbn y dni únicos + datos semilla)
```

## Reglas de negocio implementadas

| Código | Endpoint | Regla |
|---|---|---|
| RN-01 | `GET/POST /api/libros`, `GET /api/libros/{id}` | ISBN obligatorio y único; título y autor obligatorios; ejemplares ≥ 0; estado calculado |
| RN-02 | `POST /api/estudiantes` | DNI obligatorio y único; nombre obligatorio; estado inicial ACTIVO |
| RN-03 | `POST /api/prestamos` `{estudianteId, libroId}` | libro existe; ejemplares > 0; estudiante ACTIVO; máx. 3 activos; baja un ejemplar |
| RN-04 | `PUT /api/prestamos/{id}/devolucion` | préstamo existe y ACTIVO; sube un ejemplar; pasa a DEVUELTO |

Respuestas: 201 creado · 400 dato inválido · 404 no encontrado · 409 regla de negocio/duplicado. Cuerpo de error: `{"status": n, "mensaje": "..."}`.

## Entorno

Java 21+, Maven 3.9+, Node 22+, PostgreSQL 16/17 (o Docker).

## Levantar la aplicación

Con Docker: copia `.env.example` a `.env`, luego:

```bash
docker compose up -d --build
```

Frontend en http://localhost:4200, API en http://localhost:8080/api.

Sin Docker (desarrollo):

```bash
cd backend && SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bibliotech_db SPRING_DATASOURCE_PASSWORD=<clave> mvn spring-boot:run
cd frontend && npm ci && npx ng serve     # proxy /api -> :8080 (proxy.conf.json)
```

## Comandos de prueba

| Comando (carpeta) | Qué comprueba | Resultado |
|---|---|---|
| `mvn test` (backend) | Unitarias, camino básico y Mockito (sin BD) | 43 ✔ |
| `mvn verify` (backend) | + @WebMvcTest y @SpringBootTest (H2) + JaCoCo ≥ 80 % | 18 ✔ · 96,5 % líneas · 100 % ramas |
| `npx ng test --watch=false` (frontend) | Pruebas Angular con cobertura (activada en angular.json) | 24 ✔ |
| `npx cypress run` (e2e, app levantada) | Flujos E2E 1, 2 y 3 | 6 ✔ |
| `npx newman run postman/coleccion.json -e postman/entorno.json` (raíz, backend levantado) | API | 18 requests · 36 aserciones ✔ |

> Angular 22 usa el builder `unit-test` (Vitest): la opción antigua `--code-coverage` (Karma) ya no existe; el equivalente es `--coverage`, y ya queda activada por defecto en `angular.json`.

Reportes: `backend/target/site/jacoco/index.html` y `frontend/coverage/bibliotech-v2fe/index.html`.

## Ramas

`main` estable · `develop` integración · `feature/*` trabajo aislado (PR hacia `develop`).
Los secretos no deben escribirse en archivos rastreados por Git.
