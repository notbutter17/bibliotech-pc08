# BiblioTech V2 — PC08

Proyecto académico de gestión de préstamos de biblioteca de **notbutter17**, construido con Spring Boot, Angular y PostgreSQL. Su arquitectura sigue el proyecto integrador utilizado en el curso de Pruebas y Despliegue de Software.

## Estrategia de ramas

- `main`: versión estable publicada.
- `develop`: integración de funcionalidades terminadas.
- `feature/*`: trabajo aislado que se integra a `develop` mediante pull request.

La práctica original de validación de estudiantes suspendidos se conserva en el historial anterior de `develop` y en sus ramas de trabajo. La arquitectura full stack se incorporó mediante `feature/alineacion-bibliotech-v2`.

## Configuración local

1. Copia `.env.example` como `.env`.
2. Cambia la contraseña de ejemplo.
3. Ejecuta el proyecto con Docker Compose cuando la rama `develop` contenga la configuración de contenedores.

Los secretos no deben escribirse en archivos rastreados por Git.
