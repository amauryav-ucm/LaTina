package latina.negocio.registro.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.registro.Registro;
import latina.negocio.rol.Rol;
import latina.negocio.turno.Turno;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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

    @Test
    void testFicharSalida_EmpleadoNoExiste() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        Query q = mock(Query.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(q);
        when(q.setParameter(eq("DNI"), any())).thenReturn(q);
        when(q.getResultList()).thenReturn(Collections.emptyList());

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharSalida(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx).rollback();
        assertEquals(-1, resultado);
    }

    @Test
    void testFicharSalida_NoEntradaActiva() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        Query qEmp = mock(Query.class);
        Query qReg = mock(Query.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleado));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado")).thenReturn(qReg);
        when(qReg.setParameter(eq("empleadoId"), any())).thenReturn(qReg);
        when(qReg.setMaxResults(1)).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(Collections.emptyList());

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharSalida(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx).rollback();
        assertEquals(-2, resultado);
    }

    @Test
    void testFicharSalida_HoraAntesDeInicio() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        Query qEmp = mock(Query.class);
        Query qReg = mock(Query.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        Turno turno = mock(Turno.class);
        when(turno.getFechaHoraInicio()).thenReturn(new Timestamp(System.currentTimeMillis() + 3600_000)); // Turno empieza en 1h

        Registro registro = mock(Registro.class);
        when(registro.getTurno()).thenReturn(turno);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleado));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado")).thenReturn(qReg);
        when(qReg.setParameter(eq("empleadoId"), any())).thenReturn(qReg);
        when(qReg.setMaxResults(1)).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(List.of(registro));

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharSalida(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx).rollback();
        assertEquals(-3, resultado);
    }

    @Test
    void testFicharSalida_Exito() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        Query qEmp = mock(Query.class);
        Query qReg = mock(Query.class);

        Empleado empleado = mock(Empleado.class);
        when(empleado.getId()).thenReturn(1);

        Turno turno = mock(Turno.class);
        Rol rol = mock(Rol.class);
        when(turno.getFechaHoraInicio()).thenReturn(new Timestamp(System.currentTimeMillis() - 3_600_000)); // hace 1h
        when(turno.getRol()).thenReturn(rol);
        when(rol.getSalario()).thenReturn(10.0); // salario base

        Registro registro = mock(Registro.class);
        when(registro.getTurno()).thenReturn(turno);
        when(registro.gethInicio()).thenReturn(new Timestamp(System.currentTimeMillis() - 2 * 3_600_000)); // hace 2h

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleado));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado")).thenReturn(qReg);
        when(qReg.setParameter(eq("empleadoId"), any())).thenReturn(qReg);
        when(qReg.setMaxResults(1)).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(List.of(registro));

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        Timestamp horaFin = new Timestamp(System.currentTimeMillis());
        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharSalida(tEmp, horaFin);

        verify(em).merge(registro);
        verify(tx).commit();
        assertEquals(1, resultado);
    }

    @Test
    void testFicharSalida_Exception() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        Query q = mock(Query.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI")).thenReturn(q);
        when(q.setParameter(eq("DNI"), any())).thenReturn(q);
        when(q.getResultList()).thenThrow(new RuntimeException("DB error"));

        when(tx.isActive()).thenReturn(true);

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharSalida(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx).rollback();
        assertEquals(-4, resultado);
    }

}
