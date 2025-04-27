package latina.negocio.registro;

import jakarta.persistence.*;
import latina.negocio.empleado.Empleado;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;

@Entity
@NamedQueries({
        @NamedQuery(
                name  = "Registro.findByEmpleado",
                query = "SELECT r FROM Registro r WHERE r.empleado.id = :empleadoId"
        ),
        @NamedQuery(
                name  = "Registro.findByEmpleadoAndTurno",
                query = "SELECT r FROM Registro r WHERE r.empleado.id = :empleadoId AND r.turno.id = :turnoId"
        ),
        @NamedQuery(
                name  = "Registro.findLatestOpenByEmpleado",
                query = "SELECT r FROM Registro r WHERE r.empleado.id = :empleadoId AND r.hFin IS NULL"
        )
})
public class Registro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    //private int nHoras;
    private double nHoras;

    private double salario;

    private Timestamp hInicio;

    private Timestamp hFin;

    /**
     * Un registro está asociado a un único turno (1-1)
     */
    @OneToOne(optional = true)
    @JoinColumn(name = "turno_id", unique = true)
    private Turno turno;

    /**
     * Varios registros pueden pertenecer a un mismo empleado (N-1 desde Registro)
     * y en el lado Empleado, 1 empleado -> N registros
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    public Registro() {
    }

    /**
     * Constructor para iniciar un registro en curso
     */
    public Registro(Empleado empleado, Timestamp hInicio, int nHoras) {
        this.empleado = empleado;
        this.hInicio = hInicio;
        this.nHoras = nHoras;
    }

    /**
     * Constructor desde DTO
     */
    public Registro(TRegistro registro, Turno turno, Empleado empleado) {
        this.id = registro.getId();
        this.nHoras = registro.getnHoras();
        this.salario = registro.getSalario();
        this.turno = turno;
        this.empleado = empleado;
        this.hInicio = registro.gethInicio();
        this.hFin = registro.gethFin();
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /*public int getnHoras() { return nHoras;}*/
    public double getnHoras() { return nHoras;}

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

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    /**
     * Convertir a DTO
     */
    public TRegistro toTransfer() {
        TRegistro tReg = new TRegistro(
                id,
                turno != null ? turno.getId() : null,
                empleado.getId(),
                hInicio,
                hFin,
                salario,
                nHoras
        );
        return tReg;
    }
}
