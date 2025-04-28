package latina.negocio.registro.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.registro.Registro;
import latina.negocio.turno.Turno;
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
        TypedQuery<Empleado> q1 = mock(TypedQuery.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(q1);
        when(q1.getResultList()).thenReturn(new ArrayList<>());

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharEntrada(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx, times(1)).rollback();
        assertEquals(-1, resultado);
    }

    @Test
    void testFicharEntrada_RegistroYaExiste() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Empleado> q1 = mock(TypedQuery.class);
        TypedQuery<Registro> q2 = mock(TypedQuery.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        List<Empleado> empleados = new ArrayList<>();
        empleados.add(empleado);

        Registro registroExistente = mock(Registro.class);
        List<Registro> registros = new ArrayList<>();
        registros.add(registroExistente);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(q1);
        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(q2);
        when(q1.getResultList()).thenReturn(empleados);
        when(q2.getResultList()).thenReturn(registros);

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharEntrada(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx, times(1)).rollback();
        assertEquals(-2, resultado);
    }

    @Test
    void testFicharEntrada_Exito() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Empleado> q1 = mock(TypedQuery.class);
        TypedQuery<Registro> q2 = mock(TypedQuery.class);
        TypedQuery<Turno> qTurno = mock(TypedQuery.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        Turno turno = mock(Turno.class);
        when(turno.getFechaHoraInicio()).thenReturn(Timestamp.from(new Timestamp(System.currentTimeMillis()).toInstant().minusSeconds(600)));
        when(turno.getFechaHoraFin()).thenReturn(Timestamp.from(new Timestamp(System.currentTimeMillis()).toInstant().plusSeconds(7200)));

        List<Empleado> empleados = List.of(empleado);
        List<Registro> registros = new ArrayList<>();
        List<Turno> turnos = List.of(turno);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(q1);
        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(q2);
        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(qTurno);
        when(q1.getResultList()).thenReturn(empleados);
        when(q2.getResultList()).thenReturn(registros);
        when(qTurno.getResultList()).thenReturn(turnos);

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharEntrada(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(em, times(1)).persist(any(Registro.class));
        verify(tx, times(1)).commit();
        assertEquals(1, resultado);
    }

    @Test
    void testFicharEntrada_Exception() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Empleado> q1 = mock(TypedQuery.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(q1);
        when(q1.getResultList()).thenThrow(new RuntimeException("DB error"));

        when(tx.isActive()).thenReturn(true);

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharEntrada(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx, times(1)).rollback();
        assertEquals(-4, resultado);
    }

    @Test
    void testFicharEntrada_FueraDeVentana() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);
        TypedQuery<Turno> qTurno = mock(TypedQuery.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(anyString(), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleado));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
        when(qReg.setParameter(anyString(), any())).thenReturn(qReg);
        when(qReg.setMaxResults(anyInt())).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(new ArrayList<>());

        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(qTurno);
        when(qTurno.setParameter(anyString(), any())).thenReturn(qTurno);
        when(qTurno.getResultList()).thenReturn(new ArrayList<>()); // No hay turno que cubra la hora + 15 min

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        Timestamp horaFichaje = Timestamp.valueOf("2025-04-27 18:44:59");
        int resultado = sa.ficharEntrada(
                new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true),
                horaFichaje);

        verify(tx, times(1)).rollback();
        assertEquals(-3, resultado);
    }

    @Test
    void testFicharEntrada_Exactamente15MinutosAntes() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);
        TypedQuery<Turno> qTurno = mock(TypedQuery.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        Turno turno = mock(Turno.class);
        when(turno.getFechaHoraInicio()).thenReturn(Timestamp.valueOf("2025-04-27 19:00:00"));
        when(turno.getFechaHoraFin()).thenReturn(Timestamp.valueOf("2025-04-27 23:00:00"));

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(anyString(), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleado));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
        when(qReg.setParameter(anyString(), any())).thenReturn(qReg);
        when(qReg.setMaxResults(anyInt())).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(new ArrayList<>());

        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(qTurno);
        when(qTurno.setParameter(anyString(), any())).thenReturn(qTurno);
        when(qTurno.getResultList()).thenReturn(List.of(turno)); // Sí hay turno válido

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        Timestamp horaFichaje = Timestamp.valueOf("2025-04-27 18:45:00");
        int resultado = sa.ficharEntrada(
                new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true),
                horaFichaje);

        verify(em, times(1)).persist(any(Registro.class));
        verify(tx, times(1)).commit();
        assertEquals(1, resultado);
    }

    @Test
    void testFicharEntrada_Exactamente15MinutosAntesDeFin() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);
        TypedQuery<Turno> qTurno = mock(TypedQuery.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        Turno turno = mock(Turno.class);
        when(turno.getFechaHoraInicio()).thenReturn(Timestamp.valueOf("2025-04-27 15:00:00"));
        when(turno.getFechaHoraFin()).thenReturn(Timestamp.valueOf("2025-04-27 19:00:00"));

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(anyString(), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleado));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
        when(qReg.setParameter(anyString(), any())).thenReturn(qReg);
        when(qReg.setMaxResults(anyInt())).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(new ArrayList<>());

        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(qTurno);
        when(qTurno.setParameter(anyString(), any())).thenReturn(qTurno);
        when(qTurno.getResultList()).thenReturn(new ArrayList<>()); // No hay turno válido

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        Timestamp horaFichaje = Timestamp.valueOf("2025-04-27 18:45:00");
        int resultado = sa.ficharEntrada(
                new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true),
                horaFichaje);

        verify(tx, times(1)).rollback();
        assertEquals(-3, resultado);
    }

    @Test
    void testFicharEntrada_15MinutosYUnSegundoAntesDeFin() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);
        TypedQuery<Turno> qTurno = mock(TypedQuery.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        Turno turno = mock(Turno.class);
        when(turno.getFechaHoraInicio()).thenReturn(Timestamp.valueOf("2025-04-27 15:00:00"));
        when(turno.getFechaHoraFin()).thenReturn(Timestamp.valueOf("2025-04-27 19:00:00"));

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(anyString(), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleado));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
        when(qReg.setParameter(anyString(), any())).thenReturn(qReg);
        when(qReg.setMaxResults(anyInt())).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(new ArrayList<>());

        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(qTurno);
        when(qTurno.setParameter(anyString(), any())).thenReturn(qTurno);
        when(qTurno.getResultList()).thenReturn(List.of(turno)); // Sí hay turno válido

        SARegistroImp sa = Mockito.spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        Timestamp horaFichaje = Timestamp.valueOf("2025-04-27 18:44:59");
        int resultado = sa.ficharEntrada(
                new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true),
                horaFichaje);

        verify(em, times(1)).persist(any(Registro.class));
        verify(tx, times(1)).commit();
        assertEquals(1, resultado);
    }


}
