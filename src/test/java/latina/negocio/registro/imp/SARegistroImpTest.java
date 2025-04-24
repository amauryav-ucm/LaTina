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
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        Query q1 = mock(Query.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(q1);
        when(q1.getResultList()).thenReturn(new ArrayList<>());

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true, false);
        int resultado = sa.ficharEntrada(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx, times(1)).rollback();
        assertEquals(-1, resultado);
    }

    @Test
    void testFicharEntrada_RegistroYaExiste() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        Query q1 = mock(Query.class);
        Query q2 = mock(Query.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        List<Empleado> empleados = new ArrayList<>();
        empleados.add(empleado);

        Registro registroExistente = mock(Registro.class);
        List<Registro> registros = new ArrayList<>();
        registros.add(registroExistente);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(q1);
        when(em.createNamedQuery("Registro.findByEmpleado")).thenReturn(q2);
        when(q1.getResultList()).thenReturn(empleados);
        when(q2.getResultList()).thenReturn(registros);

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true, false);
        int resultado = sa.ficharEntrada(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx, times(1)).rollback();
        assertEquals(-2, resultado);
    }

    @Test
    void testFicharEntrada_Exito() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        Query q1 = mock(Query.class);
        Query q2 = mock(Query.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        List<Empleado> empleados = List.of(empleado);
        List<Registro> registros = new ArrayList<>();

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(q1);
        when(q1.getResultList()).thenReturn(empleados);
        when(em.createNamedQuery("Registro.findByEmpleado")).thenReturn(q2);
        when(q2.getResultList()).thenReturn(registros);

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true, false);
        int resultado = sa.ficharEntrada(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(em, times(1)).persist(any(Registro.class));
        verify(tx, times(1)).commit();
        assertEquals(1, resultado);
    }

    @Test
    void testFicharEntrada_Exception() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        Query q1 = mock(Query.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(q1);
        when(q1.getResultList()).thenThrow(new RuntimeException("DB error"));

        when(tx.isActive()).thenReturn(true);

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true, false);
        int resultado = sa.ficharEntrada(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx, times(1)).rollback();
        assertEquals(-4, resultado);
    }
}
