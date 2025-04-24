package latina.negocio.registro.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.registro.Registro;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SARegistroImpTest {

    @Test
    void testFicharEntrada_EmpleadoNoExiste() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        Query stubQuery = mock(Query.class);

        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery("Empleado.findByDNI")).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>());

        SARegistroImp saRegistro = Mockito.spy(new SARegistroImp());
        doReturn(stubEntityManager).when(saRegistro).createEntityManager();

        TEmpleado empleado = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correoo@example.com", "123456788", true);
        int resultado = saRegistro.ficharEntrada(empleado, new Timestamp(System.currentTimeMillis()));

        verify(stubTransaction, times(1)).rollback();
        assertEquals(-1, resultado);
    }

    @Test
    void testFicharEntrada_Exito() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        Query stubQuery = mock(Query.class);

        Empleado mockEmpleado = mock(Empleado.class);
        List<Empleado> lista = new ArrayList<>();
        lista.add(mockEmpleado);

        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery("Empleado.findByDNI")).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(lista);

        SARegistroImp saRegistro = Mockito.spy(new SARegistroImp());
        doReturn(stubEntityManager).when(saRegistro).createEntityManager();

        TEmpleado empleado = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correoo@example.com", "123456788", true);
        int resultado = saRegistro.ficharEntrada(empleado, new Timestamp(System.currentTimeMillis()));

        verify(stubTransaction, times(1)).commit();
        verify(stubEntityManager, times(1)).persist(any(Registro.class));
        assertEquals(1, resultado);
    }
    /*@Test
    void testFicharEntrada_Exception() {
        // Simulando una excepción en el proceso
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        Query stubQuery = mock(Query.class);

        // Configurando la simulación para lanzar una excepción en getResultList
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery("Empleado.findByDNI")).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenThrow(new RuntimeException("Error de base de datos"));

        // Creamos el objeto SARegistroImp como un espía
        SARegistroImp saRegistro = Mockito.spy(new SARegistroImp());
        // Le decimos que devuelva el EntityManager simulado cuando se llame a createEntityManager
        doReturn(stubEntityManager).when(saRegistro).createEntityManager();

        // Creamos un TEmpleado para pasar al método
        TEmpleado empleado = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correoo@example.com", "123456788", true);

        // Ejecutamos el método
        int resultado = saRegistro.ficharEntrada(empleado, new Timestamp(System.currentTimeMillis()));

        // Verificamos que el rollback haya ocurrido
        verify(stubTransaction, times(1)).rollback();

        // Aseguramos que el resultado sea -4 (código de error para excepción general)
        assertEquals(-4, resultado);

        // Verificación adicional para asegurarnos de que no se hizo commit
        verify(stubTransaction, times(0)).commit();
    }*/



    @Test
    void testFicharEntrada_TransaccionNoActiva() {
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        Query stubQuery = mock(Query.class);

        // La transacción no está activa, por lo que no se debería hacer commit ni persist
        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery("Empleado.findByDNI")).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>()); // Empleado no existe

        // Simulamos que la transacción ya está marcada como no activa
        when(stubTransaction.isActive()).thenReturn(false);

        SARegistroImp saRegistro = Mockito.spy(new SARegistroImp());
        doReturn(stubEntityManager).when(saRegistro).createEntityManager();

        TEmpleado empleado = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correoo@example.com", "123456788", true);
        int resultado = saRegistro.ficharEntrada(empleado, new Timestamp(System.currentTimeMillis()));

        verify(stubTransaction, never()).commit(); // No debe intentar hacer commit
        verify(stubEntityManager, never()).persist(any(Registro.class)); // No debe persistir nada
        assertEquals(-1, resultado); // El empleado no existe, por lo que el código debería ser -1
    }

    @Test
    void testFicharEntrada_RollbackOnFailure() {
        // Simulando que se intenta fichar pero ocurre algún fallo (por ejemplo, empleado no existe)
        EntityTransaction stubTransaction = mock(EntityTransaction.class);
        EntityManager stubEntityManager = mock(EntityManager.class);
        Query stubQuery = mock(Query.class);

        when(stubEntityManager.getTransaction()).thenReturn(stubTransaction);
        when(stubEntityManager.createNamedQuery("Empleado.findByDNI")).thenReturn(stubQuery);
        when(stubQuery.getResultList()).thenReturn(new ArrayList<>()); // Empleado no existe

        SARegistroImp saRegistro = Mockito.spy(new SARegistroImp());
        doReturn(stubEntityManager).when(saRegistro).createEntityManager();

        TEmpleado empleado = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correoo@example.com", "123456788", true);
        int resultado = saRegistro.ficharEntrada(empleado, new Timestamp(System.currentTimeMillis()));

        // Verificar que la transacción se haga rollback si algo sale mal
        verify(stubTransaction, times(1)).rollback(); // Debe llamar a rollback
        assertEquals(-1, resultado); // El empleado no existe, lo que retorna -1
    }


}
