package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurnoRolEmpleado;
import latina.negocio.turno.Turno;
import latina.negocio.rol.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SATurnoImpTestIT {
    private SATurno sa;

    @BeforeEach
    public void setUp() {
        sa = SAFactory.getInstance().createSATurno();
        limpiarBaseDeDatos();
    }

    private void limpiarBaseDeDatos() {
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        // Limpiar tablas (orden respetando FK)
        em.createQuery("DELETE FROM Turno").executeUpdate();
        em.createQuery("DELETE FROM Rol").executeUpdate();

        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void getTurnoSemanalExitoso() {

        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();

        // Crear Rol
        Rol rol = new Rol();
        rol.setNombre("ROL_TEST");
        rol.setSalario(150.0);
        em.persist(rol);


        // Turno 1 dentro de la semana
        Turno t1 = new Turno();
        t1.setRol(rol);
        t1.setFechaHoraInicio(Timestamp.valueOf("2025-03-26 18:00:00"));
        t1.setFechaHoraFin   (Timestamp.valueOf("2025-03-27 04:00:00"));
        em.persist(t1);

        // Turno 2 dentro de la semana
        Turno t2 = new Turno();
        t2.setRol(rol);
        t2.setFechaHoraInicio(Timestamp.valueOf("2025-03-28 09:00:00"));
        t2.setFechaHoraFin   (Timestamp.valueOf("2025-03-28 17:00:00"));
        em.persist(t2);

        // Turno 3 fuera de la semana (02-Abr)
        Turno t3 = new Turno();
        t3.setRol(rol);
        t3.setFechaHoraInicio(Timestamp.valueOf("2025-03-10 03:00:00"));
        t3.setFechaHoraFin   (Timestamp.valueOf("2025-03-10 10:00:00"));
        em.persist(t3);

        // Turno 2 dentro de la semana
        Turno t4 = new Turno();
        t4.setRol(rol);
        t4.setFechaHoraInicio(Timestamp.valueOf("2025-03-30 23:30:00"));
        t4.setFechaHoraFin   (Timestamp.valueOf("2025-03-31 10:00:00"));
        em.persist(t4);

        em.getTransaction().commit();
        em.close();

        Timestamp semanaEspecifica = Timestamp.valueOf("2025-03-25 10:30:00");

        List<TTurnoRolEmpleado> lista = sa.getTurnosSemana(semanaEspecifica);
        if(lista.isEmpty()){
            System.out.println("ERROR");
        }
        // 4. Validar resultados: deben ser exactamente los 2 de la semana
        assertEquals(3, lista.size(), "Se deben devolver solo los 3 turnos de la semana");
        assertTrue(lista.stream().anyMatch(x -> x.getIdTurno() == t1.getId()),
                "Debe contener el turno ID=" + t1.getId());
        assertTrue(lista.stream().anyMatch(x -> x.getIdTurno() == t2.getId()),
                "Debe contener el turno ID=" + t2.getId());
        assertFalse(lista.stream().anyMatch(x -> x.getIdTurno() == t3.getId()),
                "No debe contener el turno ID=" + t3.getId());
        assertTrue(lista.stream().anyMatch(x -> x.getIdTurno() == t4.getId()),
                "Debe contener el turno ID=" + t4.getId());
    }

    @Test
    public void getTurnoSemanalError() {

        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();

        // Crear Rol
        Rol rol = new Rol();
        rol.setNombre("ROL_TEST");
        rol.setSalario(150.0);
        em.persist(rol);

        // Turno 3 fuera de la semana (02-Abr)
        Turno t3 = new Turno();
        t3.setRol(rol);
        t3.setFechaHoraInicio(Timestamp.valueOf("2025-03-08 23:59:59"));
        t3.setFechaHoraFin   (Timestamp.valueOf("2025-03-09 10:00:00"));
        em.persist(t3);

        em.getTransaction().commit();
        em.close();

        Timestamp semanaEspecifica = Timestamp.valueOf("2025-03-11 10:30:00");

        List<TTurnoRolEmpleado> lista = sa.getTurnosSemana(semanaEspecifica);
        if(lista.isEmpty()){
            System.out.println("BIEN");
        }
        //Exito
        assertTrue(lista.isEmpty());
    }



}
