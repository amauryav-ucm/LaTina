package latina.negocio.usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.negocio.usuario.imp.SAUsuarioImp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SAUsuarioImpTest {
    @Test
    void testAltaUsuarioCorrecto() {
        TUsuario stubUsuario = new TUsuario("juanito123", "password123", true, false);
        Usuario usuarioEntity = new Usuario(stubUsuario);

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Forzar que al persistir el usuario se le asigne un ID
        doAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(42); // ID simulado
            return null;
        }).when(stubEntityManager).persist(any(Usuario.class));

        SAUsuarioImp sa = Mockito.spy(new SAUsuarioImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        int resultado = sa.altaUsuario(stubUsuario);

        assertEquals(42, resultado);
        verify(stubTransaction).begin();
        verify(stubTransaction).commit();
        verify(stubEntityManager).persist(any(Usuario.class));
        verify(stubEntityManager).close();
    }

    @Test
    void testAltaUsuarioConExcepcion() {
        TUsuario stubUsuario = new TUsuario("juanito123", "password123", true, false);

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        when(stubTransaction.isActive()).thenReturn(true);

        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        doThrow(new RuntimeException("Fallo al persistir")).when(stubEntityManager).persist(any(Usuario.class));

        SAUsuarioImp sa = Mockito.spy(new SAUsuarioImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        int resultado = sa.altaUsuario(stubUsuario);

        assertEquals(-1, resultado);
        verify(stubTransaction).rollback();
        verify(stubEntityManager).close();
    }

    @Test
    void testIniciarSesionCorrectoGerente() {
        TUsuario stubUsuario = new TUsuario("gerente1", "clave123", true, true);
        Usuario usuarioEntity = new Usuario(stubUsuario);
        List<Usuario> usuarios = List.of(usuarioEntity);

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter("usuario", stubUsuario.getUsuario())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(usuarios);

        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery("Usuario.findByNombreUsuario")).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        SAUsuarioImp sa = Mockito.spy(new SAUsuarioImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        int resultado = sa.iniciarSesion(stubUsuario);

        assertEquals(2, resultado); // Porque es gerente
        verify(stubTransaction).commit();
        verify(stubEntityManager).close();
    }

    @Test
    void testIniciarSesionUsuarioNoEncontrado() {
        TUsuario stubUsuario = new TUsuario("desconocido", "clave", true, false);
        List<Usuario> usuarios = new ArrayList<>();

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter("usuario", stubUsuario.getUsuario())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(usuarios);

        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery("Usuario.findByNombreUsuario")).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        SAUsuarioImp sa = Mockito.spy(new SAUsuarioImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        int resultado = sa.iniciarSesion(stubUsuario);

        assertEquals(-1, resultado); // Usuario no existe
        verify(stubTransaction).commit();
    }

    @Test
    void testIniciarSesionContrasenyaIncorrecta() {
        // Crear un usuario con la contraseña correcta
        TUsuario usuarioCorrecto = new TUsuario("usuario123", "claveCorrecta", true, false);
        Usuario usuarioEntity = new Usuario(usuarioCorrecto);

        // Usuario que intenta iniciar sesión con contraseña incorrecta
        TUsuario stubUsuario = new TUsuario("usuario123", "claveIncorrecta", true, false);

        List<Usuario> usuarios = List.of(usuarioEntity);

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter("usuario", stubUsuario.getUsuario())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(usuarios);

        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery("Usuario.findByNombreUsuario")).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        SAUsuarioImp sa = Mockito.spy(new SAUsuarioImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        int resultado = sa.iniciarSesion(stubUsuario);

        assertEquals(-2, resultado); // Contraseña incorrecta
    }


    @Test
    void testIniciarSesionUsuarioInactivo() {
        // Crear usuario inactivo
        TUsuario usuarioInactivo = new TUsuario("usuario123", "clave123", false, false);
        Usuario usuarioEntity = new Usuario(usuarioInactivo);

        // Usuario que intenta iniciar sesión (suponiendo que cree estar activo)
        TUsuario stubUsuario = new TUsuario("usuario123", "clave123", true, false);

        List<Usuario> usuarios = List.of(usuarioEntity);

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        Query stubQuery = mock(Query.class);
        when(stubQuery.setParameter("usuario", stubUsuario.getUsuario())).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(usuarios);

        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery("Usuario.findByNombreUsuario")).thenReturn(stubQuery);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        SAUsuarioImp sa = Mockito.spy(new SAUsuarioImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        int resultado = sa.iniciarSesion(stubUsuario);

        assertEquals(-3, resultado); // Usuario inactivo
    }


    @Test
    void testIniciarSesionConExcepcion() {
        TUsuario stubUsuario = new TUsuario("usuario123", "clave", true, false);

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        when(stubTransaction.isActive()).thenReturn(true);

        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery(anyString())).thenThrow(new RuntimeException("Error"));

        SAUsuarioImp sa = Mockito.spy(new SAUsuarioImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        int resultado = sa.iniciarSesion(stubUsuario);

        assertEquals(-4, resultado);
        verify(stubTransaction).rollback();
        verify(stubEntityManager).close();
    }
}


