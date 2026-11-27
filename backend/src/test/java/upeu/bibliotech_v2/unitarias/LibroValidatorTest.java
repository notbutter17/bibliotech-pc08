package upeu.bibliotech_v2.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.validation.LibroValidator;

import static org.junit.jupiter.api.Assertions.*;

/** RN-01 · Casos CP01–CP06 de la Sesión 1 automatizados con JUnit 5 y patrón AAA. */
@DisplayName("RN-01 · Validaciones de libro")
class LibroValidatorTest {

    private LibroValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LibroValidator();
    }

    @Test
    @DisplayName("CP01 · Libro con todos los datos válidos se acepta y queda DISPONIBLE")
    void cp01_libroValido() {
        // Arrange
        Libro libro = new Libro("978-0134685991", "Effective Java", "Joshua Bloch", 5);
        // Act + Assert
        assertDoesNotThrow(() -> validator.validar(libro));
        assertEquals(Libro.DISPONIBLE, libro.getEstado());
    }

    @ParameterizedTest(name = "CP02 · ISBN inválido [{0}] se rechaza")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void cp02_isbnObligatorio(String isbn) {
        // Arrange
        Libro libro = new Libro(isbn, "Effective Java", "Joshua Bloch", 5);
        // Act
        DatoInvalidoException ex = assertThrows(DatoInvalidoException.class, () -> validator.validar(libro));
        // Assert
        assertEquals("El ISBN es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("CP03 · Título vacío se rechaza")
    void cp03_tituloObligatorio() {
        // Arrange
        Libro libro = new Libro("978-0134685991", "", "Joshua Bloch", 5);
        // Act
        DatoInvalidoException ex = assertThrows(DatoInvalidoException.class, () -> validator.validar(libro));
        // Assert
        assertEquals("El título es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("CP04 · Autor nulo se rechaza")
    void cp04_autorObligatorio() {
        // Arrange
        Libro libro = new Libro("978-0134685991", "Effective Java", null, 5);
        // Act
        DatoInvalidoException ex = assertThrows(DatoInvalidoException.class, () -> validator.validar(libro));
        // Assert
        assertEquals("El autor es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("CP05 · Ejemplares negativos (-1, borde inferior inválido) se rechazan")
    void cp05_ejemplaresNegativos() {
        // Arrange
        Libro libro = new Libro("978-0134685991", "Effective Java", "Joshua Bloch", -1);
        // Act
        DatoInvalidoException ex = assertThrows(DatoInvalidoException.class, () -> validator.validar(libro));
        // Assert
        assertEquals("Los ejemplares no pueden ser negativos", ex.getMessage());
    }

    @Test
    @DisplayName("CP06 · Ejemplares = 0 (borde válido) se acepta y queda NO DISPONIBLE")
    void cp06_ejemplaresCeroBorde() {
        // Arrange
        Libro libro = new Libro("978-0134685991", "Effective Java", "Joshua Bloch", 0);
        // Act + Assert
        assertDoesNotThrow(() -> validator.validar(libro));
        assertEquals(Libro.NO_DISPONIBLE, libro.getEstado());
    }

    @Test
    @DisplayName("Borde · Ejemplares = 1 deja el libro DISPONIBLE")
    void bordeUnEjemplarDisponible() {
        // Arrange
        Libro libro = new Libro("978-0134685991", "Effective Java", "Joshua Bloch", 1);
        // Act
        String estado = libro.getEstado();
        // Assert
        assertEquals(Libro.DISPONIBLE, estado);
    }

    @Test
    @DisplayName("Libro nulo se rechaza")
    void libroNulo() {
        // Arrange + Act
        DatoInvalidoException ex = assertThrows(DatoInvalidoException.class, () -> validator.validar(null));
        // Assert
        assertEquals("Los datos del libro son obligatorios", ex.getMessage());
    }
}
