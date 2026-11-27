package upeu.bibliotech_v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.service.LibroService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public List<Libro> listar() {
        return libroService.listar();
    }

    @GetMapping("/{id}")
    public Libro obtener(@PathVariable Long id) {
        return libroService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Libro> registrar(@RequestBody Libro libro) {
        Libro creado = libroService.registrar(libro);
        return ResponseEntity.created(URI.create("/api/libros/" + creado.getId())).body(creado);
    }
}
