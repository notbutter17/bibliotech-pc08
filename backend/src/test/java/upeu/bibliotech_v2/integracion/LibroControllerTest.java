package upeu.bibliotech_v2.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import upeu.bibliotech_v2.controller.LibroController;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.exception.RecursoNoEncontradoException;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.service.LibroService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Capa web aislada: @WebMvcTest + MockMvc sobre LibroController, servicio simulado con @MockitoBean. */
@WebMvcTest(LibroController.class)
@DisplayName("@WebMvcTest · LibroController")
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibroService libroService;

    private static final String JSON_VALIDO = """
            {"isbn":"978-1","titulo":"Clean Code","autor":"Robert Martin","ejemplaresDisponibles":2}
            """;

    private Libro libro(Long id, int ejemplares) {
        Libro l = new Libro("978-" + id, "Libro " + id, "Autor", ejemplares);
        l.setId(id);
        return l;
    }

    @Test
    @DisplayName("GET /api/libros → 200 con la lista y el estado calculado")
    void listar_200() throws Exception {
        // Arrange
        when(libroService.listar()).thenReturn(List.of(libro(1L, 3), libro(2L, 0)));
        // Act + Assert
        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$[1].estado").value("NO DISPONIBLE"));
    }

    @Test
    @DisplayName("POST /api/libros válido → 201 con Location")
    void registrar_201() throws Exception {
        // Arrange
        when(libroService.registrar(any(Libro.class))).thenReturn(libro(10L, 2));
        // Act + Assert
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/libros/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("POST /api/libros sin ISBN → 400 con mensaje")
    void registrar_400() throws Exception {
        // Arrange
        when(libroService.registrar(any(Libro.class))).thenThrow(new DatoInvalidoException("El ISBN es obligatorio"));
        // Act + Assert
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"X\",\"autor\":\"Y\",\"ejemplaresDisponibles\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El ISBN es obligatorio"));
    }

    @Test
    @DisplayName("POST /api/libros con JSON mal formado → 400")
    void registrar_jsonInvalido_400() throws Exception {
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content("{isbn:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/libros con ISBN repetido → 409")
    void registrar_duplicado_409() throws Exception {
        // Arrange
        when(libroService.registrar(any(Libro.class))).thenThrow(new ReglaNegocioException("Ya existe un libro con el ISBN 978-1"));
        // Act + Assert
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/libros/{id} existente → 200; inexistente → 404")
    void obtener_200_y_404() throws Exception {
        // Arrange
        when(libroService.obtener(1L)).thenReturn(libro(1L, 1));
        when(libroService.obtener(99L)).thenThrow(new RecursoNoEncontradoException("El libro con id 99 no existe"));
        // Act + Assert
        mockMvc.perform(get("/api/libros/1")).andExpect(status().isOk()).andExpect(jsonPath("$.isbn").value("978-1"));
        mockMvc.perform(get("/api/libros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El libro con id 99 no existe"));
    }
}
