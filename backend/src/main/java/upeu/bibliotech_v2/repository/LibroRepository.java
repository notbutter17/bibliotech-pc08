package upeu.bibliotech_v2.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.bibliotech_v2.model.Libro;

import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    /**
     * SELECT ... FOR UPDATE: bloquea la fila del libro hasta el fin de la transacción.
     * Evita la actualización perdida de ejemplares cuando hay préstamos/devoluciones concurrentes
     * (hallazgo de la prueba de estrés con JMeter, Unidad 2).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Libro l where l.id = :id")
    Optional<Libro> findByIdParaActualizar(@Param("id") Long id);
}
