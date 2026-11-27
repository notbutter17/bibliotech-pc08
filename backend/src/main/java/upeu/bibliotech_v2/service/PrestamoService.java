package upeu.bibliotech_v2.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.bibliotech_v2.exception.RecursoNoEncontradoException;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.List;

/** RN-03 (registrar préstamo) y RN-04 (devolver libro). */
@Service
public class PrestamoService {

    public static final int MAX_PRESTAMOS_ACTIVOS = 3;

    private final EstudianteRepository estudianteRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public PrestamoService(EstudianteRepository estudianteRepository,
                           LibroRepository libroRepository,
                           PrestamoRepository prestamoRepository) {
        this.estudianteRepository = estudianteRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    /**
     * RN-03. Evalúa cuatro decisiones en orden (ver docs/camino-basico.md; V(G)=5):
     * D1 ¿el libro existe?  D2 ¿tiene ejemplares?  D3 ¿estudiante ACTIVO?  D4 ¿menos de 3 activos?
     */
    @Transactional
    public Prestamo registrarPrestamo(Long estudianteId, Long libroId) {
        Libro libro = libroRepository.findByIdParaActualizar(libroId).orElse(null);         // N1 (fila bloqueada)
        if (libro == null) {                                                                 // N1 · D1
            throw new RecursoNoEncontradoException("El libro no existe");                    // N2
        }
        if (libro.getEjemplaresDisponibles() <= 0) {                                         // N3 · D2
            throw new ReglaNegocioException("El libro no tiene ejemplares disponibles");     // N4
        }
        Estudiante estudiante = estudianteRepository.findById(estudianteId)                  // N5
                .filter(Estudiante::esActivo)
                .orElse(null);
        if (estudiante == null) {                                                            // N5 · D3
            throw new ReglaNegocioException("El estudiante no existe o no está ACTIVO");     // N6
        }
        long activos = prestamoRepository.countByEstudianteIdAndEstado(estudianteId, Prestamo.ACTIVO); // N7
        if (activos >= MAX_PRESTAMOS_ACTIVOS) {                                              // N7 · D4
            throw new ReglaNegocioException("El estudiante ya tiene 3 préstamos activos");   // N8
        }
        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() - 1);                // N9
        libroRepository.save(libro);
        return prestamoRepository.save(
                new Prestamo(estudiante, libro, LocalDate.now(), Prestamo.ACTIVO));         // N9 → N10 (fin)
    }

    /**
     * RN-04. Decisiones: D1 ¿el préstamo existe?  D2 ¿está ACTIVO?  → V(G)=3
     */
    @Transactional
    public Prestamo devolverLibro(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId).orElse(null);           // N1
        if (prestamo == null) {                                                              // N1 · D1
            throw new RecursoNoEncontradoException("El préstamo no existe");                 // N2
        }
        if (!Prestamo.ACTIVO.equals(prestamo.getEstado())) {                                 // N3 · D2
            throw new ReglaNegocioException("El préstamo ya fue devuelto");                  // N4
        }
        Libro libro = libroRepository.findByIdParaActualizar(prestamo.getLibro().getId())    // N5 (fila bloqueada)
                .orElse(prestamo.getLibro());
        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() + 1);
        libroRepository.save(libro);
        prestamo.setEstado(Prestamo.DEVUELTO);
        return prestamoRepository.save(prestamo);                                            // N5 → N6 (fin)
    }
}
