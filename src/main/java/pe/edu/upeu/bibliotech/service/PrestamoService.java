package pe.edu.upeu.bibliotech.service;

import org.springframework.stereotype.Service;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;

@Service
public class PrestamoService {

    public static final int MAXIMO_PRESTAMOS_ACTIVOS = 3;

    private final EstudianteRepository estudianteRepository;

    public PrestamoService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public boolean puedeSolicitarPrestamo(
            int estudianteId,
            int prestamosActivos,
            int ejemplaresDisponibles) {

        Estudiante estudiante = estudianteRepository.buscarPorId(estudianteId)
                .orElseThrow(() -> new EstudianteNoEncontradoException(estudianteId));

        if (estudiante.suspendido()) {
            throw new EstudianteSuspendidoException(estudianteId);
        }
        if (!estudiante.activo()) {
            return false;
        }
        if (prestamosActivos >= MAXIMO_PRESTAMOS_ACTIVOS) {
            return false;
        }
        return ejemplaresDisponibles > 0;
    }
}
