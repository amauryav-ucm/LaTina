package latina.negocio.turno;

import java.sql.Timestamp;

public class TTurno {

    private int idTurno;

    private int idRol;

    private Timestamp fechaHoraInicio;

    private Timestamp fechaHoraFin;

    // Sera -1 cuando no esta asignado
    private int idEmpleado;

    public TTurno()
    {

    }

    public TTurno(int idTurno, int idRol, Timestamp fechaHoraInicio, Timestamp fechaHoraFin) {
        this.idTurno = idTurno;
        this.idRol = idRol;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
    }

    public TTurno(int idTurno, int idRol, Timestamp fechaHoraInicio, Timestamp fechaHoraFin, int idEmpleado) {
        this.idTurno = idTurno;
        this.idRol = idRol;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.idEmpleado = idEmpleado;
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

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public boolean estaAsignado(){
        return idEmpleado != -1;
    }
}
