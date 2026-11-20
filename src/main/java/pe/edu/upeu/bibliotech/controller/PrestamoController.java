package pe.edu.upeu.bibliotech.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bibliotech.service.EstudianteNoEncontradoException;
import pe.edu.upeu.bibliotech.service.PrestamoService;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping("/validar")
    public ResponseEntity<Map<String, Object>> validar(
            @RequestParam int estudianteId,
            @RequestParam int prestamosActivos,
            @RequestParam int ejemplaresDisponibles) {
        try {
            boolean permitido = prestamoService.puedeSolicitarPrestamo(
                    estudianteId, prestamosActivos, ejemplaresDisponibles);
            return ResponseEntity.ok(Map.of(
                    "permitido", permitido,
                    "mensaje", permitido ? "Préstamo autorizado" : "Préstamo no autorizado"));
        } catch (EstudianteNoEncontradoException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "permitido", false,
                    "mensaje", exception.getMessage()));
        }
    }
}
