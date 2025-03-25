package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.negocio.turno.Turno;
import latina.negocio.empleado.Empleado;
import latina.negocio.dispoinibilidad.Disponibilidad;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        Turno turno = new Turno();
        turno.setId(1);
        Timestamp inicioTurno = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        Timestamp finTurno = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1));
        turno.setFechaHoraInicio(inicioTurno);
        turno.setFechaHoraFin(finTurno);
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Empleado a asignar
        Empleado empleado = new Empleado();
        empleado.setId(1);
        // Simulamos que el empleado ya tiene un turno que solapa.
        Turno turnoExistente = new Turno();
        // Supongamos un turno que va de inicioTurno - 10 minutos a finTurno + 10 minutos.
        turnoExistente.setFechaHoraInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1).minusMinutes(10)));
        turnoExistente.setFechaHoraFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1).plusMinutes(10)));
        List<Turno> listaTurnos = new ArrayList<>();
        listaTurnos.add(turnoExistente);
        empleado.setTurno(listaTurnos);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        int resultado = sat.asignarTurno(1, 1);
        verify(tx, times(1)).rollback();
        assertEquals(-3, resultado);
    }

    /**
     * Caso: Error en la persistencia.
     * Se simula que al persistir se lanza una excepción.
     * Se espera rollback y devolución de -4.
     */
    @Test
    void testPersistenciaFalla() {
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

        // Empleado válido.
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true);
        // Simular que el empleado no tiene turnos conflictivos.
        empleado.setTurno(new ArrayList<Turno>());
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simulamos que al persistir (asignar el turno) se lanza una excepción.
        doThrow(new RuntimeException("Error en persistencia")).when(em).persist(any());

        int resultado = sat.asignarTurno(1, 1);
        verify(tx, times(1)).rollback();
        assertEquals(-4, resultado);
    }

    /**
     * Caso: Asignación exitosa.
     * Se dispone de un turno válido, el empleado no tiene turnos conflictivos y la persistencia se realiza correctamente.
     * Se espera commit y devolución de 1.
     */
    @Test
    void testAsignacionExitosa() {
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

        // Empleado válido.
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true);
        // El empleado no tiene turnos conflictivos.
        empleado.setTurno(new ArrayList<>());
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simulamos que al persistir se asigna el empleado al turno.
        doAnswer(invocation -> {
            turno.setEmpleado(empleado);
            return null;
        }).when(em).persist(any());

        int resultado = sat.asignarTurno(1, 1);
        verify(tx, times(1)).commit();
        assertEquals(1, resultado);
    }
}
