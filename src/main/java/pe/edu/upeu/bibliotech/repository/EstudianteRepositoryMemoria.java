package pe.edu.upeu.bibliotech.repository;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bibliotech.model.Estudiante;

@Repository
public class EstudianteRepositoryMemoria implements EstudianteRepository {

    private final Map<Integer, Estudiante> estudiantes = Map.of(
            1, new Estudiante(1, "Ana Torres", true, false),
            2, new Estudiante(2, "Luis Quispe", false, false),
            3, new Estudiante(3, "Rosa Mamani", true, true));

    @Override
    public Optional<Estudiante> buscarPorId(int estudianteId) {
        return Optional.ofNullable(estudiantes.get(estudianteId));
    }
}
