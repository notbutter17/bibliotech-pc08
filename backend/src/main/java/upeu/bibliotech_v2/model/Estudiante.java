package upeu.bibliotech_v2.model;

import jakarta.persistence.*;

/** Estudiante (RN-02). Estado: ACTIVO o INACTIVO. */
@Entity
@Table(name = "estudiantes")
public class Estudiante {

    public static final String ACTIVO = "ACTIVO";
    public static final String INACTIVO = "INACTIVO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String dni;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String estado;

    public Estudiante() {
    }

    public Estudiante(String dni, String nombre, String estado) {
        this.dni = dni;
        this.nombre = nombre;
        this.estado = estado;
    }

    public boolean esActivo() {
        return ACTIVO.equalsIgnoreCase(estado);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
