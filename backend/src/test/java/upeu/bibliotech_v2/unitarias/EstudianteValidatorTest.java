package upeu.bibliotech_v2.unitarias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.validation.EstudianteValidator;

import static org.junit.jupiter.api.Assertions.*;

/** RN-02 · Validaciones de estudiante con JUnit 5 y patrón AAA. */
@DisplayName("RN-02 · Validaciones de estudiante")
class EstudianteValidatorTest {

    private EstudianteValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EstudianteValidator();
    }

    @Test
    @DisplayName("Estudiante con DNI y nombre válidos se acepta")
    void estudianteValido() {
        // Arrange
        Estudiante estudiante = new Estudiante("70123456", "Ana Torres", null);
        // Act + Assert
        assertDoesNotThrow(() -> validator.validar(estudiante));
    }

    @ParameterizedTest(name = "DNI inválido [{0}] se rechaza")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void dniObligatorio(String dni) {
        // Arrange
        Estudiante estudiante = new Estudiante(dni, "Ana Torres", null);
        // Act
        DatoInvalidoException ex = assertThrows(DatoInvalidoException.class, () -> validator.validar(estudiante));
        // Assert
        assertEquals("El DNI es obligatorio", ex.getMessage());
    }

    @ParameterizedTest(name = "Nombre inválido [{0}] se rechaza")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void nombreObligatorio(String nombre) {
        // Arrange
        Estudiante estudiante = new Estudiante("70123456", nombre, null);
        // Act
        DatoInvalidoException ex = assertThrows(DatoInvalidoException.class, () -> validator.validar(estudiante));
        // Assert
        assertEquals("El nombre es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("Estudiante nulo se rechaza")
    void estudianteNulo() {
        // Arrange + Act
        DatoInvalidoException ex = assertThrows(DatoInvalidoException.class, () -> validator.validar(null));
        // Assert
        assertEquals("Los datos del estudiante son obligatorios", ex.getMessage());
    }

    @Test
    @DisplayName("esActivo distingue ACTIVO de INACTIVO")
    void esActivo() {
        // Arrange
        Estudiante activo = new Estudiante("1", "A", Estudiante.ACTIVO);
        Estudiante inactivo = new Estudiante("2", "B", Estudiante.INACTIVO);
        // Act + Assert
        assertTrue(activo.esActivo());
        assertFalse(inactivo.esActivo());
    }
}
