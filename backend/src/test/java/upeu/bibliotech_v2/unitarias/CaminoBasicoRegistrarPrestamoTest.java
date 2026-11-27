package upeu.bibliotech_v2.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import upeu.bibliotech_v2.exception.RecursoNoEncontradoException;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.repository.PrestamoRepository;
import upeu.bibliotech_v2.service.PrestamoService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Método del camino básico · registrarPrestamo() (RN-03).
 * 4 decisiones (D1..D4) → V(G) = 4 + 1 = 5 caminos independientes; un caso por camino.
 */
@DisplayName("Camino básico · registrarPrestamo()  V(G)=5")
class CaminoBasicoRegistrarPrestamoTest {

    private LibroRepository libroRepo;
    private EstudianteRepository estudianteRepo;
    private PrestamoRepository prestamoRepo;
    private PrestamoService service;

    @BeforeEach
    void setUp() {
        libroRepo = mock(LibroRepository.class);
        estudianteRepo = mock(EstudianteRepository.class);
        prestamoRepo = mock(PrestamoRepository.class);
        service = new PrestamoService(estudianteRepo, libroRepo, prestamoRepo);
    }

    @Test
    @DisplayName("C1 · 1→2→10 · El libro no existe → 404")
    void c1_libroNoExiste() {
        // Arrange
        when(libroRepo.findByIdParaActualizar(99L)).thenReturn(Optional.empty());
        // Act
        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.registrarPrestamo(1L, 99L));
        // Assert
        assertEquals("El libro no existe", ex.getMessage());
    }

    @Test
    @DisplayName("C2 · 1→3→4→10 · Libro sin ejemplares → rechazo")
    void c2_sinEjemplares() {
        // Arrange
        when(libroRepo.findByIdParaActualizar(1L)).thenReturn(Optional.of(new Libro("111", "T", "A", 0)));
        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.registrarPrestamo(1L, 1L));
        // Assert
        assertEquals("El libro no tiene ejemplares disponibles", ex.getMessage());
    }

    @Test
    @DisplayName("C3 · 1→3→5→6→10 · Estudiante INACTIVO → rechazo")
    void c3_estudianteInactivo() {
        // Arrange
        when(libroRepo.findByIdParaActualizar(1L)).thenReturn(Optional.of(new Libro("111", "T", "A", 2)));
        when(estudianteRepo.findById(1L)).thenReturn(Optional.of(new Estudiante("1", "Ana", Estudiante.INACTIVO)));
        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.registrarPrestamo(1L, 1L));
        // Assert
        assertEquals("El estudiante no existe o no está ACTIVO", ex.getMessage());
    }

    @Test
    @DisplayName("C4 · 1→3→5→7→8→10 · Estudiante con 3 préstamos activos → rechazo")
    void c4_limiteDePrestamos() {
        // Arrange
        when(libroRepo.findByIdParaActualizar(1L)).thenReturn(Optional.of(new Libro("111", "T", "A", 2)));
        when(estudianteRepo.findById(1L)).thenReturn(Optional.of(new Estudiante("1", "Ana", Estudiante.ACTIVO)));
        when(prestamoRepo.countByEstudianteIdAndEstado(1L, Prestamo.ACTIVO)).thenReturn(3L);
        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.registrarPrestamo(1L, 1L));
        // Assert
        assertEquals("El estudiante ya tiene 3 préstamos activos", ex.getMessage());
    }

    @Test
    @DisplayName("C5 · 1→3→5→7→9→10 · Todo válido → préstamo ACTIVO y baja un ejemplar")
    void c5_prestamoExitoso() {
        // Arrange
        Libro libro = new Libro("111", "T", "A", 2);
        when(libroRepo.findByIdParaActualizar(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepo.findById(1L)).thenReturn(Optional.of(new Estudiante("1", "Ana", Estudiante.ACTIVO)));
        when(prestamoRepo.countByEstudianteIdAndEstado(1L, Prestamo.ACTIVO)).thenReturn(2L);
        when(prestamoRepo.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        Prestamo prestamo = service.registrarPrestamo(1L, 1L);
        // Assert
        assertEquals(Prestamo.ACTIVO, prestamo.getEstado());
        assertEquals(1, libro.getEjemplaresDisponibles());
    }
}
