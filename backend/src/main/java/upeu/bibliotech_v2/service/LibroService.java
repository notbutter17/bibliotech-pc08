package upeu.bibliotech_v2.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.bibliotech_v2.exception.RecursoNoEncontradoException;
import upeu.bibliotech_v2.exception.ReglaNegocioException;
import upeu.bibliotech_v2.model.Libro;
import upeu.bibliotech_v2.repository.LibroRepository;
import upeu.bibliotech_v2.validation.LibroValidator;

import java.util.List;

/** RN-01: registrar y consultar libros. */
@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final LibroValidator validator = new LibroValidator();

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro obtener(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El libro con id " + id + " no existe"));
    }

    @Transactional
    public Libro registrar(Libro libro) {
        validator.validar(libro);
        libro.setIsbn(libro.getIsbn().trim());
        if (libroRepository.existsByIsbn(libro.getIsbn())) {
            throw new ReglaNegocioException("Ya existe un libro con el ISBN " + libro.getIsbn());
        }
        libro.setId(null);
        return libroRepository.save(libro);
    }
}
