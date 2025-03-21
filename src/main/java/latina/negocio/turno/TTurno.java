package latina.negocio.turno;

import java.sql.Timestamp;

public class TTurno {

    private int idTurno;

    private int idRol;

    private Timestamp fechaHoraInicio;

    private Timestamp fechaHoraFin;

    public TTurno(int idTurno, int idRol, Timestamp fechaHoraInicio, Timestamp fechaHoraFin) {
        this.idTurno = idTurno;
        this.idRol = idRol;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
    }

    public int getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public Timestamp getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(Timestamp fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public Timestamp getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(Timestamp fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
}
