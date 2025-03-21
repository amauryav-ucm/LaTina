package latina.negocio.dispoinibilidad;

import java.sql.Timestamp;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import latina.negocio.empleado.Empleado;

@Entity
@NamedQueries({
        @NamedQuery(name = "Disponibilidad.findByEmpleado",
                query = "SELECT d FROM Disponibilidad d WHERE d.empleado = :empleado"),
        @NamedQuery(name = "Disponibilidad.findByFechaInicio",
                query = "SELECT d FROM Disponibilidad d WHERE d.fechaInicio = :fechaInicio"),
        @NamedQuery(name = "Disponibilidad.findByFechaFin",
                query = "SELECT d FROM Disponibilidad d WHERE d.fechaFin = :fechaFin"),
        @NamedQuery(name = "Disponibilidad.findByEmpleadoAndFechaInicio",
                query = "SELECT d FROM Disponibilidad d WHERE d.empleado = :empleado AND d.fechaInicio = :fechaInicio")
})
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Empleado empleado;

    @Column(nullable = false)
    private Timestamp fechaInicio;

    @Column(nullable = false)
    private Timestamp fechaFin;


    public int getId() {return id;   }

    public void setId(int id) {this.id = id;    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Timestamp getFechaInicio() {return fechaInicio;    }

    public void setFechaInicio(Timestamp fechaInicio) {this.fechaInicio = fechaInicio;    }

    public Timestamp getFechaFin() {return fechaFin;    }

    public void setFechaFin(Timestamp fechaFin) {this.fechaFin = fechaFin;    }

    public Disponibilidad() { }

    public Disponibilidad(Empleado empleado, Timestamp fechaInicio, Timestamp fechaFin) {
        this.empleado = empleado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;

    }
}
