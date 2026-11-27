export interface Libro {
  id?: number;
  isbn: string;
  titulo: string;
  autor: string;
  ejemplaresDisponibles: number;
  estado?: 'DISPONIBLE' | 'NO DISPONIBLE';
}

export interface Estudiante {
  id?: number;
  dni: string;
  nombre: string;
  estado?: 'ACTIVO' | 'INACTIVO';
}

export interface Prestamo {
  id: number;
  estudiante: Estudiante;
  libro: Libro;
  fechaPrestamo: string;
  estado: 'ACTIVO' | 'DEVUELTO';
}

/** Cuerpo de error que devuelve el backend: { status, mensaje }. */
export interface ErrorApi {
  status: number;
  mensaje: string;
}
