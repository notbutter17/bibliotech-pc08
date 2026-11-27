-- DATOS INICIALES DE BIBLIOTECH V2
-- PostgreSQL ejecuta este archivo antes de que Spring/Hibernate inicie.
-- Columnas nuevas del Producto U1: libros.isbn y estudiantes.dni (obligatorias y únicas).

CREATE TABLE IF NOT EXISTS estudiantes (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(15) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    estado VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS libros (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    ejemplares_disponibles INTEGER NOT NULL DEFAULT 0 CHECK (ejemplares_disponibles >= 0)
);

CREATE TABLE IF NOT EXISTS prestamos (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT REFERENCES estudiantes(id),
    libro_id BIGINT REFERENCES libros(id),
    fecha_prestamo DATE,
    estado VARCHAR(255)
);

INSERT INTO estudiantes (id, dni, nombre, estado) VALUES
    (1, '70000001', 'Juan Perez', 'ACTIVO'),
    (2, '70000002', 'Maria Quispe', 'ACTIVO'),
    (3, '70000003', 'Carlos Mamani', 'INACTIVO')
ON CONFLICT (id) DO NOTHING;

INSERT INTO libros (id, isbn, titulo, autor, ejemplares_disponibles) VALUES
    (1, '978-0134685991', 'Programacion en Java', 'James Gosling', 5),
    (2, '978-1491999835', 'Desarrollo Web con Angular', 'Equipo Angular', 3),
    (3, '978-0000000003', 'Bases de Datos', 'Autor de prueba', 0)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('estudiantes', 'id'), COALESCE((SELECT MAX(id) FROM estudiantes), 1), true);
SELECT setval(pg_get_serial_sequence('libros', 'id'), COALESCE((SELECT MAX(id) FROM libros), 1), true);
