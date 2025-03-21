package latina.negocio.turno;

import jakarta.persistence.*;
import latina.negocio.rol.Rol;

import java.sql.Timestamp;

@Entity
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Timestamp fechaHoraInicio;

    private Timestamp fechaHoraFin;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    public Turno(){}

    public Turno(TTurno turno, Rol rol) {
        this.id = turno.getIdTurno();
        this.fechaHoraInicio = turno.getFechaHoraInicio();
        this.fechaHoraFin = turno.getFechaHoraFin();
        this.rol = rol;
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
        return new TTurno(id, rol.getId(), fechaHoraInicio, fechaHoraFin);
    }
}
