package latina.negocio.empleado;

import jakarta.persistence.*;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.turno.Turno;
import latina.negocio.registro.Registro;

import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name = "Empleado.findByDNI", query = "SELECT e FROM Empleado e WHERE e.DNI = :DNI"),
        @NamedQuery(name = "Empleado.findByCorreo", query = "SELECT e FROM Empleado e WHERE e.correo = :correo"),
        @NamedQuery(name = "Disponibilidad.findByRangoFecha",
                query = "SELECT d FROM Disponibilidad d WHERE d.fechaHoraInicio <= :fechaHoraIni AND d.fechaHoraFin >= :fechaHoraFin"),
        @NamedQuery(name = "Empleado.findAll", query = "SELECT e FROM Empleado e")

})
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String DNI;

    private String nombre;
    private String apellidos;

    @Column(unique = true, nullable = false)
    private String correo;

    private String telefono;
    private boolean activo;

    @OneToMany(mappedBy = "empleado", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Disponibilidad> disponibilidad = new ArrayList<>();

    @OneToMany(mappedBy = "empleado", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Turno> turno = new ArrayList<>();

    /**
     * Relación 1 (Empleado) - N (Registro)
     * Un empleado puede tener múltiples registros; un registro pertenece a un solo empleado.
     */
    @OneToMany(mappedBy = "empleado", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Registro> registros = new ArrayList<>();

    public Empleado() {
    }

    public Empleado(TEmpleado empleado) {
        this.DNI = empleado.getDNI();
        this.nombre = empleado.getNombre();
        this.apellidos = empleado.getApellidos();
        this.correo = empleado.getCorreo();
        this.telefono = empleado.getTelefono();
        this.activo = empleado.isActivo();
        this.disponibilidad = new ArrayList<>();
        this.turno = new ArrayList<>();
        this.registros = new ArrayList<>();
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Disponibilidad> getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(List<Disponibilidad> disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public List<Turno> getTurno() {
        return turno;
    }

    public void setTurno(List<Turno> turno) {
        this.turno = turno;
    }

    public List<Registro> getRegistros() {
        return registros;
    }

    public void setRegistros(List<Registro> registros) {
        this.registros = registros;
    }

    public TEmpleado toTransfer() {
        return new TEmpleado(id, DNI, nombre, apellidos, correo, telefono, activo);
    }
}
