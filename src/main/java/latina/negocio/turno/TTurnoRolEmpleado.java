package latina.negocio.turno;

import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.rol.Rol;
import latina.negocio.rol.TRol;

import java.sql.Timestamp;

public class TTurnoRolEmpleado {
    private int idTurno;
    private Timestamp fechaHoraInicio;
    private Timestamp fechaHoraFin;
    private int idRol;
    private String nombreRol;
    private double salarioRol;
    // Sera -1 cuando no esta asignado
    private int idEmpleado;
    private String DNIEmpleado;
    private String nombreEmpleado;
    private String apellidosEmpleado;
    private String correoEmpleado;
    private String telefonoEmpleado;

    public TTurnoRolEmpleado(TTurno turno, TRol rol, TEmpleado empleado) {
        idTurno = turno.getIdTurno();
        fechaHoraInicio = turno.getFechaHoraInicio();
        fechaHoraFin = turno.getFechaHoraFin();
        idRol = rol.getId();
        nombreRol = rol.getNombre();
        salarioRol = rol.getSalario();
        idEmpleado = empleado.getId();
        DNIEmpleado = empleado.getDNI();
        nombreEmpleado = empleado.getNombre();
        apellidosEmpleado = empleado.getApellidos();
        correoEmpleado = empleado.getCorreo();
        telefonoEmpleado = empleado.getTelefono();
    }

    public TTurnoRolEmpleado(TTurno turno, TRol rol) {
        idTurno = turno.getIdTurno();
        fechaHoraInicio = turno.getFechaHoraInicio();
        fechaHoraFin = turno.getFechaHoraFin();
        idRol = rol.getId();
        nombreRol = rol.getNombre();
        salarioRol = rol.getSalario();
        idEmpleado = -1;
    }

    public int getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
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

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public double getSalarioRol() {
        return salarioRol;
    }

    public void setSalarioRol(double salarioRol) {
        this.salarioRol = salarioRol;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getDNIEmpleado() {
        return DNIEmpleado;
    }

    public void setDNIEmpleado(String DNIEmpleado) {
        this.DNIEmpleado = DNIEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getApellidosEmpleado() {
        return apellidosEmpleado;
    }

    public void setApellidosEmpleado(String apellidosEmpleado) {
        this.apellidosEmpleado = apellidosEmpleado;
    }

    public String getCorreoEmpleado() {
        return correoEmpleado;
    }

    public void setCorreoEmpleado(String correoEmpleado) {
        this.correoEmpleado = correoEmpleado;
    }

    public String getTelefonoEmpleado() {
        return telefonoEmpleado;
    }

    public void setTelefonoEmpleado(String telefonoEmpleado) {
        this.telefonoEmpleado = telefonoEmpleado;
    }

}
