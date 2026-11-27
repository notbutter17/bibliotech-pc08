package upeu.bibliotech_v2.exception;

/** Datos de entrada que no cumplen las reglas de validación (HTTP 400). */
public class DatoInvalidoException extends RuntimeException {
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
