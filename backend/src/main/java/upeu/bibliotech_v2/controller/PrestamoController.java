package upeu.bibliotech_v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.bibliotech_v2.dto.PrestamoRequest;
import upeu.bibliotech_v2.exception.DatoInvalidoException;
import upeu.bibliotech_v2.model.Prestamo;
import upeu.bibliotech_v2.service.PrestamoService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<Prestamo> listar() {
        return prestamoService.listar();
    }

    @PostMapping
    public ResponseEntity<Prestamo> registrar(@RequestBody PrestamoRequest request) {
        if (request.estudianteId() == null || request.libroId() == null) {
            throw new DatoInvalidoException("estudianteId y libroId son obligatorios");
        }
        Prestamo prestamo = prestamoService.registrarPrestamo(request.estudianteId(), request.libroId());
        return ResponseEntity.created(URI.create("/api/prestamos/" + prestamo.getId())).body(prestamo);
    }

    @PutMapping("/{id}/devolucion")
    public Prestamo devolver(@PathVariable Long id) {
        return prestamoService.devolverLibro(id);
    }
}
