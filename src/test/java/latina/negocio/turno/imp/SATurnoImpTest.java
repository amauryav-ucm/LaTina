package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.Turno;
import latina.negocio.empleado.Empleado;
import latina.negocio.disponibilidad.Disponibilidad;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SATurnoImpTest {

    /**
     * Caso: Campos obligatorios incompletos.
     * Si idTurno o idEmpleado son <= 0 (o se produce que find retorne null) se hace rollback y se devuelve -4.
     */
    @Test
    void testCamposIncompletos() {
        // Usamos mocks para simular EntityManager y Transaction.
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Caso: idTurno es 0, lo que simula que find(Turno.class, 0) retorne null.
        when(em.find(Turno.class, 0)).thenReturn(null);
        int resultado = sat.asignarTurno(0, 1);
        verify(tx, times(1)).rollback();
        assertEquals(-4, resultado);

        // Caso: idEmpleado es 0, simula que find(Empleado.class, 0) retorne null.
        when(em.find(Turno.class, 1)).thenReturn(new Turno());
        when(em.find(Empleado.class, 0)).thenReturn(null);
        resultado = sat.asignarTurno(1, 0);
        verify(tx, times(2)).rollback(); // Se espera otro rollback
        assertEquals(-4, resultado);
    }

    /**
     * Caso: Turnos conflictivos.
     * Se simula que el empleado ya tiene un turno asignado que solapa con el turno a asignar,
     * de modo que se retorna -3.
     */
    @Test
    void testTurnosConflictivos() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Creamos un turno a asignar (fecha futura)

        // Empleado a asignar
        Empleado empleado = new Empleado();
        empleado.setId(1);
        // Simulamos que el empleado ya tiene un turno que solapa.
        Turno turnoExistente = new Turno();
        // Supongamos un turno que va de 16 a 20
        turnoExistente.setFechaHoraInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(16).truncatedTo(ChronoUnit.HOURS)));
        turnoExistente.setFechaHoraFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(20).truncatedTo(ChronoUnit.HOURS)));
        List<Turno> listaTurnos = new ArrayList<>();
        listaTurnos.add(turnoExistente);
        empleado.setTurno(listaTurnos);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);


        Turno turno = new Turno();
        turno.setId(1);
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        for(int inicio = 15; inicio <= 17; inicio++) for(int fin = 19; fin <= 21; fin++) {
            turno.setFechaHoraInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(inicio).truncatedTo(ChronoUnit.HOURS)));
            turno.setFechaHoraFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(fin).truncatedTo(ChronoUnit.HOURS)));
            int resultado = sat.asignarTurno(1, 1);
            assertEquals(-3, resultado);
        }

        verify(tx, times(9)).rollback();
    }

    /**
     * Caso: Error en la persistencia.
     * Se simula que al persistir se lanza una excepción.
     * Se espera rollback y devolución de -4.
     */


    @Test
    void testPersistenciaFalla() {
        EntityTransaction tx = mock(EntityTransaction.class);
        when(tx.isActive()).thenReturn(true);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Turno válido: fecha futura y sin asignar.
        Turno turno = new Turno();
        turno.setId(1);
        Timestamp inicioTurno = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        Timestamp finTurno = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1));
        turno.setFechaHoraInicio(inicioTurno);
        turno.setFechaHoraFin(finTurno);
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Empleado válido.
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true);
        // Inicializamos las colecciones para evitar problemas con lazy loading.
        empleado.setTurno(new ArrayList<>());
        ArrayList<Disponibilidad> disponibilidades = new ArrayList<>();
        Disponibilidad disp = new Disponibilidad();
        disp.setFechaHoraInicio(inicioTurno);
        disp.setFechaHoraFin(finTurno);
        disp.setEmpleado(empleado);
        disponibilidades.add(disp);
        empleado.setDisponibilidad(disponibilidades);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simulamos que al persistir el turno (actualización de la asignación) se lanza una excepción,
        // lo que debería provocar rollback y retornar -4.
        doThrow(new RuntimeException("Error en persistencia")).when(em).persist(turno);

        int resultado = sat.asignarTurno(1, 1);
        assertEquals(-5, resultado);
        verify(tx, times(1)).rollback();
    }


    /**
     * Caso: Asignación exitosa.
     * Se dispone de un turno válido, el empleado no tiene turnos conflictivos y la persistencia se realiza correctamente.
     * Se espera commit y devolución de 1.
     */
    @Test
    public void testAsignacionExitosaExacta() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Turno válido: fecha futura y sin asignar.
        Turno turno = new Turno();
        turno.setId(1);
        Timestamp inicioTurno = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        Timestamp finTurno = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1));
        turno.setFechaHoraInicio(inicioTurno);
        turno.setFechaHoraFin(finTurno);
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        Query removeQuery = mock(Query.class);
        when(em.createNamedQuery("Disponibilidad.delete")).thenReturn(removeQuery);


        // Empleado válido.
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true);
        // Inicializamos las colecciones para evitar problemas con lazy loading.
        empleado.setTurno(new ArrayList<>());
        // Creamos una lista de disponibilidades que cubra el turno.
        ArrayList<Disponibilidad> disponibilidades = new ArrayList<>();
        Disponibilidad disp = new Disponibilidad();
        // Suponemos que la disponibilidad comienza a la hora de inicio y termina a la hora de fin del turno.
        disp.setFechaHoraInicio(inicioTurno);
        disp.setFechaHoraFin(finTurno);
        disp.setEmpleado(empleado);
        disponibilidades.add(disp);
        empleado.setDisponibilidad(disponibilidades);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simulamos que al persistir se asigna el empleado al turno.
        doAnswer(invocation -> {
            turno.setEmpleado(empleado);
            return null;
        }).when(em).persist(any());

        int resultado = sat.asignarTurno(1, 1);
        assertEquals(1, resultado);
        verify(tx, times(1)).commit();
    }

    @Test
    public void testAsignacionExitosaIzq() {
        // Disponibilidad: de 10:00 a 13:00
        // Turno: de 11:00 a 13:00
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Crear turno: de 11:00 a 13:00
        Turno turno = new Turno();
        turno.setId(1);
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(11).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(13).truncatedTo(ChronoUnit.HOURS));
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Crear empleado con disponibilidad de 10:00 a 13:00
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true);
        empleado.setTurno(new ArrayList<>());
        ArrayList<Disponibilidad> disponibilidades = new ArrayList<>();
        Disponibilidad disp = new Disponibilidad();
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(13).truncatedTo(ChronoUnit.HOURS));
        disp.setFechaHoraInicio(dispInicio);
        disp.setFechaHoraFin(dispFin);
        disp.setEmpleado(empleado);
        disponibilidades.add(disp);
        empleado.setDisponibilidad(disponibilidades);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simular persist del turno (se asigna el empleado) y de la disponibilidad (caso 3: recortar la parte final)
        doAnswer(invocation -> {
            turno.setEmpleado(empleado);
            return null;
        }).when(em).persist(turno);

        int resultado = sat.asignarTurno(1, 1);
        assertEquals(1, resultado);
        verify(tx, times(1)).commit();
    }

    @Test
    public void testAsignacionExitosaDcha() {
        // Disponibilidad: de 10:00 a 13:00
        // Turno: de 10:00 a 12:00
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Crear turno: de 10:00 a 12:00
        Turno turno = new Turno();
        turno.setId(1);
        LocalDateTime a = LocalDateTime.now();
        LocalDateTime b = a.plusDays(1);
        LocalDateTime c = b.withHour(10);
        LocalDateTime d = c.truncatedTo(ChronoUnit.HOURS);
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Crear empleado con disponibilidad de 10:00 a 13:00
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true);
        empleado.setTurno(new ArrayList<>());
        ArrayList<Disponibilidad> disponibilidades = new ArrayList<>();
        Disponibilidad disp = new Disponibilidad();
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(13).truncatedTo(ChronoUnit.HOURS));
        disp.setFechaHoraInicio(dispInicio);
        disp.setFechaHoraFin(dispFin);
        disp.setEmpleado(empleado);
        disponibilidades.add(disp);
        empleado.setDisponibilidad(disponibilidades);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simular persist (Caso 2: recortar la parte inicial, es decir, se modifica la disponibilidad para que inicie en turnoFin)
        doAnswer(invocation -> {
            turno.setEmpleado(empleado);
            return null;
        }).when(em).persist(turno);

        int resultado = sat.asignarTurno(1, 1);
        assertEquals(1, resultado);
        verify(tx, times(1)).commit();
    }

    @Test
    public void testAsignacionExitosaEntreMedias() {
        // Disponibilidad: de 9:00 a 13:00
        // Turno: de 10:00 a 12:00
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Crear turno: de 10:00 a 12:00
        Turno turno = new Turno();
        turno.setId(1);
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Crear empleado con disponibilidad de 9:00 a 13:00
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true);
        empleado.setTurno(new ArrayList<>());
        ArrayList<Disponibilidad> disponibilidades = new ArrayList<>();
        Disponibilidad disp = new Disponibilidad();
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(9).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(13).truncatedTo(ChronoUnit.HOURS));
        disp.setFechaHoraInicio(dispInicio);
        disp.setFechaHoraFin(dispFin);
        disp.setEmpleado(empleado);
        disponibilidades.add(disp);
        empleado.setDisponibilidad(disponibilidades);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simular persist (Caso 4: dividir la disponibilidad en dos partes)
        doAnswer(invocation -> {
            turno.setEmpleado(empleado);
            return null;
        }).when(em).persist(turno);

        int resultado = sat.asignarTurno(1, 1);
        assertEquals(1, resultado);
        verify(tx, times(1)).commit();
    }


    @Test
    public void asignacionTurnoEmpleadoNoExiste() {
        // Simulamos EntityManager y Transaction
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        // Creamos un spy del SATurnoImp para inyectar nuestro EntityManager mockeado
        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Simulamos que el turno existe
        Turno turno = new Turno();
        turno.setId(1);
        Timestamp inicioTurno = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        Timestamp finTurno = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1));
        turno.setFechaHoraInicio(inicioTurno);
        turno.setFechaHoraFin(finTurno);
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Simulamos que el empleado NO existe
        when(em.find(Empleado.class, 1)).thenReturn(null);

        // Llamamos al SA y verificamos que retorne -4 y se haga rollback
        int resultado = sat.asignarTurno(1, 1);
        assertEquals(-4, resultado);
        verify(tx, times(1)).rollback();
    }

    @Test
    public void asignacionTurnoTurnoNoExiste() {
        // Simulamos EntityManager y Transaction
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        // Creamos un spy del SATurnoImp para inyectar nuestro EntityManager mockeado
        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Simulamos que el turno NO existe
        when(em.find(Turno.class, 1)).thenReturn(null);

        // Simulamos que el empleado existe
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true);
        // Inicializamos las colecciones para evitar problemas de lazy loading en test.
        empleado.setTurno(new ArrayList<>());
        empleado.setDisponibilidad(new ArrayList<>());
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Llamamos al SA y verificamos que retorne -4 y se haga rollback
        int resultado = sat.asignarTurno(1, 1);
        assertEquals(-4, resultado);
        verify(tx, times(1)).rollback();
    }

    @Test
    public void testGetTurnosSemanaConTurnos() {
        EntityManager em = mock(EntityManager.class);
        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Simular valores de entrada
        Timestamp semana = Timestamp.valueOf(LocalDateTime.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));

        List<Turno> turnosSimulados = new ArrayList<>();
        turnosSimulados.add(new Turno());
        turnosSimulados.add(new Turno());

        TypedQuery<Turno> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(turnosSimulados);

        List<Turno> resultado = sat.getTurnosSemana(semana);
        assertEquals(2, resultado.size());
    }

    @Test
    public void testGetTurnosSemanaSinTurnos() {
        EntityManager em = mock(EntityManager.class);
        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        Timestamp semana = Timestamp.valueOf(LocalDateTime.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));

        TypedQuery<Turno> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Turno.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<>());

        List<Turno> resultado = sat.getTurnosSemana(semana);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testGetTurnosSemanaError() {
        EntityManager em = mock(EntityManager.class);
        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        Timestamp semana = Timestamp.valueOf(LocalDateTime.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));

        when(em.createQuery(anyString(), eq(Turno.class))).thenThrow(new RuntimeException("Database error"));

        List<Turno> resultado = sat.getTurnosSemana(semana);
        assertNull(resultado);
    }


}
