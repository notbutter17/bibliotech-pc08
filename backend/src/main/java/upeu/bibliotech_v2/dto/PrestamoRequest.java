package upeu.bibliotech_v2.dto;

/** Cuerpo de POST /api/prestamos. */
public record PrestamoRequest(Long estudianteId, Long libroId) {
}
