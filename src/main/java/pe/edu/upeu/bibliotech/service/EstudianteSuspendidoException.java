package pe.edu.upeu.bibliotech.service;

public class EstudianteSuspendidoException extends IllegalStateException {

    public EstudianteSuspendidoException(int estudianteId) {
        super("El estudiante con id " + estudianteId + " está suspendido");
    }
}
