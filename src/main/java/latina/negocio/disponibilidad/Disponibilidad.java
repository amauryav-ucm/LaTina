package latina.negocio.disponibilidad;

import java.sql.Timestamp;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jdk.jfr.Name;
import latina.negocio.empleado.Empleado;

@Entity
@NamedQueries({
        @NamedQuery(name = "Disponibilidad.findByEmpleado",
                query = "SELECT d FROM Disponibilidad d WHERE d.empleado = :empleado"),
        @NamedQuery(name = "Disponibilidad.findByFechaInicio",
                query = "SELECT d FROM Disponibilidad d WHERE d.fechaHoraInicio = :fechaInicio"),
        @NamedQuery(name = "Disponibilidad.findByFechaFin",
                query = "SELECT d FROM Disponibilidad d WHERE d.fechaHoraFin = :fechaFin"),
        @NamedQuery(name = "Disponibilidad.findByEmpleadoAndFechaInicio",
                query = "SELECT d FROM Disponibilidad d WHERE d.empleado = :empleado AND d.fechaHoraInicio = :fechaInicio"),
        @NamedQuery(name = "Disponibilidad.delete", query = "DELETE FROM Disponibilidad d WHERE d.id = :id")
        /*
        @NamedQuery(name = "Disponibilidad.findByRangoFecha",
                query = "SELECT d FROM Disponibilidad d WHERE d.fechaInicio <= :fechaHoraIni AND d.fechaFin >= :fechaHoraFin")*/
})
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Empleado empleado;

    @Column(nullable = false)
    private Timestamp fechaHoraInicio;

    @Column(nullable = false)
    private Timestamp fechaHoraFin;


    public int getId() {return id;   }

    public void setId(int id) {this.id = id;    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Timestamp getFechaHoraInicio() {return fechaHoraInicio;    }

    public void setFechaHoraInicio(Timestamp fechaInicio) {this.fechaHoraInicio = fechaInicio;    }

    public Timestamp getFechaHoraFin() {return fechaHoraFin;    }

    public void setFechaHoraFin(Timestamp fechaFin) {this.fechaHoraFin = fechaFin;    }

    public Disponibilidad() { }

    public Disponibilidad(Empleado empleado, TDisponibilidad tDisponibilidad) {
        this.empleado = empleado;
        this.fechaHoraInicio = tDisponibilidad.getFechaInicio();
        this.fechaHoraFin = tDisponibilidad.getFechaFin();

    }

    public Disponibilidad(Empleado empleado, Timestamp fechaHoraInicio, Timestamp fechaHoraFin) {
        this.empleado = empleado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;

    }

    /**
     * @param inicio el inicio de la ventana a comparar
     * @param fin el final de la ventana a comparar, debe ser posterior a inicio
     * @returns true si la disponibilidad solapa con la ventana definida por inicio y fin
     */
    public boolean solapaCon(Timestamp inicio, Timestamp fin){
        // Se presupone que las horas estan bien cronologicamente
        return !(fechaHoraInicio.after(fin) || fechaHoraFin.before(inicio) || fechaHoraInicio.equals(fin) || fechaHoraFin.equals(inicio));
    }

    public boolean seDebeUnirCon(Disponibilidad otraDisponibilidad){
        // Si es la misma no se deben unir
        if(id==otraDisponibilidad.getId()) {
            return false;
        }
        // Si empieza despues que la otra acaba no se deben unir
        if(fechaHoraInicio.after(otraDisponibilidad.getFechaHoraFin())){
            return false;
        }
        // Si acaba antes que la otra comience no se deben unir
        if(fechaHoraFin.before(otraDisponibilidad.getFechaHoraInicio())){
            return false;
        }
        return true;
    }

    /**
     * @param inicio el inicio de la ventana a comparar
     * @param fin el final de la ventana a comparar, debe ser posterior a inicio
     * @returns true si la disponibilidad contiene la ventana definida por inicio y fin
     */
    public boolean contiene(Timestamp inicio, Timestamp fin){
        // Se presupone que las horas estan bien cronologicamente
        return !fechaHoraInicio.after(inicio) && !fechaHoraFin.before(fin);
    }
}
