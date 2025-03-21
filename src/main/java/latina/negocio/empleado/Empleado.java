package latina.negocio.empleado;

import jakarta.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "Empleado.findByDNI", query = "select obj from Empleado obj where :DNI = obj.DNI "),
        @NamedQuery(name = "Empleado.findByCorreo", query = "select obj from Empleado obj where :correo = obj.correo ")
})

public class Empleado {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    @Column(unique = true, nullable = false)
    private String DNI;
    private String nombre;
    private String apellidos;
    @Column(unique = true, nullable = false)
    private String correo;
    private String telefono;
    private boolean activo;

    public Empleado() {

    }

    public Empleado(TEmpleado empleado) {
        this.DNI = empleado.getDNI();
        this.nombre = empleado.getNombre();
        this.apellidos = empleado.getApellidos();
        this.correo = empleado.getCorreo();
        this.telefono = empleado.getTelefono();
        this.activo = empleado.isActivo();
    }

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

    public String getNombre()
    {
        return this.nombre;
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
        return this.activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}
