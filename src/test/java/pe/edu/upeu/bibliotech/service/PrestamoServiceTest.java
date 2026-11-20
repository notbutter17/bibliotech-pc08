package pe.edu.upeu.bibliotech.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.bibliotech.model.Estudiante;
import pe.edu.upeu.bibliotech.repository.EstudianteRepository;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    private PrestamoService prestamoService;

    @BeforeEach
    void configurar() {
        prestamoService = new PrestamoService(estudianteRepository);
    }

    @Test
    @DisplayName("rechaza un estudiante inexistente")
    void rechazaEstudianteInexistente() {
        when(estudianteRepository.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(EstudianteNoEncontradoException.class,
                () -> prestamoService.puedeSolicitarPrestamo(99, 0, 1));
    }

    @Test
    @DisplayName("rechaza un estudiante inactivo")
    void rechazaEstudianteInactivo() {
        when(estudianteRepository.buscarPorId(2))
                .thenReturn(Optional.of(new Estudiante(2, "Luis", false, false)));

        assertFalse(prestamoService.puedeSolicitarPrestamo(2, 0, 1));
    }

    @Test
    @DisplayName("rechaza cuando alcanza el límite de préstamos")
    void rechazaLimiteDePrestamos() {
        when(estudianteRepository.buscarPorId(1))
                .thenReturn(Optional.of(new Estudiante(1, "Ana", true, false)));

        assertFalse(prestamoService.puedeSolicitarPrestamo(1, 3, 1));
    }

    @Test
    @DisplayName("rechaza cuando no hay ejemplares disponibles")
    void rechazaSinEjemplaresDisponibles() {
        when(estudianteRepository.buscarPorId(1))
                .thenReturn(Optional.of(new Estudiante(1, "Ana", true, false)));

        assertFalse(prestamoService.puedeSolicitarPrestamo(1, 0, 0));
    }

    @Test
    @DisplayName("autoriza cuando se cumplen las reglas existentes")
    void autorizaPrestamoValido() {
        when(estudianteRepository.buscarPorId(1))
                .thenReturn(Optional.of(new Estudiante(1, "Ana", true, false)));

        assertTrue(prestamoService.puedeSolicitarPrestamo(1, 2, 1));
    }

    @Test
    @DisplayName("autoriza a un estudiante habilitado y no suspendido")
    void autorizaEstudianteNoSuspendido() {
        Estudiante habilitado = new Estudiante(4, "Carla", true, false);
        when(estudianteRepository.buscarPorId(4)).thenReturn(Optional.of(habilitado));

        boolean autorizado = prestamoService.puedeSolicitarPrestamo(4, 0, 2);

        assertTrue(autorizado);
        verify(estudianteRepository).buscarPorId(4);
    }

    @Test
    @DisplayName("rechaza a un estudiante suspendido antes de evaluar otras reglas")
    void rechazaEstudianteSuspendido() {
        Estudiante suspendido = new Estudiante(3, "Rosa", true, true);
        when(estudianteRepository.buscarPorId(3)).thenReturn(Optional.of(suspendido));

        EstudianteSuspendidoException exception = assertThrows(
                EstudianteSuspendidoException.class,
                () -> prestamoService.puedeSolicitarPrestamo(3, 0, 5));

        assertEquals("El estudiante con id 3 está suspendido", exception.getMessage());
    }
}
