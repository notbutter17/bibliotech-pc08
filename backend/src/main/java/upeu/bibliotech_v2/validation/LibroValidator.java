package upeu.bibliotech_v2.validation;

import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Libro;

/**
 * Reglas de formato de RN-01 (sin dependencias: se prueba con JUnit puro).
 * La unicidad del ISBN se verifica en LibroService porque requiere el repositorio.
 */
public class LibroValidator {

    public void validar(Libro libro) {
        if (libro == null) {
            throw new DatoInvalidoException("Los datos del libro son obligatorios");
        }
        if (estaVacio(libro.getIsbn())) {
            throw new DatoInvalidoException("El ISBN es obligatorio");
        }
        if (estaVacio(libro.getTitulo())) {
            throw new DatoInvalidoException("El título es obligatorio");
        }
        if (estaVacio(libro.getAutor())) {
            throw new DatoInvalidoException("El autor es obligatorio");
        }
        if (libro.getEjemplaresDisponibles() < 0) {
            throw new DatoInvalidoException("Los ejemplares no pueden ser negativos");
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
