package upeu.bibliotech_v2.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.validation.EstudianteValidator;

import java.util.List;

/** RN-02: registrar estudiantes. */
@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final EstudianteValidator validator = new EstudianteValidator();

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public List<Estudiante> listar() {
        return estudianteRepository.findAll();
    }

    @Transactional
    public Estudiante registrar(Estudiante estudiante) {
        validator.validar(estudiante);
        estudiante.setDni(estudiante.getDni().trim());
        if (estudianteRepository.existsByDni(estudiante.getDni())) {
            throw new ReglaNegocioException("Ya existe un estudiante con el DNI " + estudiante.getDni());
        }
        estudiante.setId(null);
        estudiante.setEstado(Estudiante.ACTIVO); // estado inicial siempre ACTIVO
        return estudianteRepository.save(estudiante);
    }
}
