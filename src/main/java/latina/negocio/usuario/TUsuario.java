package latina.negocio.usuario;


public class TUsuario {
    private int id;
    private String usuario;
    private String contrasenya;
    boolean esGerente;
    private boolean activo;

    public TUsuario(String usuario, String contrasenya, boolean esGerente, boolean activo)
    {
        this.usuario = usuario;
        this.contrasenya = contrasenya;
        this.esGerente = esGerente;
        this.activo = activo;
    }

    public TUsuario(int id, String usuario, String contrasenya, boolean esGerente, boolean activo)
    {
        this.id = id;
        this.usuario = usuario;
        this.contrasenya = contrasenya;
        this.esGerente = esGerente;
        this.activo = activo;
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

    public void getUsuario(String usuario)
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

    public String toString()
    {
        return "Usuario: " + this.usuario + " | Contrasena: " + this.contrasenya + " | Es Gerente: " + this.esGerente;
    }


}
