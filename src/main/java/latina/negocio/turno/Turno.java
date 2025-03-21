package latina.negocio.turno;

import jakarta.persistence.*;
import latina.negocio.rol.Rol;

import java.sql.Timestamp;

@Entity
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Timestamp fechaHoraInicio;

    private Timestamp fechaHoraFin;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
