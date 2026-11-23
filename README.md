# BiblioTech V2 — PC08

Proyecto académico de gestión de préstamos de biblioteca de **notbutter17**, construido con Spring Boot, Angular y PostgreSQL. Su arquitectura sigue el proyecto integrador utilizado en el curso de Pruebas y Despliegue de Software.

## Estrategia de ramas

- `main`: versión estable publicada.
- `develop`: integración de funcionalidades terminadas.
- `feature/*`: trabajo aislado que se integra a `develop` mediante pull request.

La práctica original de validación de estudiantes suspendidos se conserva en el historial anterior de `develop` y en sus ramas de trabajo. La arquitectura full stack se incorporó mediante `feature/alineacion-bibliotech-v2`.

## Configuración local

1. Copia `.env.example` como `.env`.
2. Cambia la contraseña de ejemplo por una contraseña local segura.
3. Construye e inicia toda la aplicación:

   ```bash
   docker compose up -d --build
   ```

4. Abre el frontend en `http://localhost:4200`.
5. La API queda disponible en `http://localhost:8080/api/prestamos`.

Para detener los contenedores ejecuta `docker compose down`. Los datos de PostgreSQL se conservan en el volumen `postgres_data`; usa `docker compose down -v` únicamente si deseas reiniciar también los datos.

## Documentación y verificaciones

- Informe entregable: `docs/seguridad/Informe_Seguridad_BiblioTech.pdf`.
- Versión editable: `docs/seguridad/Informe_Seguridad_BiblioTech.docx`.
- Fuente del informe: `docs/seguridad/informe-seguridad.md`.
- Estrategia de pruebas: `docs/estrategias-pruebas.md`.
- CI: Maven/JUnit para el backend y ESLint/build para el frontend.

Los secretos no deben escribirse en archivos rastreados por Git.
