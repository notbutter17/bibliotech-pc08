package upeu.bibliotech_v2.mockito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.service.EstudianteService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito · EstudianteService (RN-02)")
class EstudianteServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @InjectMocks
    private EstudianteService estudianteService;

    @Test
    @DisplayName("Registra con estado inicial ACTIVO aunque se envíe otro estado")
    void registrar_estadoInicialActivo() {
        // Arrange
        Estudiante estudiante = new Estudiante("70123456", "Ana Torres", Estudiante.INACTIVO);
        when(estudianteRepository.existsByDni("70123456")).thenReturn(false);
        when(estudianteRepository.save(estudiante)).thenReturn(estudiante);
        // Act
        Estudiante guardado = estudianteService.registrar(estudiante);
        // Assert
        verify(estudianteRepository).save(estudiante);
        assertEquals(Estudiante.ACTIVO, guardado.getEstado());
    }

    @Test
    @DisplayName("DNI duplicado se rechaza con never(save)")
    void registrar_dniDuplicado() {
        // Arrange
        Estudiante estudiante = new Estudiante("70123456", "Ana Torres", null);
        when(estudianteRepository.existsByDni("70123456")).thenReturn(true);
        // Act
        assertThrows(ReglaNegocioException.class, () -> estudianteService.registrar(estudiante));
        // Assert
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("listar delega en el repositorio")
    void listar() {
        // Arrange
        when(estudianteRepository.findAll()).thenReturn(List.of(new Estudiante()));
        // Act + Assert
        assertEquals(1, estudianteService.listar().size());
    }
}
