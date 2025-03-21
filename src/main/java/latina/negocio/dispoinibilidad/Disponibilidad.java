package latina.negocio.dispoinibilidad;

import jakarta.persistence.*;
import jakarta.persistence.Id;
//import latina.negocio.empleado.Empleado;

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

    @Column(nullable = false)
    private String fechaInicio; // Formato DD/MM/YY

    @Column(nullable = false)
    private String fechaFin; // Formato DD/MM/YY

    @Column(nullable = false)
    private String horaInicio; // Formato HH:MM

    @Column(nullable = false)
    private String horaFin; // Formato HH:MM

    public int getId() {return id;   }

    public void setId(int id) {this.id = id;    }

    /*public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }*/

    public String getFechaInicio() {return fechaInicio;    }

    public void setFechaInicio(String fechaInicio) {this.fechaInicio = fechaInicio;    }

    public String getFechaFin() {return fechaFin;    }

    public void setFechaFin(String fechaFin) {this.fechaFin = fechaFin;    }

    public String getHoraInicio() {return horaInicio;    }

    public void setHoraInicio(String horaInicio) {this.horaInicio = horaInicio;    }

    public String getHoraFin() {return horaFin;    }

    public void setHoraFin(String horaFin) {this.horaFin = horaFin;    }

    public Disponibilidad() { }

    public Disponibilidad(/*Empleado empleado,*/ String fechaInicio, String fechaFin, String horaInicio, String horaFin) {
        //this.empleado = empleado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }
}
