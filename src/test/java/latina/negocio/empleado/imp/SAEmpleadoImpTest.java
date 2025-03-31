package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SAEmpleadoImpTest {

    @Test
    void testBuscarEmpleados_DevuelveLista() {
        // Simulamos una transacción falsa
        EntityTransaction stubTransaction = mock(EntityTransaction.class);

        // Simulamos un EntityManager falso
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Creamos una lista de empleados simulados
        List<Empleado> empleadosFalsos = new ArrayList<>();
        Empleado emp1 = mock(Empleado.class);
        Empleado emp2 = mock(Empleado.class);

        when(emp1.toTransfer()).thenReturn(new TEmpleado(1, "12345678A", "Juan", "Pérez", "juan@example.com", "600123456", true));
        when(emp2.toTransfer()).thenReturn(new TEmpleado(2, "87654321B", "María", "Gómez", "maria@example.com", "611987654", true));

        empleadosFalsos.add(emp1);
        empleadosFalsos.add(emp2);

        // Simulamos una consulta que devuelve la lista de empleados falsos
        Query stubQueryBuscarEmpleados = mock(Query.class);
        when(stubQueryBuscarEmpleados.getResultList()).thenReturn(empleadosFalsos);
        when(stubEntityManager.createNamedQuery("Empleado.findAll")).thenReturn(stubQueryBuscarEmpleados);

        // Espiamos la clase SAEmpleadoImp para controlar el EntityManager
        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        // Llamamos a la función
        List<TEmpleado> resultado = sa.buscarEmpleados();

        // Verificamos que la transacción se inició y finalizó correctamente
        verify(stubTransaction, times(1)).begin();
        verify(stubTransaction, times(0)).rollback();

        // Comprobamos que la lista devuelta tiene los empleados esperados
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getId());
        assertEquals("12345678A", resultado.get(0).getDNI());
        assertEquals("Juan", resultado.get(0).getNombre());
        assertEquals("Pérez", resultado.get(0).getApellidos());
        assertEquals("600123456", resultado.get(0).getTelefono());

        assertEquals(2, resultado.get(1).getId());
        assertEquals("87654321B", resultado.get(1).getDNI());
        assertEquals("María", resultado.get(1).getNombre());
        assertEquals("Gómez", resultado.get(1).getApellidos());
        assertEquals("611987654", resultado.get(1).getTelefono());
    }

    @Test
    void testBuscarEmpleados_ListaVacia() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);

        // Simulamos una consulta que devuelve una lista vacía
        Query stubQueryBuscarEmpleados = mock(Query.class);
        when(stubQueryBuscarEmpleados.getResultList()).thenReturn(new ArrayList<>());
        when(stubEntityManager.createNamedQuery("Empleado.findAll")).thenReturn(stubQueryBuscarEmpleados);

        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        List<TEmpleado> resultado = sa.buscarEmpleados();

        verify(stubTransaction, times(1)).begin();
        verify(stubTransaction, times(0)).rollback();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testBuscarEmpleados_ErrorBaseDatos() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);

        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubTransaction.isActive()).thenReturn(true);  // 🔥 Aseguramos que rollback() puede ejecutarse

        // Simulamos que la consulta lanza una excepción
        Query stubQueryBuscarEmpleados = mock(Query.class);
        when(stubQueryBuscarEmpleados.getResultList()).thenThrow(new RuntimeException("Error en BD"));
        when(stubEntityManager.createNamedQuery("Empleado.findAll")).thenReturn(stubQueryBuscarEmpleados);

        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        List<TEmpleado> resultado = sa.buscarEmpleados();

        // Verificamos que se inicia y revierte la transacción correctamente
        verify(stubTransaction, times(1)).begin();
        verify(stubTransaction, times(1)).rollback();  // 🔥 Esto ahora debería ejecutarse

        assertNull(resultado);
    }

}
