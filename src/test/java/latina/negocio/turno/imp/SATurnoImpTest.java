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
     * Si idTurno o idEmpleado son <= 0 se hace rollback y se devuelve -4.
     */
    @Test
    void testCamposIncompletos() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // idTurno inválido (0)
        int resultado = sat.asignarTurno(0, 1);
        verify(tx, times(1)).rollback();
        assertEquals(-4, resultado);

        // idEmpleado inválido (0)
        resultado = sat.asignarTurno(1, 0);
        verify(tx, times(2)).rollback(); // Se espera que se haga rollback de nuevo.
        assertEquals(-4, resultado);
    }

    /**
     * Caso: La fecha de inicio del turno es pasada.
     * Se espera rollback y devolución de -1.
     */
    @Test
    void testFechaPasada() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Creamos un Turno con fechaHoraInicio en el pasado (por ejemplo, ayer) y sin asignación.
        Turno turno = new Turno();
        turno.setId(1);
        turno.setFechaHoraInicio(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        turno.setFechaHoraFin(Timestamp.valueOf(LocalDateTime.now().minusDays(1).plusHours(1)));
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Empleado "disponible" (aunque no se evaluará la disponibilidad por la fecha)
        Empleado empleado = new Empleado();
        empleado.setId(1);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        int resultado = sat.asignarTurno(1, 1);
        verify(tx, times(1)).rollback();
        assertEquals(-1, resultado);
    }

    /**
     * Caso: El turno ya se encuentra asignado.
     * Se espera rollback y devolución de -2.
     */
    @Test
    void testTurnoYaAsignado() { //Probablemente lo tenga que borrar
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Creamos un Turno con fecha futura pero que ya tiene asignado un empleado.
        Turno turno = new Turno();
        turno.setId(1);
        turno.setFechaHoraInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        turno.setFechaHoraFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1)));
        // Simulamos turno asignado: el campo empleado es distinto de null.
        Empleado asignado = new Empleado();
        asignado.setId(2);
        turno.setEmpleado(asignado);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Se intenta asignar otro empleado.
        Empleado empleado = new Empleado();
        empleado.setId(1);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        int resultado = sat.asignarTurno(1, 1);
        verify(tx, times(1)).rollback();
        assertEquals(-2, resultado);
    }

    /**
     * Caso: El empleado no está disponible para el turno.
     * Se simula que no existe disponibilidad que cubra el turno.
     * Se espera rollback y devolución de -3.
     */
    @Test
    void testEmpleadoNoDisponible() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SATurnoImp sat = Mockito.spy(new SATurnoImp());
        doReturn(em).when(sat).createEntityManager();

        // Turno válido: fecha futura y sin asignar.
        Turno turno = new Turno();
        turno.setId(1);
        turno.setFechaHoraInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        turno.setFechaHoraFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1)));
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Empleado a asignar.
        Empleado empleado = new Empleado();
        empleado.setId(1);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simulamos que la consulta de disponibilidad (por ejemplo, "Disponibilidad.findByEmpleadoAndFechaInicio")
        // devuelve una lista vacía, es decir, el empleado no tiene disponibilidad para cubrir el turno.
        Query query = mock(Query.class);
        when(query.getResultList()).thenReturn(new ArrayList<Disponibilidad>());
        when(em.createNamedQuery("Disponibilidad.findByEmpleadoAndFechaInicio")).thenReturn(query);

        int resultado = sat.asignarTurno(1, 1);
        verify(tx, times(1)).rollback();
        assertEquals(-3, resultado);
    }

    /**
     * Caso: Error en la persistencia al asignar el turno.
     * Se simula que al persistir se lanza una excepción.
     * Se espera rollback y devolución de -5.
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
        turno.setFechaHoraInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        turno.setFechaHoraFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1)));
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Empleado a asignar.
        Empleado empleado = new Empleado();
        empleado.setId(1);
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simulamos que la consulta de disponibilidad devuelve una lista con al menos una Disponibilidad,
        // lo que indica que el empleado sí está disponible.
        List<Disponibilidad> dispList = new ArrayList<>();
        Disponibilidad disp = new Disponibilidad();
        disp.setId(1);
        disp.setEmpleado(empleado);
        // La disponibilidad cubre el intervalo del turno.
        disp.setFechaInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1).minusMinutes(30)));
        disp.setFechaFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));
        dispList.add(disp);
        Query query = mock(Query.class);
        when(query.getResultList()).thenReturn(dispList);
        when(em.createNamedQuery("Disponibilidad.findByEmpleadoAndFechaInicio")).thenReturn(query);

        // Forzamos que al persistir la asignación se lance una excepción.
        doThrow(new RuntimeException("Error en persistencia")).when(em).persist(any());
        int resultado = sat.asignarTurno(1, 1);
        verify(tx, times(1)).rollback();
        assertEquals(-5, resultado);
    }

    /**
     * Caso: Asignación exitosa.
     * Se dispone de un turno válido, el empleado está disponible (tiene una Disponibilidad que cubre el turno)
     * y la persistencia se realiza con éxito. Se espera commit y devolución de un id > 0.
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
        turno.setFechaHoraInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        turno.setFechaHoraFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1)));
        turno.setEmpleado(null);
        when(em.find(Turno.class, 1)).thenReturn(turno);

        // Empleado a asignar.
        Empleado empleado = new Empleado();
        empleado.setId(1);
        empleado.setActivo(true); // Suponemos que este atributo indica que el empleado puede trabajar.
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        // Simulamos que la consulta de disponibilidad retorna una lista con una Disponibilidad
        // que cubre el intervalo del turno.
        List<Disponibilidad> dispList = new ArrayList<>();
        Disponibilidad disp = new Disponibilidad();
        disp.setId(1);
        disp.setEmpleado(empleado);
        disp.setFechaInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1).minusMinutes(30)));
        disp.setFechaFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));
        dispList.add(disp);
        Query query = mock(Query.class);
        when(query.getResultList()).thenReturn(dispList);
        when(em.createNamedQuery("Disponibilidad.findByEmpleadoAndFechaInicio")).thenReturn(query);

        // Simulamos que al persistir se asigna el empleado al turno.
        doAnswer(invocation -> {
            turno.setEmpleado(empleado);
            return null;
        }).when(em).persist(any());

        int resultado = sat.asignarTurno(1, 1);
        verify(tx, times(1)).commit();
        //Se espera que el meTodo retorne el id del turno asignado (mayor que 0)
        assertTrue(resultado > 0);
    }
}
