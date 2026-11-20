# BiblioTech

Proyecto de la Práctica Calificada 08 de Pruebas y Despliegue de Software.

## Requisitos

- Java 21
- Maven 3.9 o superior

## Ejecutar pruebas

```bash
mvn test
```

## Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La API expone `GET /api/prestamos/validar` con los parámetros `estudianteId`, `prestamosActivos` y `ejemplaresDisponibles`.

## Regla del Equipo 1

Un estudiante suspendido no puede solicitar préstamos. La validación se ejecuta después de confirmar que el estudiante existe y antes de revisar las demás reglas de elegibilidad.
