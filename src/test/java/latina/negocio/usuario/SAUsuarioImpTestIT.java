package latina.negocio.usuario;

import jakarta.persistence.EntityManager;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.usuario.imp.SAUsuarioImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SAUsuarioImpTestIT {
    private SAUsuario sau;
    private EntityManager em;

    @BeforeEach
    public void setUp() {
        try {
            Field instancia = EMFContainer.class.getDeclaredField("emfc");
            instancia.setAccessible(true);
            instancia.set(null, new EMFContainerImpTest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        sau = new SAUsuarioImp();
    }

    @Test
    public void altaUsuarioExitoso() {
        int id = sau.altaUsuario(new TUsuario("12345678N", "contrasena", false, true));
        //Exito
        assertTrue(id > 0);
    }

    @Test
    public void iniciarSesionExitosoEmpleado() {
        em.getTransaction().begin();
        Usuario usuario = new Usuario(new TUsuario("12345678A", "contrasena", false, true));
        em.persist(usuario);
        em.getTransaction().commit();
        int result = sau.iniciarSesion(new TUsuario(usuario.getUsuario(), usuario.getContrasenya(), false, true));
        //Exito
        assertTrue(result == 1);
    }

    @Test
    public void iniciarSesionExitosoGerente() {
        em.getTransaction().begin();
        Usuario gerente = new Usuario(new TUsuario("87654321A", "contrasenaG", true, true));
        em.persist(gerente);
        em.getTransaction().commit();
        int result = sau.iniciarSesion(new TUsuario(gerente.getUsuario(), gerente.getContrasenya(), true, true));
        //Exito
        assertTrue(result == 2);
    }

    @Test
    public void iniciarSesionUsuarioNoExistente() {
        int result = sau.iniciarSesion(new TUsuario("12345678B", "contrasena", false, true));
        //Exito
        assertTrue(result == -1);
    }

    @Test
    public void iniciarSesionUsuarioFalloContrasena() {
        em.getTransaction().begin();
        Usuario usuario = new Usuario(new TUsuario("12345678A", "contrasena", false, true));
        em.persist(usuario);
        em.getTransaction().commit();
        int result = sau.iniciarSesion(new TUsuario(usuario.getUsuario(), "contraFalsa", false, true));
        //Exito
        assertTrue(result == -2);
    }

    @Test
    public void iniciarSesionUsuarioFalloNoActivo() {
        em.getTransaction().begin();
        Usuario usuario = new Usuario(new TUsuario("12345678A", "contrasena", false, false));
        em.persist(usuario);
        em.getTransaction().commit();
        int result = sau.iniciarSesion(new TUsuario(usuario.getUsuario(), usuario.getContrasenya(), false, true));
        //Exito
        assertTrue(result == -3);
    }
}
