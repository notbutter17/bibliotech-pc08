package upeu.bibliotech_v2.validation;

import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Estudiante;

/**
 * Reglas de formato de RN-02 (sin dependencias: se prueba con JUnit puro).
 * La unicidad del DNI se verifica en EstudianteService porque requiere el repositorio.
 */
public class EstudianteValidator {

    public void validar(Estudiante estudiante) {
        if (estudiante == null) {
            throw new DatoInvalidoException("Los datos del estudiante son obligatorios");
        }
        if (estudiante.getDni() == null || estudiante.getDni().isBlank()) {
            throw new DatoInvalidoException("El DNI es obligatorio");
        }
        if (estudiante.getNombre() == null || estudiante.getNombre().isBlank()) {
            throw new DatoInvalidoException("El nombre es obligatorio");
        }
    }
}
