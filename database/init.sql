-- DATOS INICIALES DE BIBLIOTECH V2

-- PostgreSQL ejecuta este archivo antes de que Spring/Hibernate inicie.
-- Por eso se crea primero el esquema mínimo y luego se cargan los datos.
CREATE TABLE IF NOT EXISTS estudiantes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255),
    estado VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS libros (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255),
    autor VARCHAR(255),
    ejemplares_disponibles INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS prestamos (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT,
    libro_id BIGINT,
    fecha_prestamo DATE,
    estado VARCHAR(255)
);

-- Estudiantes
INSERT INTO estudiantes (id, nombre, estado)
VALUES
    (1, 'Juan Perez', 'ACTIVO'),
    (2, 'Maria Quispe', 'ACTIVO'),
    (3, 'Carlos Mamani', 'SUSPENDIDO')
ON CONFLICT (id) DO NOTHING;

-- Libros
INSERT INTO libros (id, titulo, autor, ejemplares_disponibles)
VALUES
    (1, 'Programacion en Java', 'James Gosling', 5),
    (2, 'Desarrollo Web con Angular', 'Equipo Angular', 3),
    (3, 'Bases de Datos', 'Autor de prueba', 0)
ON CONFLICT (id) DO NOTHING;

-- Ajustar las secuencias de los IDs
SELECT setval(
    pg_get_serial_sequence('estudiantes', 'id'),
    COALESCE((SELECT MAX(id) FROM estudiantes), 1),
    true
);

SELECT setval(
    pg_get_serial_sequence('libros', 'id'),
    COALESCE((SELECT MAX(id) FROM libros), 1),
    true
);
