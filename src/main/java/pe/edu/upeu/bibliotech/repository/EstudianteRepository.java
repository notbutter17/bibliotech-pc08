package pe.edu.upeu.bibliotech.repository;

import java.util.Optional;
import pe.edu.upeu.bibliotech.model.Estudiante;

public interface EstudianteRepository {

    Optional<Estudiante> buscarPorId(int estudianteId);
}
