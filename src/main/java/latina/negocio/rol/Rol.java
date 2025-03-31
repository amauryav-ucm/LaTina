package latina.negocio.rol;

import jakarta.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "Rol.findBynombre", query = "select obj from Rol obj where :nombre = obj.nombre "),
        @NamedQuery(name = "Rol.findAll", query = "select r from Rol r")
})
public class Rol {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(unique = true, nullable = false)
    private String nombre;

    private boolean activo;


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    private double salario;

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public boolean isActivo(){return this.activo;}

    public void setActivo(boolean activo){
        this.activo = activo;
    }

    public Rol() {
    }

    public Rol(TRol rol) {
        this.nombre = rol.getNombre();
        this.salario = rol.getSalario();
        this.activo = rol.isActivo();
    }

    public TRol toTransfer(){
        return new TRol(id, nombre, salario, activo);
    }


}