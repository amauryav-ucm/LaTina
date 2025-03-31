package latina.negocio.disponibilidad;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import latina.negocio.disponibilidad.imp.SADisponibilidadImp;
import latina.negocio.empleado.Empleado;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;    


public class SADisponibilidadTest
{
    @Test
    void testAltaDisponibilidadEmpleadoNoExiste() {
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
    void testAltaDisponibilidadFechasInvalidas() {
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
    void testAltaDisponibilidadExitosa() {
        EntityTransaction tx = mock(EntityTransaction.class);
        EntityManager em = mock(EntityManager.class);
        when(em.getTransaction()).thenReturn(tx);

        SADisponibilidadImp sad = Mockito.spy(new SADisponibilidadImp());
        doReturn(em).when(sad).crearEntityManager();

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

}
