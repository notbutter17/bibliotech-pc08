package upeu.bibliotech_v2.mockito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.exception.RecursoNoEncontradoException;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.service.LibroService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito · LibroService (RN-01)")
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroService libroService;

    @Test
    @DisplayName("Registra un libro válido (ISBN recortado) y verify(save)")
    void registrar_valido() {
        // Arrange
        Libro libro = new Libro("  978-1  ", "Clean Code", "Robert Martin", 2);
        when(libroRepository.existsByIsbn("978-1")).thenReturn(false);
        when(libroRepository.save(libro)).thenReturn(libro);
        // Act
        Libro guardado = libroService.registrar(libro);
        // Assert
        verify(libroRepository).save(libro);
        assertEquals("978-1", guardado.getIsbn());
    }

    @Test
    @DisplayName("CP · ISBN duplicado se rechaza con never(save)")
    void registrar_isbnDuplicado() {
        // Arrange
        Libro libro = new Libro("978-1", "Clean Code", "Robert Martin", 2);
        when(libroRepository.existsByIsbn("978-1")).thenReturn(true);
        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> libroService.registrar(libro));
        // Assert
        assertTrue(ex.getMessage().contains("978-1"));
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Datos inválidos: no consulta unicidad ni guarda")
    void registrar_invalido() {
        // Arrange
        Libro libro = new Libro("978-1", "Clean Code", "Robert Martin", -3);
        // Act
        assertThrows(DatoInvalidoException.class, () -> libroService.registrar(libro));
        // Assert
        verify(libroRepository, never()).existsByIsbn(anyString());
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtener lanza 404 si el libro no existe")
    void obtener_noExiste() {
        // Arrange
        when(libroRepository.findById(8L)).thenReturn(Optional.empty());
        // Act + Assert
        assertThrows(RecursoNoEncontradoException.class, () -> libroService.obtener(8L));
    }

    @Test
    @DisplayName("obtener y listar devuelven lo del repositorio")
    void obtenerYListar() {
        // Arrange
        Libro libro = new Libro("978-1", "Clean Code", "Robert Martin", 2);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(libroRepository.findAll()).thenReturn(List.of(libro));
        // Act + Assert
        assertSame(libro, libroService.obtener(1L));
        assertEquals(1, libroService.listar().size());
    }
}
