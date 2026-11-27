package upeu.bibliotech_v2.integracion;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.repository.EstudianteRepository;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.repository.PrestamoRepository;
import upeu.bibliotech_v2.service.EstudianteService;
import upeu.bibliotech_v2.service.LibroService;
import upeu.bibliotech_v2.service.PrestamoService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Flujo completo con contexto Spring real y base H2 en memoria (datos controlados:
 * cada prueba limpia las tablas y crea sus propios registros).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("@SpringBootTest · Flujo préstamo / devolución")
class FlujoPrestamoIntegrationTest {

    @Autowired private LibroService libroService;
    @Autowired private EstudianteService estudianteService;
    @Autowired private PrestamoService prestamoService;
    @Autowired private LibroRepository libroRepository;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private PrestamoRepository prestamoRepository;
    @Autowired private MockMvc mockMvc;

    private Estudiante ana;

    @BeforeEach
    void limpiarYSembrar() {
        prestamoRepository.deleteAll();
        libroRepository.deleteAll();
        estudianteRepository.deleteAll();
        ana = estudianteService.registrar(new Estudiante("70123456", "Ana Torres", null));
    }

    @Test
    @DisplayName("Registrar préstamo baja un ejemplar y lo deja ACTIVO")
    void registrarPrestamo_bajaEjemplar() {
        // Arrange
        Libro libro = libroService.registrar(new Libro("978-A", "Refactoring", "Martin Fowler", 2));
        // Act
        Prestamo prestamo = prestamoService.registrarPrestamo(ana.getId(), libro.getId());
        // Assert
        assertNotNull(prestamo.getId());
        assertEquals(Prestamo.ACTIVO, prestamo.getEstado());
        assertEquals(1, libroRepository.findById(libro.getId()).orElseThrow().getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Sin ejemplares se rechaza y no se crea préstamo")
    void sinEjemplares_seRechaza() {
        // Arrange
        Libro libro = libroService.registrar(new Libro("978-B", "DDD", "Eric Evans", 1));
        prestamoService.registrarPrestamo(ana.getId(), libro.getId()); // agota el único ejemplar
        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> prestamoService.registrarPrestamo(ana.getId(), libro.getId()));
        // Assert
        assertEquals("El libro no tiene ejemplares disponibles", ex.getMessage());
        assertEquals(1, prestamoRepository.count());
        assertEquals(Libro.NO_DISPONIBLE, libroRepository.findById(libro.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Devolver restituye el ejemplar y el préstamo pasa a DEVUELTO")
    void devolver_restituyeEjemplar() {
        // Arrange
        Libro libro = libroService.registrar(new Libro("978-C", "TDD", "Kent Beck", 1));
        Prestamo prestamo = prestamoService.registrarPrestamo(ana.getId(), libro.getId());
        // Act
        prestamoService.devolverLibro(prestamo.getId());
        // Assert
        assertEquals(1, libroRepository.findById(libro.getId()).orElseThrow().getEjemplaresDisponibles());
        assertEquals(Prestamo.DEVUELTO, prestamoRepository.findById(prestamo.getId()).orElseThrow().getEstado());
        assertThrows(ReglaNegocioException.class, () -> prestamoService.devolverLibro(prestamo.getId()));
    }

    @Test
    @DisplayName("El cuarto préstamo activo se rechaza (máximo 3)")
    void cuartoPrestamo_seRechaza() {
        // Arrange
        Libro libro = libroService.registrar(new Libro("978-D", "SICP", "Abelson", 10));
        for (int i = 0; i < 3; i++) {
            prestamoService.registrarPrestamo(ana.getId(), libro.getId());
        }
        // Act + Assert
        assertThrows(ReglaNegocioException.class, () -> prestamoService.registrarPrestamo(ana.getId(), libro.getId()));
        assertEquals(7, libroRepository.findById(libro.getId()).orElseThrow().getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Unicidad real en BD: ISBN y DNI duplicados se rechazan")
    void unicidad() {
        libroService.registrar(new Libro("978-E", "X", "Y", 1));
        assertThrows(ReglaNegocioException.class, () -> libroService.registrar(new Libro("978-E", "Otro", "Z", 1)));
        assertThrows(ReglaNegocioException.class,
                () -> estudianteService.registrar(new Estudiante("70123456", "Otra", null)));
    }

    @Test
    @DisplayName("HTTP extremo a extremo: POST libro → POST préstamo → PUT devolución")
    void flujoHttpCompleto() throws Exception {
        String libroJson = mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"978-F\",\"titulo\":\"Patterns\",\"autor\":\"GoF\",\"ejemplaresDisponibles\":1}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long libroId = ((Number) JsonPath.read(libroJson, "$.id")).longValue();

        String prestamoJson = mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estudianteId\":" + ana.getId() + ",\"libroId\":" + libroId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.libro.estado").value("NO DISPONIBLE"))
                .andReturn().getResponse().getContentAsString();
        long prestamoId = ((Number) JsonPath.read(prestamoJson, "$.id")).longValue();

        mockMvc.perform(put("/api/prestamos/" + prestamoId + "/devolucion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"));
        mockMvc.perform(get("/api/libros/" + libroId))
                .andExpect(jsonPath("$.ejemplaresDisponibles").value(1));
    }

    @Test
    @DisplayName("Concurrencia: 20 préstamos simultáneos descuentan exactamente 20 ejemplares (sin actualización perdida)")
    void prestamosConcurrentes_noPierdenActualizaciones() throws Exception {
        // Arrange
        Libro libro = libroService.registrar(new Libro("978-CONC", "Concurrencia", "JMeter", 50));
        java.util.List<Long> estudiantes = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            estudiantes.add(estudianteService.registrar(new Estudiante("8000" + i, "Concurrente " + i, null)).getId());
        }
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(20);
        java.util.concurrent.CountDownLatch salida = new java.util.concurrent.CountDownLatch(1);
        java.util.List<java.util.concurrent.Future<Prestamo>> futuros = new java.util.ArrayList<>();
        // Act
        for (Long id : estudiantes) {
            futuros.add(pool.submit(() -> { salida.await(); return prestamoService.registrarPrestamo(id, libro.getId()); }));
        }
        salida.countDown();
        for (var f : futuros) { f.get(); }
        pool.shutdown();
        // Assert
        assertEquals(30, libroRepository.findById(libro.getId()).orElseThrow().getEjemplaresDisponibles());
        assertEquals(20, prestamoRepository.count());
    }
}
