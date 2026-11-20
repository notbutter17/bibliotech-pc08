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
