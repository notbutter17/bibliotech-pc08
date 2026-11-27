package upeu.bibliotech_v2.exception;

/** Operación rechazada por una regla de negocio o un conflicto de estado (HTTP 409). */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
