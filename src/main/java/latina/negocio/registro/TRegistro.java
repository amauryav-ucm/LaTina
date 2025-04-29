package latina.negocio.registro;

import java.sql.Timestamp;

public class TRegistro {
    private int id;
    //private int nHoras;
    private double nHoras;
    private double salario;
    private int idTurno;
    private int idEmpleado;
    private Timestamp hInicio;
    private Timestamp hFin;

    /*public TRegistro(int idReg, int idTurno, int idEmpleado, Timestamp hIni, Timestamp hFin, double salario, int nHoras){
        this.id = idReg;
        this.idTurno = idTurno;
        this.idEmpleado = idEmpleado;
        this.hInicio = hIni;
        this.hFin = hFin;
        this.salario = salario;
        this.nHoras = nHoras;
    }*/
    public TRegistro(int idReg, int idTurno, int idEmpleado, Timestamp hIni, Timestamp hFin, double salario, double nHoras){
        this.id = idReg;
        this.idTurno = idTurno;
        this.idEmpleado = idEmpleado;
        this.hInicio = hIni;
        this.hFin = hFin;
        this.salario = salario;
        this.nHoras = nHoras;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /*public int getnHoras() {
        return nHoras;
    }*/
    public double getnHoras() {
        return nHoras;
    }

    /*public void setnHoras(int nHoras) {
        this.nHoras = nHoras;
    }*/
    public void setnHoras(double nHoras) {
        this.nHoras = nHoras;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public void setIdTurno(int idTurno) {
        this.idTurno = idTurno;
    }

    public int getIdTurno() {
        return idTurno;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Timestamp gethInicio() {
        return hInicio;
    }

    public void sethInicio(Timestamp hInicio) {
        this.hInicio = hInicio;
    }

    public Timestamp gethFin() {
        return hFin;
    }

    public void sethFin(Timestamp hFin) {
        this.hFin = hFin;
    }
}
