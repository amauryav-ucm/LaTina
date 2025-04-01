package latina.negocio.rol.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.negocio.rol.Rol;
import latina.negocio.rol.TRol;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SARolImpTest {

    @Test
    void testNombreRepetido() {
        // Como son test unitarios, no pueden conectarse con la base de datos ni con nada
        // de eso y se deben crear stubs para las funciones

        // Creamos un rol falso que estaria en teoria dentro de la base de datos


        Rol stubRol = new Rol();
        stubRol.setActivo(true);
        stubRol.setNombre("MIROL");
        stubRol.setSalario(10);

        // Lo metemos en una lista porque es lo que devuelve la query

        List<Object> stubResultList = new ArrayList<>();
        stubResultList.add(stubRol);

        // Creamos una query falsa que cuando se le pida los resultados devuelva la lista que creamos antes

        Query stubQueryBuscarPorNombre = mock(Query.class);
        when(stubQueryBuscarPorNombre.getResultList()).thenReturn(stubResultList);

        // Creamos una transaccion falsa para devolver cuando se pida al EM

        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.createNamedQuery("Rol.findBynombre")).thenReturn(stubQueryBuscarPorNombre);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        SARolImp sa = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        TRol trol = new TRol("MIROL", 10, true);
        int resultado = sa.altaRol(trol);

        assertEquals(-1, resultado);
    }

    @Test
    void testSalarioNegativo() {
        // Simulamos una transacción falsa
        EntityTransaction stubTransaction = mock(EntityTransaction.class);

        // Simulamos un EntityManager falso
        EntityManager stubEntityManager = mock(EntityManager.class);

        // Cuando se llame a getTransaction(), devolvemos la transacción falsa
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Simulamos que la consulta "Rol.findBynombre" devuelve una lista vacía (rol no existe)
        Query stubQueryBuscarPorNombre = mock(Query.class);
        when(stubQueryBuscarPorNombre.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Rol.findBynombre")).thenReturn(stubQueryBuscarPorNombre);

        // Espiamos la clase SARolImp para controlar el EntityManager
        SARolImp sa = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Creamos un rol con salario negativo
        TRol trol = new TRol("NUEVOROL", -5, true);

        // Llamamos a la función
        int resultado = sa.altaRol(trol);

        // Verificamos que si el salario es negativo, se hace rollback
        verify(stubTransaction, times(1)).rollback();

        // Como el salario es negativo, debería devolver -2
        assertEquals(-2, resultado);
    }
    @Test
    void testSalarioCero() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        Query stubQueryBuscarPorNombre = mock(Query.class);
        when(stubQueryBuscarPorNombre.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Rol.findBynombre")).thenReturn(stubQueryBuscarPorNombre);

        SARolImp sa = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        TRol trol = new TRol("GERENTE", 0, true);
        int resultado = sa.altaRol(trol);

        verify(stubTransaction, times(1)).rollback();
        assertEquals(-2, resultado);
    }
    @Test
    void testNombreCaracteresEspeciales() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        Query stubQueryBuscarPorNombre = mock(Query.class);
        when(stubQueryBuscarPorNombre.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Rol.findBynombre")).thenReturn(stubQueryBuscarPorNombre);

        SARolImp sa = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        TRol trol = new TRol("@ROL!", 15, true);
        int resultado = sa.altaRol(trol);

        verify(stubTransaction, times(1)).rollback();
        assertEquals(-3, resultado);
    }
    @Test
    void testPersistenciaFalla() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        Query stubQueryBuscarPorNombre = mock(Query.class);
        when(stubQueryBuscarPorNombre.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Rol.findBynombre")).thenReturn(stubQueryBuscarPorNombre);

        SARolImp sa = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        doThrow(new RuntimeException("Error en persistencia")).when(stubEntityManager).persist(any(Rol.class));

        TRol trol = new TRol("SUPERVISOR", 30, true);
        int resultado = sa.altaRol(trol);

        assertEquals(-4, resultado);
    }


    @Test
    void testNombreFormatoIncorrecto() {
        // Simulamos una transacción falsa
        EntityTransaction stubTransaction = mock(EntityTransaction.class);

        // Simulamos un EntityManager falso
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Simulamos que la consulta "Rol.findBynombre" devuelve una lista vacía (rol no existe)
        Query stubQueryBuscarPorNombre = mock(Query.class);
        when(stubQueryBuscarPorNombre.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Rol.findBynombre")).thenReturn(stubQueryBuscarPorNombre);

        // Espiamos la clase SARolImp para controlar el EntityManager
        SARolImp sa = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Creamos un TRol con un nombre inválido (contiene números y minúsculas)
        TRol trol = new TRol("Nombre123", 15, true);

        // Llamamos a la función
        int resultado = sa.altaRol(trol);

        // Verificamos que si el nombre es inválido, se hace rollback
        verify(stubTransaction, times(1)).rollback();

        // Como el nombre tiene formato incorrecto, debería devolver -3
        assertEquals(-3, resultado);
    }


    @Test
    void testAltaRolExitoso() {
        // Simulamos una transacción falsa
        EntityTransaction stubTransaction = mock(EntityTransaction.class);

        // Simulamos un EntityManager falso
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Simulamos que la consulta "Rol.findBynombre" devuelve una lista vacía (rol no existe)
        Query stubQueryBuscarPorNombre = mock(Query.class);
        when(stubQueryBuscarPorNombre.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Rol.findBynombre")).thenReturn(stubQueryBuscarPorNombre);

        // Espiamos la clase SARolImp para controlar el EntityManager
        SARolImp sa = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Creamos un TRol válido (nuevo rol con salario positivo y nombre correcto)
        TRol trol = new TRol("NUEVOROL", 20, true);

        // Simulamos que el persist() asigna un ID mayor que 0
        doAnswer(invocation -> {
            Rol nuevoRol = invocation.getArgument(0); // Obtenemos el objeto Rol pasado a persist()
            nuevoRol.setId(1); // Asignamos un ID válido
            return null;
        }).when(stubEntityManager).persist(any(Rol.class));

        // Llamamos a la función
        int resultado = sa.altaRol(trol);

        // Verificamos que commit() fue llamado
        verify(stubTransaction, times(1)).commit();

        // El ID devuelto debe ser mayor que 0
        assertTrue(resultado > 0);
    }

    // TESTS PARA BUSCAR ROLES
    @Test
    void testBuscarRoles_Exitoso() {
        // Simulamos una transacción falsa
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        Query stubQuery = mock(Query.class);

        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery("Rol.findAll")).thenReturn(stubQuery);

        // Creamos varios roles
        List<Rol> stubRoles = new ArrayList<>();
        stubRoles.add(new Rol(new TRol(1, "COCINERO", 6, true)));
        stubRoles.add(new Rol(new TRol(2, "MAITRE", 5, true)));

        when(stubQuery.getResultList()).thenReturn(stubRoles);

        // Espiamos la clase SARolImp para controlar el EntityManager
        SARolImp sa = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Llamamos a la función
        List<TRol> resultado = sa.buscarRoles();

        verify(stubTransaction, times(1)).commit();
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("COCINERO", resultado.get(0).getNombre());
        assertEquals(6, resultado.get(0).getSalario());
        assertTrue(resultado.get(0).isActivo());
    }

    @Test
    void testBuscarRoles_SinResultados() {
        // Simulamos una transacción falsa
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        Query stubQuery = mock(Query.class);

        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery("Rol.findAll")).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>());

        SARolImp saRol = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(saRol).crearEntityManager();

        List<TRol> resultado = saRol.buscarRoles();

        verify(stubTransaction, times(1)).commit();
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
    @Test
    void testBuscarRoles_PersistenciaFalla() {
        // Simulamos una transacción falsa
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);

        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery("Rol.findAll")).thenThrow(new RuntimeException("Error en persistencia"));

        SARolImp saRol = Mockito.spy(new SARolImp());
        doReturn(stubEntityManager).when(saRol).crearEntityManager();

        List<TRol> resultado = saRol.buscarRoles();

        assertNull(resultado);
    }
}
