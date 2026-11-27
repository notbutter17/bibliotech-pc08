package upeu.bibliotech_v2.mockito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** PrestamoService con repositorios simulados (@Mock + @InjectMocks), al estilo MatriculaServiceTest. */
@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito · PrestamoService")
class PrestamoServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private LibroRepository libroRepository;
    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private PrestamoService prestamoService;

    private final Estudiante activo = new Estudiante("70123456", "Ana Torres", Estudiante.ACTIVO);

    @Test
    @DisplayName("Éxito · registra el préstamo: verify(save) del libro y del préstamo")
    void registrarPrestamo_exitoso() {
        // Arrange
        Libro libro = new Libro("978-1", "Clean Code", "Robert Martin", 3);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(activo));
        when(prestamoRepository.countByEstudianteIdAndEstado(1L, Prestamo.ACTIVO)).thenReturn(0L);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Prestamo resultado = prestamoService.registrarPrestamo(1L, 1L);

        // Assert
        ArgumentCaptor<Prestamo> captor = ArgumentCaptor.forClass(Prestamo.class);
        verify(prestamoRepository, times(1)).save(captor.capture());
        verify(libroRepository).save(libro);
        assertEquals(Prestamo.ACTIVO, captor.getValue().getEstado());
        assertSame(activo, captor.getValue().getEstudiante());
        assertEquals(LocalDate.now(), resultado.getFechaPrestamo());
        assertEquals(2, libro.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Rechazo · libro inexistente: never() consulta estudiante ni guarda")
    void registrarPrestamo_libroNoExiste() {
        // Arrange
        when(libroRepository.findById(9L)).thenReturn(Optional.empty());
        // Act
        assertThrows(RecursoNoEncontradoException.class, () -> prestamoService.registrarPrestamo(1L, 9L));
        // Assert
        verify(estudianteRepository, never()).findById(anyLong());
        verify(prestamoRepository, never()).save(any());
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechazo · sin ejemplares: never(save)")
    void registrarPrestamo_sinEjemplares() {
        // Arrange
        when(libroRepository.findById(1L)).thenReturn(Optional.of(new Libro("978-1", "T", "A", 0)));
        // Act
        assertThrows(ReglaNegocioException.class, () -> prestamoService.registrarPrestamo(1L, 1L));
        // Assert
        verify(prestamoRepository, never()).save(any());
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechazo · estudiante inexistente: never(countBy...) ni save")
    void registrarPrestamo_estudianteNoExiste() {
        // Arrange
        when(libroRepository.findById(1L)).thenReturn(Optional.of(new Libro("978-1", "T", "A", 2)));
        when(estudianteRepository.findById(7L)).thenReturn(Optional.empty());
        // Act
        assertThrows(ReglaNegocioException.class, () -> prestamoService.registrarPrestamo(7L, 1L));
        // Assert
        verify(prestamoRepository, never()).countByEstudianteIdAndEstado(anyLong(), any());
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechazo · 3 préstamos activos: never(save) y el stock no cambia")
    void registrarPrestamo_limiteAlcanzado() {
        // Arrange
        Libro libro = new Libro("978-1", "T", "A", 2);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(activo));
        when(prestamoRepository.countByEstudianteIdAndEstado(1L, Prestamo.ACTIVO)).thenReturn(3L);
        // Act
        assertThrows(ReglaNegocioException.class, () -> prestamoService.registrarPrestamo(1L, 1L));
        // Assert
        verify(prestamoRepository, never()).save(any());
        assertEquals(2, libro.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Éxito · devolver libro: verify(save) del préstamo DEVUELTO")
    void devolverLibro_exitoso() {
        // Arrange
        Libro libro = new Libro("978-1", "T", "A", 0);
        Prestamo prestamo = new Prestamo(activo, libro, LocalDate.now(), Prestamo.ACTIVO);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(prestamo)).thenReturn(prestamo);
        // Act
        Prestamo resultado = prestamoService.devolverLibro(5L);
        // Assert
        verify(libroRepository).save(libro);
        verify(prestamoRepository).save(prestamo);
        assertEquals(Prestamo.DEVUELTO, resultado.getEstado());
        assertEquals(1, libro.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Rechazo · devolver préstamo inexistente: never(save)")
    void devolverLibro_noExiste() {
        // Arrange
        when(prestamoRepository.findById(5L)).thenReturn(Optional.empty());
        // Act
        assertThrows(RecursoNoEncontradoException.class, () -> prestamoService.devolverLibro(5L));
        // Assert
        verify(prestamoRepository, never()).save(any());
        verifyNoInteractions(libroRepository);
    }

    @Test
    @DisplayName("listar delega en el repositorio")
    void listar() {
        // Arrange
        when(prestamoRepository.findAll()).thenReturn(List.of(new Prestamo()));
        // Act
        List<Prestamo> lista = prestamoService.listar();
        // Assert
        assertEquals(1, lista.size());
        verify(prestamoRepository).findAll();
    }
}
