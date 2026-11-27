package upeu.bibliotech_v2.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import upeu.bibliotech_v2.controller.EstudianteController;
import upeu.bibliotech_v2.controller.PrestamoController;
import upeu.bibliotech_v2.exception.RecursoNoEncontradoException;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Estudiante;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.service.EstudianteService;
import upeu.bibliotech_v2.service.PrestamoService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** @WebMvcTest de los controladores de préstamos y estudiantes. */
@WebMvcTest({PrestamoController.class, EstudianteController.class})
@DisplayName("@WebMvcTest · PrestamoController y EstudianteController")
class PrestamoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrestamoService prestamoService;
    @MockitoBean
    private EstudianteService estudianteService;

    private Prestamo prestamo(String estado) {
        Prestamo p = new Prestamo(new Estudiante("1", "Ana", "ACTIVO"), new Libro("978-1", "T", "A", 1), LocalDate.now(), estado);
        p.setId(7L);
        return p;
    }

    @Test
    @DisplayName("POST /api/prestamos válido → 201")
    void registrar_201() throws Exception {
        when(prestamoService.registrarPrestamo(1L, 1L)).thenReturn(prestamo("ACTIVO"));
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estudianteId\":1,\"libroId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /api/prestamos sin libroId → 400 y no llama al servicio")
    void registrar_400() throws Exception {
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content("{\"estudianteId\":1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content("{\"libroId\":1}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(prestamoService);
    }

    @Test
    @DisplayName("POST /api/prestamos sin ejemplares → 409 con mensaje de RN-03")
    void registrar_409() throws Exception {
        when(prestamoService.registrarPrestamo(1L, 3L))
                .thenThrow(new ReglaNegocioException("El libro no tiene ejemplares disponibles"));
        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estudianteId\":1,\"libroId\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El libro no tiene ejemplares disponibles"));
    }

    @Test
    @DisplayName("PUT /api/prestamos/{id}/devolucion → 200 DEVUELTO; inexistente → 404")
    void devolver_200_y_404() throws Exception {
        when(prestamoService.devolverLibro(7L)).thenReturn(prestamo("DEVUELTO"));
        when(prestamoService.devolverLibro(999L)).thenThrow(new RecursoNoEncontradoException("El préstamo no existe"));
        mockMvc.perform(put("/api/prestamos/7/devolucion"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("DEVUELTO"));
        mockMvc.perform(put("/api/prestamos/999/devolucion"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/prestamos → 200")
    void listar_200() throws Exception {
        when(prestamoService.listar()).thenReturn(List.of(prestamo("ACTIVO")));
        mockMvc.perform(get("/api/prestamos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(7));
    }

    @Test
    @DisplayName("POST /api/estudiantes → 201 ACTIVO; GET → 200")
    void estudiantes() throws Exception {
        Estudiante e = new Estudiante("70123456", "Ana", "ACTIVO");
        e.setId(4L);
        when(estudianteService.registrar(any(Estudiante.class))).thenReturn(e);
        when(estudianteService.listar()).thenReturn(List.of(e));
        mockMvc.perform(post("/api/estudiantes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"70123456\",\"nombre\":\"Ana\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/estudiantes/4"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
        mockMvc.perform(get("/api/estudiantes")).andExpect(status().isOk());
    }
}
