package pe.edu.upeu.bibliotech.service;

public class EstudianteNoEncontradoException extends RuntimeException {

    public EstudianteNoEncontradoException(int estudianteId) {
        super("No existe el estudiante con id " + estudianteId);
    }
}
