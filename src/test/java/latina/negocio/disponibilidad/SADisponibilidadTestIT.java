package latina.negocio.disponibilidad;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.disponibilidad.imp.SADisponibilidadImp;
import latina.negocio.empleado.Empleado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

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

}
