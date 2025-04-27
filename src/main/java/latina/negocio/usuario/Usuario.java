package latina.negocio.usuario;

import jakarta.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "Usuario.findByNombreUsuario", query = "select obj from Usuario obj where :usuario = obj.usuario "),
        @NamedQuery(name = "Usuario.findGerente", query = "SELECT obj FROM Usuario obj WHERE obj.esGerente = true")
})
public class Usuario {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;
    @Column(unique = true, nullable = false)
    private String usuario;
    private String contrasenya;
    boolean esGerente;
    boolean activo;

    public Usuario(){}

    public Usuario(TUsuario usuario)
    {
        this.usuario = usuario.getUsuario();
        this.contrasenya = usuario.getContrasenya();
        this.esGerente = usuario.isEsGerente();
        this.activo = usuario.isActivo();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario()
    {
        return this.usuario;
    }

    public void setUsuario(String usuario)
    {
        this.usuario = usuario;
    }

    public String getContrasenya()
    {
        return this.contrasenya;
    }

    public void setContrasenya(String contrasenya)
    {
        this.contrasenya = contrasenya;
    }

    public boolean isEsGerente()
    {
        return this.esGerente;
    }

    public void setEsGerente(boolean esGerente)
    {
        this.esGerente = esGerente;
    }

    public boolean isActivo() {

        return this.activo;
    }

    public void setActivo(boolean activo) {

        this.activo = activo;
    }
}
