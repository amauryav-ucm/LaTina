package latina.negocio.disponibilidad;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import latina.negocio.disponibilidad.imp.SADisponibilidadImp;
import latina.negocio.empleado.Empleado;
import latina.negocio.turno.Turno;
import latina.negocio.turno.imp.SATurnoImp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class SADisponibilidadTest
{
    @Test
    public void testAltaDisponibilidadEmpleadoNoExiste() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SADisponibilidadImp sad = spy(new SADisponibilidadImp());
        doReturn(em).when(sad).crearEntityManager();

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(1);
        tDisponibilidad.setFechaInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        tDisponibilidad.setFechaFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));

        when(em.find(Empleado.class, 1)).thenReturn(null);

        int resultado = sad.altaDisponibilidad(tDisponibilidad);
        assertEquals(-1, resultado);
        verify(tx, times(1)).rollback();
    }

    @Test
    public void testAltaDisponibilidadFechasInvalidas() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SADisponibilidadImp sad = Mockito.spy(new SADisponibilidadImp());
        doReturn(em).when(sad).crearEntityManager();

        Empleado empleado = new Empleado();
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(1);
        Timestamp fecha = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        tDisponibilidad.setFechaInicio(fecha);
        tDisponibilidad.setFechaFin(fecha);

        int resultado = sad.altaDisponibilidad(tDisponibilidad);
        assertEquals(-2, resultado);
        verify(tx, times(1)).rollback();
    }

    @Test
    public void testAltaDisponibilidadExitosa() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SADisponibilidadImp sad = Mockito.spy(new SADisponibilidadImp());
        doReturn(em).when(sad).crearEntityManager();
        doNothing().when(sad).combinarDisponibilidad(anyInt());

        Empleado empleado = new Empleado();
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(1);
        tDisponibilidad.setFechaInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        tDisponibilidad.setFechaFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));

        // Capturar el objeto que se persiste y asignarle manualmente un ID
        doAnswer(invocation -> {
            Disponibilidad disp = invocation.getArgument(0); // Captura el objeto pasado a persist()
            disp.setId(100); // Simula que JPA le asigna un ID
            return null;
        }).when(em).persist(any(Disponibilidad.class));

        int resultado = sad.altaDisponibilidad(tDisponibilidad);
        assertEquals(100, resultado);
        verify(tx, times(1)).commit();
    }

    @Test
    public void testAltaDisponibilidadTurnoCoincidente(){
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SADisponibilidadImp sad = Mockito.spy(new SADisponibilidadImp());
        doReturn(em).when(sad).crearEntityManager();

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


        Disponibilidad disp = new Disponibilidad();
        disp.setId(1);
        disp.setEmpleado(null);
        when(em.find(Disponibilidad.class, 1)).thenReturn(disp);

        for(int inicio = 15; inicio <= 17; inicio++) for(int fin = 19; fin <= 21; fin++) {
            TDisponibilidad tDisp = new TDisponibilidad(
                    1,
                    Timestamp.valueOf(LocalDateTime.now().plusDays(1)
                            .withHour(inicio).truncatedTo(java.time.temporal.ChronoUnit.HOURS)),
                    Timestamp.valueOf(LocalDateTime.now().plusDays(1)
                            .withHour(fin).truncatedTo(java.time.temporal.ChronoUnit.HOURS))
            );
            int resultado = sad.altaDisponibilidad(tDisp);
            assertEquals(-3, resultado);
        }

        verify(tx, times(9)).rollback();
    }

    @Test
    public void testAltaDisponibilidadHoraActual(){
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SADisponibilidadImp sad = Mockito.spy(new SADisponibilidadImp());
        doReturn(em).when(sad).crearEntityManager();

        Empleado empleado = new Empleado();
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(1);
        Timestamp fechaInicio = Timestamp.valueOf(LocalDateTime.now().withHour(0).truncatedTo(ChronoUnit.HOURS));
        Timestamp fechaFin = Timestamp.valueOf(LocalDateTime.now().withHour(23).truncatedTo(ChronoUnit.HOURS));
        tDisponibilidad.setFechaInicio(fechaInicio);
        tDisponibilidad.setFechaFin(fechaFin);

        int resultado = sad.altaDisponibilidad(tDisponibilidad);
        assertEquals(-4, resultado);
        verify(tx, times(1)).rollback();
    }

    @Test
    public void testAltaDisponibilidad24HorasExcedidas(){
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SADisponibilidadImp sad = Mockito.spy(new SADisponibilidadImp());
        doReturn(em).when(sad).crearEntityManager();

        Empleado empleado = new Empleado();
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(1);
        Timestamp fechaInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        Timestamp fechaFin = Timestamp.valueOf(LocalDateTime.now().plusDays(2).plusMinutes(30));
        tDisponibilidad.setFechaInicio(fechaInicio);
        tDisponibilidad.setFechaFin(fechaFin);

        int resultado = sad.altaDisponibilidad(tDisponibilidad);
        assertEquals(-6, resultado);
        verify(tx, times(1)).rollback();
    }

    @Test
    public void testAltaDisponibilidadPersistenciaFalla() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SADisponibilidadImp sad = Mockito.spy(new SADisponibilidadImp());
        doReturn(em).when(sad).crearEntityManager();
        doNothing().when(sad).combinarDisponibilidad(anyInt());

        Empleado empleado = new Empleado();
        when(em.find(Empleado.class, 1)).thenReturn(empleado);

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(1);
        tDisponibilidad.setFechaInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        tDisponibilidad.setFechaFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));

        // Simular que `persist()` lanza una excepción
        doThrow(new RuntimeException("Error en persistencia")).when(em).persist(any(Disponibilidad.class));

        int resultado = sad.altaDisponibilidad(tDisponibilidad);

        // Verificamos que el método devuelve el código de error correspondiente
        assertEquals(-5, resultado);

        // Verificamos que la transacción se haya iniciado
        verify(tx, times(1)).begin();
        //verify(tx, times(1)).rollback();
    }
}
