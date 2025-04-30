package latina.negocio.turno;

import jakarta.persistence.*;
import latina.negocio.empleado.Empleado;
import latina.negocio.rol.Rol;
import latina.negocio.registro.Registro;

import java.sql.Timestamp;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "Turno.findByDia",
                query = "SELECT t FROM Turno t WHERE CAST(t.fechaHoraInicio AS DATE) = :dia AND t.empleado IS NULL"
        ),
        @NamedQuery(
                name = "Turno.findByDiaAsignados",
                query = "SELECT t FROM Turno t WHERE CAST(t.fechaHoraInicio AS DATE) = :dia AND t.empleado IS NOT NULL"
        )
})
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Timestamp fechaHoraInicio;
    private Timestamp fechaHoraFin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    // Puede tener un empleado null, que significa que el turno aún no está asignado
    @ManyToOne(optional = true)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    /**
     * Lado inverso de la relación 1-1 con Registro.
     * Un turno puede tener asociado un único registro de fichaje.
     */
    @OneToOne(mappedBy = "turno", fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL)
    private Registro registro;

    public Turno() {}

    public Turno(TTurno turno, Rol rol) {
        this.id = turno.getIdTurno();
        this.fechaHoraInicio = turno.getFechaHoraInicio();
        this.fechaHoraFin = turno.getFechaHoraFin();
        this.rol = rol;
    }

    public Turno(TTurno turno, Rol rol, Empleado empleado) {
        this(turno, rol);
        this.empleado = empleado;
    }

    // Getters y setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Registro getRegistro() {
        return registro;
    }

    public void setRegistro(Registro registro) {
        this.registro = registro;
    }

    public TTurno toTransfer() {
        TTurno tTurno = new TTurno(id, rol.getId(), fechaHoraInicio, fechaHoraFin, -1);
        if (empleado != null) {
            tTurno.setIdEmpleado(empleado.getId());
        }
        return tTurno;
    }

    // Si empleado es null es que no se ha asignado el turno
    public boolean estaAsignado() {
        return empleado != null;
    }

    /**
     * @param inicio el inicio de la ventana a comparar
     * @param fin    el final de la ventana a comparar, debe ser posterior a inicio
     * @returns true si el turno solapa con la ventana definida por inicio y fin
     */
    public boolean solapaCon(Timestamp inicio, Timestamp fin) {
        return !(fechaHoraInicio.after(fin)
                || fechaHoraFin.before(inicio)
                || fechaHoraInicio.equals(fin)
                || fechaHoraFin.equals(inicio));
    }
}
