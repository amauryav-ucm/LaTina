package latina.negocio.disponibilidad;

import jakarta.persistence.EntityManager;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.disponibilidad.imp.SADisponibilidadImp;
import latina.negocio.empleado.Empleado;
import latina.negocio.rol.Rol;
import latina.negocio.turno.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SADisponibilidadTestIT {

    private SADisponibilidad sa;
    private Empleado empleado; // Empleado global para reutilizar

    @BeforeEach
    public void setUp() {
        try {
            Field instancia = EMFContainer.class.getDeclaredField("emfc");
            instancia.setAccessible(true);
            instancia.set(null, new EMFContainerImpTest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        sa = new SADisponibilidadImp();


        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();

        empleado = new Empleado();
        empleado.setNombre("Juan");
        empleado.setApellidos("Pérez");
        empleado.setDNI("12345678A"); // 🔹 Agrega un DNI válido
        empleado.setCorreo("juan.perez@email.com");
        empleado.setTelefono("666777888");
        empleado.setActivo(true);

        em.persist(empleado);
        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void testAltaDisponibilidadExitosa() {

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(empleado.getId());
        tDisponibilidad.setFechaInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        tDisponibilidad.setFechaFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));

        int id = sa.altaDisponibilidad(tDisponibilidad);

        assertTrue(id > 0);


        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        Disponibilidad disponibilidad = em.find(Disponibilidad.class, id);
        assertNotNull(disponibilidad);
        assertEquals(empleado.getId(), disponibilidad.getEmpleado().getId());

        em.close();
    }

    @Test
    public void testAltaDisponibilidadEmpleadoNoExiste() {
        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(9999); // ID de empleado que no existe
        tDisponibilidad.setFechaInicio(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        tDisponibilidad.setFechaFin(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));

        int resultado = sa.altaDisponibilidad(tDisponibilidad);
        assertEquals(-1, resultado);
    }

    @Test
    public void testAltaDisponibilidadFechasInvalidas() {

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(empleado.getId());
        Timestamp fecha = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        tDisponibilidad.setFechaInicio(fecha);
        tDisponibilidad.setFechaFin(fecha);

        int resultado = sa.altaDisponibilidad(tDisponibilidad);
        assertEquals(-2, resultado);
    }

    @Test
    public void testAltaDisponibilidadTurnoCoincidente(){
        // Persistir empleado y un Rol.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROLTEST3");
        rol.setSalario(10);
        rol.setActivo(true);
        em.persist(rol);
        em.getTransaction().commit();
        em.close();

        // Crear dos turnos: uno ya asignado (de 10:00 a 12:00) y otro nuevo que solapa (de 11:00 a 13:00).
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        Turno turno = new Turno();
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(empleado); // Ya asignado al empleado.
        turno.setRol(rol);
        em.persist(turno);
        // Nueva disp conflictivo: de 11:00 a 13:00.
        TDisponibilidad tDisp = new TDisponibilidad(
                empleado.getId(),
                Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(11).truncatedTo(ChronoUnit.HOURS)),
                Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(13).truncatedTo(ChronoUnit.HOURS))
        );
        em.getTransaction().commit();
        em.close();

        int result = sa.altaDisponibilidad(tDisp);
        assertEquals(-3, result);
    }
    @Test
    public void testAltaDisponibilidadHoraPasada() {

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(empleado.getId());

        Timestamp fechaPasada = Timestamp.valueOf(LocalDateTime.now().minusHours(1)); // Hace 1 hora
        Timestamp fechaFin = Timestamp.valueOf(LocalDateTime.now().plusHours(2)); // Dentro de 2 horas

        tDisponibilidad.setFechaInicio(fechaPasada);
        tDisponibilidad.setFechaFin(fechaFin);


        int resultado = sa.altaDisponibilidad(tDisponibilidad);

        assertEquals(-4, resultado);
    }

    @Test
    public void testAltaDisponibilidad24HorasExcedidas() {

        TDisponibilidad tDisponibilidad = new TDisponibilidad();
        tDisponibilidad.setEmpleadoId(empleado.getId());

        Timestamp fechaInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
        Timestamp fechaFin = Timestamp.valueOf(LocalDateTime.now().plusDays(2).plusMinutes(30));

        tDisponibilidad.setFechaInicio(fechaInicio);
        tDisponibilidad.setFechaFin(fechaFin);


        int resultado = sa.altaDisponibilidad(tDisponibilidad);

        assertEquals(-6, resultado);
    }

}
