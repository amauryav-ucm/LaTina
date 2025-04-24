package latina.negocio.registro;

import jakarta.persistence.*;
import latina.negocio.empleado.Empleado;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;

@Entity
@NamedQueries({
    @NamedQuery(name="Registro.findByEmpleado", query="SELECT r FROM Registro r WHERE r.empleado.id = :empleadoId")
})
public class Registro {
   // id/ id turno/ id empleado/ nhoras/ salario/ hInicio / hFin
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    @Column(unique = true, nullable = false)
    private int nHoras;
    private double salario;
    private Timestamp hInicio;
    private Timestamp hFin;
    @ManyToOne(optional = true)
    @JoinColumn(name = "turno_id")
    private Turno turno;
    @ManyToOne(optional = true)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;


    public Registro(){}

    public Registro( Empleado emp,Timestamp horactual , int nHoras){
        this.nHoras = nHoras;
        this.empleado = emp;
        this.hInicio = horactual;
    }

    public Registro(TRegistro registro, Turno turno, Empleado empleado){
        this.id = registro.getId();
        this.nHoras = registro.getnHoras();
        this.salario = registro.getSalario();
        this.turno = turno;
        this.empleado = empleado;
        this.hInicio = registro.gethInicio();
        this.hFin = registro.gethFin();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getnHoras() {
        return nHoras;
    }

    public void setnHoras(int nHoras) {
        this.nHoras = nHoras;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
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

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }


    public TRegistro toTransfer(){
        TRegistro tReg = new TRegistro(id, turno.getId(), empleado.getId(), hInicio, hFin, salario, nHoras );
        return tReg;
    }
}
