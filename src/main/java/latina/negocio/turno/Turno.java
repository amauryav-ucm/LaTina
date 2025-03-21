package latina.negocio.turno;

import jakarta.persistence.*;
import latina.negocio.empleado.Empleado;
import latina.negocio.rol.Rol;

import java.sql.Timestamp;

@Entity
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Timestamp fechaHoraInicio;

    private Timestamp fechaHoraFin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    // Puede tener un empleado null, que significa que el tueno aun no esta asignado
    @ManyToOne(optional = true)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Turno(){}

    public Turno(TTurno turno, Rol rol) {
        this.id = turno.getIdTurno();
        this.fechaHoraInicio = turno.getFechaHoraInicio();
        this.fechaHoraFin = turno.getFechaHoraFin();
        this.rol = rol;
    }

    public Turno(TTurno turno, Rol rol, Empleado empleado) {
        this.id = turno.getIdTurno();
        this.fechaHoraInicio = turno.getFechaHoraInicio();
        this.fechaHoraFin = turno.getFechaHoraFin();
        this.rol = rol;
        this.empleado = empleado;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Timestamp getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(Timestamp fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public Timestamp getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(Timestamp fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public TTurno toTransfer(){
        TTurno tTurno = new TTurno(id, rol.getId(), fechaHoraInicio, fechaHoraFin, -1);
        if(estaAsignado()){
            tTurno.setIdEmpleado(empleado.getId());
        }
        return tTurno;
    }

    // Si empleado es null es que no se ha asignado el turno
    public boolean estaAsignado(){
        return empleado != null;
    }

}
