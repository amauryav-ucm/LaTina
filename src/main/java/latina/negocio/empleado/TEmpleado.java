package latina.negocio.empleado;

public class TEmpleado {
    private int id;
    private String DNI;
    private String nombre;
    private String apellidos;
    private String correo;
    private String telefono;
    private boolean activo;

    public TEmpleado(String DNI, String nombre, String apellidos, String correo, String telefono, boolean activo)
    {
        this.DNI = DNI;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.activo = activo;
    }

    public TEmpleado(int id, String DNI, String nombre, String apellidos, String correo, String telefono, boolean activo)
    {
        this.id = id;
        this.DNI = DNI;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.activo = activo;
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

    public String toString()
    {
        return "DNI: " + DNI + " | Nombre: " + nombre + " | Apellidos: " + apellidos + " | Correo: " + correo
                + " | Telefono: " + telefono;
    }

}