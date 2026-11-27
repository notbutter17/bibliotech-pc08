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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Método del camino básico · devolverLibro() (RN-04).
 * 2 decisiones (D1 ¿existe?, D2 ¿ACTIVO?) → V(G) = 2 + 1 = 3 caminos independientes.
 */
@DisplayName("Camino básico · devolverLibro()  V(G)=3")
class CaminoBasicoDevolverLibroTest {

    private PrestamoRepository prestamoRepo;
    private PrestamoService service;

    @BeforeEach
    void setUp() {
        prestamoRepo = mock(PrestamoRepository.class);
        service = new PrestamoService(mock(EstudianteRepository.class), mock(LibroRepository.class), prestamoRepo);
    }

    private Prestamo prestamo(String estado, Libro libro) {
        return new Prestamo(new Estudiante("1", "Ana", Estudiante.ACTIVO), libro, LocalDate.now(), estado);
    }

    @Test
    @DisplayName("C1 · 1→2→6 · El préstamo no existe → 404")
    void c1_prestamoNoExiste() {
        // Arrange
        when(prestamoRepo.findById(50L)).thenReturn(Optional.empty());
        // Act
        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.devolverLibro(50L));
        // Assert
        assertEquals("El préstamo no existe", ex.getMessage());
    }

    @Test
    @DisplayName("C2 · 1→3→4→6 · Préstamo ya DEVUELTO → rechazo")
    void c2_prestamoYaDevuelto() {
        // Arrange
        Libro libro = new Libro("111", "T", "A", 1);
        when(prestamoRepo.findById(1L)).thenReturn(Optional.of(prestamo(Prestamo.DEVUELTO, libro)));
        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> service.devolverLibro(1L));
        // Assert
        assertEquals("El préstamo ya fue devuelto", ex.getMessage());
        assertEquals(1, libro.getEjemplaresDisponibles(), "no debe sumar ejemplares");
    }

    @Test
    @DisplayName("C3 · 1→3→5→6 · Préstamo ACTIVO → DEVUELTO y sube un ejemplar")
    void c3_devolucionExitosa() {
        // Arrange
        Libro libro = new Libro("111", "T", "A", 0);
        when(prestamoRepo.findById(1L)).thenReturn(Optional.of(prestamo(Prestamo.ACTIVO, libro)));
        when(prestamoRepo.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        Prestamo devuelto = service.devolverLibro(1L);
        // Assert
        assertEquals(Prestamo.DEVUELTO, devuelto.getEstado());
        assertEquals(1, libro.getEjemplaresDisponibles());
        assertEquals(Libro.DISPONIBLE, libro.getEstado());
    }
}
