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

        //devuelve lista con empleados
        //se comprueba que la transaccion se realizo de manera correcta y se comprueban los datos de los empleados

        EntityTransaction stubTransaction = mock(EntityTransaction.class);


        EntityManager stubEntityManager = mock(EntityManager.class);
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);


        List<Empleado> empleadosFalsos = new ArrayList<>();
        Empleado emp1 = mock(Empleado.class);
        Empleado emp2 = mock(Empleado.class);

        when(emp1.toTransfer()).thenReturn(new TEmpleado(1, "12345678A", "Juan", "Pérez", "juan@example.com", "600123456", true));
        when(emp2.toTransfer()).thenReturn(new TEmpleado(2, "87654321B", "María", "Gómez", "maria@example.com", "611987654", true));

        empleadosFalsos.add(emp1);
        empleadosFalsos.add(emp2);


        Query stubQueryBuscarEmpleados = mock(Query.class);
        when(stubQueryBuscarEmpleados.getResultList()).thenReturn(empleadosFalsos);
        when(stubEntityManager.createNamedQuery("Empleado.findAll")).thenReturn(stubQueryBuscarEmpleados);

        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();


        List<TEmpleado> resultado = sa.buscarEmpleados();


        verify(stubTransaction, times(1)).begin();
        verify(stubTransaction, times(0)).rollback();


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


        //devuelve lista vacía , se comprueba que la transaccion fue exitosa


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
        when(stubTransaction.isActive()).thenReturn(true);

        
        Query stubQueryBuscarEmpleados = mock(Query.class);
        when(stubQueryBuscarEmpleados.getResultList()).thenThrow(new RuntimeException("Error en BD"));
        when(stubEntityManager.createNamedQuery("Empleado.findAll")).thenReturn(stubQueryBuscarEmpleados);

        SAEmpleadoImp sa = Mockito.spy(new SAEmpleadoImp());
        doReturn(stubEntityManager).when(sa).crearEntityManager();

        List<TEmpleado> resultado = sa.buscarEmpleados();

        // inicia y revierte la transacción correctamente
        verify(stubTransaction, times(1)).begin();
        verify(stubTransaction, times(1)).rollback();  // 🔥 Esto ahora debería ejecutarse

        assertNull(resultado);
    }

}
