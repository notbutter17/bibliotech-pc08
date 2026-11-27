package upeu.bibliotech_v2.model;

import jakarta.persistence.*;

/**
 * Libro del catálogo (RN-01).
 * El estado NO se guarda: se calcula a partir de ejemplaresDisponibles (CA-06 / CA-07).
 */
@Entity
@Table(name = "libros")
public class Libro {

    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String NO_DISPONIBLE = "NO DISPONIBLE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(name = "ejemplares_disponibles", nullable = false)
    private int ejemplaresDisponibles;

    public Libro() {
    }

    public Libro(String isbn, String titulo, String autor, int ejemplaresDisponibles) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    /** Estado calculado: mayor que 0 = DISPONIBLE; igual a 0 = NO DISPONIBLE. */
    @Transient
    public String getEstado() {
        return ejemplaresDisponibles > 0 ? DISPONIBLE : NO_DISPONIBLE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public void setEjemplaresDisponibles(int ejemplaresDisponibles) { this.ejemplaresDisponibles = ejemplaresDisponibles; }
}
