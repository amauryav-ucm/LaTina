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

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(Collections.emptyList());

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
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);

        Empleado empleadoMock = mock(Empleado.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleadoMock));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
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
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);

        Empleado empleadoMock = mock(Empleado.class);

        Turno turno = mock(Turno.class);
        // 1) Inicio del turno dentro de 1h ⇒ debe entrar en el if(hora.before(inicioTurno))
        when(turno.getFechaHoraInicio())
                .thenReturn(new Timestamp(System.currentTimeMillis() + 3_600_000)); // +1h
        // 2) Fin del turno también stubeado para evitar NPE al calcular limiteSalida
        when(turno.getFechaHoraFin())
                .thenReturn(new Timestamp(System.currentTimeMillis() + 3_600_000)); // +1h (cualquiera > now)

        Registro registroMock = mock(Registro.class);
        when(registroMock.getTurno()).thenReturn(turno);

        when(em.getTransaction()).thenReturn(tx);

        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class))
                .thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleadoMock));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class))
                .thenReturn(qReg);
        when(qReg.setParameter(eq("empleadoId"), any())).thenReturn(qReg);
        when(qReg.setMaxResults(1)).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(List.of(registroMock));

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        // Act
        int resultado = sa.ficharSalida(
                new TEmpleado("10101010J", "Juan", "Pérez Gómez",
                        "correo@example.com", "123456788", true),
                new Timestamp(System.currentTimeMillis())
        );

        // Assert
        verify(tx).rollback();
        assertEquals(-3, resultado);
    }


    @Test
    void testFicharSalida_Exito() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);

        Empleado empleadoMock = mock(Empleado.class);

        Turno turno = mock(Turno.class);
        Rol rol = mock(Rol.class);
        when(turno.getRol()).thenReturn(rol);
        when(rol.getSalario()).thenReturn(10.0);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(turno.getFechaHoraInicio()).thenReturn(new Timestamp(now.getTime() - 3_600_000)); // -1h
        when(turno.getFechaHoraFin()).thenReturn(new Timestamp(now.getTime() + 3_600_000));   // +1h

        Registro registroMock = mock(Registro.class);
        when(registroMock.getTurno()).thenReturn(turno);
        when(registroMock.gethInicio()).thenReturn(new Timestamp(now.getTime() - 2 * 3_600_000)); // -2h

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleadoMock));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
        when(qReg.setParameter(eq("empleadoId"), any())).thenReturn(qReg);
        when(qReg.setMaxResults(1)).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(List.of(registroMock));

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        int resultado = sa.ficharSalida(
                new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true),
                now
        );

        verify(tx).commit();
        verify(em, never()).merge(any());
        verify(em, never()).persist(any());
        assertEquals(1, resultado);
    }

    @Test
    void testFicharSalida_Exception() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenThrow(new RuntimeException("DB error"));
        when(tx.isActive()).thenReturn(true);

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        TEmpleado tEmp = new TEmpleado("10101010J", "Juan", "Pérez Gómez", "correo@example.com", "123456788", true);
        int resultado = sa.ficharSalida(tEmp, new Timestamp(System.currentTimeMillis()));

        verify(tx).rollback();
        assertEquals(-5, resultado);
    }

    @Test
    void testFicharSalida_DentroDeVentana15Minutos() {
        EntityManager em     = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);

        // 1) Empleado
        Empleado empleadoMock = mock(Empleado.class);

        // 2) Turno + Rol → aquí estaba la excepción
        Turno turno = mock(Turno.class);
        Rol rol     = mock(Rol.class);
        when(rol.getSalario()).thenReturn(10.0);
        when(turno.getRol()).thenReturn(rol);

        // 3) Fechas del turno
        Timestamp now      = new Timestamp(System.currentTimeMillis());
        Timestamp finTurno = new Timestamp(now.getTime() - TimeUnit.HOURS.toMillis(1)); // finis hace 1h
        when(turno.getFechaHoraInicio())
                .thenReturn(new Timestamp(finTurno.getTime() - TimeUnit.HOURS.toMillis(2)));
        when(turno.getFechaHoraFin()).thenReturn(finTurno);

        // 4) Registro asociado
        Registro registroMock = mock(Registro.class);
        when(registroMock.getTurno()).thenReturn(turno);
        when(registroMock.gethInicio())
                .thenReturn(new Timestamp(finTurno.getTime() - TimeUnit.HOURS.toMillis(2)));

        // 5) EntityManager + Queries
        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleadoMock));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
        when(qReg.setParameter(eq("empleadoId"), any())).thenReturn(qReg);
        when(qReg.setMaxResults(1)).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(List.of(registroMock));

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        // 6) Fichamos 14 min 59 s tarde → OK
        Timestamp salida = new Timestamp(
                finTurno.getTime()
                        + TimeUnit.MINUTES.toMillis(14)
                        + TimeUnit.SECONDS.toMillis(59)
        );
        int resultado = sa.ficharSalida(
                new TEmpleado("10101010J","Juan","Pérez","correo@example.com","123456788", true),
                salida
        );

        verify(tx).commit();
        assertEquals(1, resultado);
    }

    @Test
    void testFicharSalida_JustoEnLimite15Minutos() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);

        Empleado empleadoMock = mock(Empleado.class);
        Turno turno = mock(Turno.class);
        Timestamp now     = new Timestamp(System.currentTimeMillis());
        Timestamp finTurno= new Timestamp(now.getTime() - TimeUnit.HOURS.toMillis(1));
        when(turno.getFechaHoraInicio()).thenReturn(new Timestamp(finTurno.getTime() - TimeUnit.HOURS.toMillis(2)));
        when(turno.getFechaHoraFin())   .thenReturn(finTurno);

        Registro registroMock = mock(Registro.class);
        when(registroMock.getTurno()).thenReturn(turno);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleadoMock));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
        when(qReg.setParameter(eq("empleadoId"), any())).thenReturn(qReg);
        when(qReg.setMaxResults(1)).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(List.of(registroMock));

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        // Intentamos fichar salida exactamente 15 min después de finTurno
        Timestamp salida = new Timestamp(finTurno.getTime() + TimeUnit.MINUTES.toMillis(15));
        int resultado = sa.ficharSalida(
                new TEmpleado("10101010J","Juan","Pérez","correo@example.com","123456788", true),
                salida
        );

        verify(tx).rollback();
        assertEquals(-4, resultado);
    }

    @Test
    void testFicharSalida_VentanaMuyAmplia_NoPermite() {
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        TypedQuery<Empleado> qEmp = mock(TypedQuery.class);
        TypedQuery<Registro> qReg = mock(TypedQuery.class);

        Empleado empleadoMock = mock(Empleado.class);
        Turno turno = mock(Turno.class);
        Timestamp now      = new Timestamp(System.currentTimeMillis());
        Timestamp finTurno = new Timestamp(now.getTime() - TimeUnit.HOURS.toMillis(1));
        when(turno.getFechaHoraInicio()).thenReturn(new Timestamp(finTurno.getTime() - TimeUnit.HOURS.toMillis(2)));
        when(turno.getFechaHoraFin())   .thenReturn(finTurno);

        Registro registroMock = mock(Registro.class);
        when(registroMock.getTurno()).thenReturn(turno);

        when(em.getTransaction()).thenReturn(tx);
        when(em.createNamedQuery("Empleado.findByDNI", Empleado.class)).thenReturn(qEmp);
        when(qEmp.setParameter(eq("DNI"), any())).thenReturn(qEmp);
        when(qEmp.getResultList()).thenReturn(List.of(empleadoMock));

        when(em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class)).thenReturn(qReg);
        when(qReg.setParameter(eq("empleadoId"), any())).thenReturn(qReg);
        when(qReg.setMaxResults(1)).thenReturn(qReg);
        when(qReg.getResultList()).thenReturn(List.of(registroMock));

        SARegistroImp sa = spy(new SARegistroImp());
        doReturn(em).when(sa).createEntityManager();

        // Intentamos fichar salida 1 hora tarde
        Timestamp salida = new Timestamp(finTurno.getTime() + TimeUnit.HOURS.toMillis(1));
        int resultado = sa.ficharSalida(
                new TEmpleado("10101010J","Juan","Pérez","correo@example.com","123456788", true),
                salida
        );

        verify(tx).rollback();
        assertEquals(-4, resultado);
    }

    // Helper para invocar el método privado
    private double invokeCalcular(Timestamp llegada, Timestamp salida, Timestamp inicio, Timestamp fin) throws Exception {
        SARegistroImp sa = new SARegistroImp();
        Method m = SARegistroImp.class
                .getDeclaredMethod("calcular_nHoras", Timestamp.class, Timestamp.class, Timestamp.class, Timestamp.class);
        m.setAccessible(true);
        return (Double) m.invoke(sa, llegada, salida, inicio, fin);
    }

    /** 1) Llegada antes del inicio, salida dentro del turno */
    @Test
    void testCalcularNHoras_LlegadaAntesInicio_SalidaEnTurno() throws Exception {
        Timestamp llegada = Timestamp.valueOf("2025-04-29 09:50:00");
        Timestamp inicio  = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp salida  = Timestamp.valueOf("2025-04-29 11:20:00");
        Timestamp fin     = Timestamp.valueOf("2025-04-29 12:00:00");
        // Efectivo: de 10:00 a 11:20 → 1h20 → 4 medias (1h) + resto 20 ≥15 → +1 media = 5 medias → 2.5h
        double horas = invokeCalcular(llegada, salida, inicio, fin);
        assertEquals(1.5, horas, 1e-6);
    }

    /** 2) Llegada dentro, salida después del fin */
    @Test
    void testCalcularNHoras_LlegadaEnTurno_SalidaDespuesFin() throws Exception {
        Timestamp llegada = Timestamp.valueOf("2025-04-29 10:30:00");
        Timestamp inicio  = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp fin     = Timestamp.valueOf("2025-04-29 12:00:00");
        Timestamp salida  = Timestamp.valueOf("2025-04-29 12:10:00");
        // Efectivo: de 10:30 a 12:00 → 1h30 exactas → 3 medias → 1.5h
        double horas = invokeCalcular(llegada, salida, inicio, fin);
        assertEquals(1.5, horas, 1e-6);
    }

    /** 3) Resto justo en el umbral de 15 minutos (exactamente 15m → redondea) */
    @Test
    void testCalcularNHoras_RestoIgual15m() throws Exception {
        Timestamp inicio  = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp llegada = Timestamp.valueOf("2025-04-29 10:00:00");
        // 1h15m después
        Timestamp salida  = new Timestamp(inicio.getTime() + TimeUnit.MINUTES.toMillis(75));
        Timestamp fin     = Timestamp.valueOf("2025-04-29 12:00:00");
        // Efectivo: 1h15 → 2 medias + resto 15 ≥15 → +1 → 3 medias → 1.5h
        double horas = invokeCalcular(llegada, salida, inicio, fin);
        assertEquals(1.5, horas, 1e-6);
    }

    /** 4) Trabajo muy corto (<15m) dentro y fuera del turno → 0h */
    @Test
    void testCalcularNHoras_MenosDe15m() throws Exception {
        Timestamp inicio  = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp llegada = Timestamp.valueOf("2025-04-29 09:55:00"); // antes
        Timestamp fin     = Timestamp.valueOf("2025-04-29 12:00:00");
        // Salida a 10:09:00 (dentro del turno, pero solo 9m de efectivo)
        Timestamp salida  = Timestamp.valueOf("2025-04-29 10:09:00");
        double horas = invokeCalcular(llegada, salida, inicio, fin);
        assertEquals(0.0, horas, 1e-6);
    }


}
