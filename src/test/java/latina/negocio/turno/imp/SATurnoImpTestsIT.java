package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.dispoinibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.Rol;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SATurnoImpTestsIT {

    private SATurno sa;

    @BeforeEach
    public void setUp() {
        sa = SAFactory.getInstance().createSATurno();
        limpiarBaseDeDatos();
    }

    /**
     * Se borran todas las entidades involucradas.
     */
    private void limpiarBaseDeDatos() {
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        // Primero borramos Turno (porque referencia a Empleado)
        em.createQuery("DELETE FROM Turno").executeUpdate();
        // Luego Disponibilidad y Empleado
        em.createQuery("DELETE FROM Disponibilidad").executeUpdate();
        em.createQuery("DELETE FROM Empleado").executeUpdate();
        // Opcionalmente, borrar Rol si lo necesitamos
        em.createQuery("DELETE FROM Rol").executeUpdate();
        tx.commit();
        em.close();
    }

    /**
     * Escenario 1: Asignación exitosa.
     * Se crea un empleado con disponibilidad que cubre el turno (por ejemplo, turno de 10:00 a 12:00 y disponibilidad de 09:00 a 13:00).
     * Se persiste un turno sin asignar y se comprueba que tras asignar, el turno queda asociado al empleado y se retorna 1.
     */
    @Test
    public void asignacionTurnoExitosaDispExacta() {
        // 1. Crear y persistir un empleado.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("12345678A");  // Asignamos DNI
        emp.setNombre("Empleado1");
        emp.setCorreo("emp1@test.com");
        emp.setActivo(true);
        em.persist(emp);
        em.getTransaction().commit();
        em.close();

        // 2. Crear y persistir una Disponibilidad que cubra el turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        // Suponemos turno mañana de 10:00 a 12:00.
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        // Disponibilidad de 10:00 a 12:00.
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        Disponibilidad disp = new Disponibilidad(emp, dispInicio, dispFin);
        em.persist(disp);
        em.getTransaction().commit();
        em.close();

        // 3. Crear y persistir un Rol (necesario para el Turno) y el Turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROLTEST");
        rol.setSalario(10);
        rol.setActivo(true);
        em.persist(rol);

        Turno turno = new Turno();
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        turno.setRol(rol);
        em.persist(turno);
        em.getTransaction().commit();
        int turnoId = turno.getId();
        int empId = emp.getId();
        em.close();

        // 4. Llamar al SA para asignar el turno.
        int result = sa.asignarTurno(turnoId, empId);
        assertEquals(1, result);

        // 5. Verificar en la BD que el turno quedó asignado al empleado.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        Turno turnoAsignado = em.find(Turno.class, turnoId);
        assertNotNull(turnoAsignado.getEmpleado());
        assertEquals(empId, turnoAsignado.getEmpleado().getId());
        em.close();
    }

    @Test
    public void asignacionTurnoExitosaDispIzq() {
        // 1. Crear y persistir un empleado.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("12345678A");  // Asignamos DNI
        emp.setNombre("Empleado1");
        emp.setCorreo("emp1@test.com");
        emp.setActivo(true);
        em.persist(emp);
        em.getTransaction().commit();
        em.close();

        // 2. Crear y persistir una Disponibilidad que cubra el turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        // Suponemos turno mañana de 10:00 a 12:00.
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        // Disponibilidad de 10:00 a 14:00.
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(14).truncatedTo(ChronoUnit.HOURS));
        Disponibilidad disp = new Disponibilidad(emp, dispInicio, dispFin);
        em.persist(disp);
        em.getTransaction().commit();
        em.close();

        // 3. Crear y persistir un Rol (necesario para el Turno) y el Turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROLTEST");
        rol.setSalario(10);
        rol.setActivo(true);
        em.persist(rol);

        Turno turno = new Turno();
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        turno.setRol(rol);
        em.persist(turno);
        em.getTransaction().commit();
        int turnoId = turno.getId();
        int empId = emp.getId();
        em.close();

        // 4. Llamar al SA para asignar el turno.
        int result = sa.asignarTurno(turnoId, empId);
        assertEquals(1, result);

        // 5. Verificar en la BD que el turno quedó asignado al empleado.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        Turno turnoAsignado = em.find(Turno.class, turnoId);
        assertNotNull(turnoAsignado.getEmpleado());
        assertEquals(empId, turnoAsignado.getEmpleado().getId());
        em.close();
    }

    @Test
    public void asignacionTurnoExitosaDispDcha() {
        // 1. Crear y persistir un empleado.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("12345678A");  // Asignamos DNI
        emp.setNombre("Empleado1");
        emp.setCorreo("emp1@test.com");
        emp.setActivo(true);
        em.persist(emp);
        em.getTransaction().commit();
        em.close();

        // 2. Crear y persistir una Disponibilidad que cubra el turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        // Suponemos turno mañana de 10:00 a 12:00.
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        // Disponibilidad de 8:00 a 12:00.
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(8).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        Disponibilidad disp = new Disponibilidad(emp, dispInicio, dispFin);
        em.persist(disp);
        em.getTransaction().commit();
        em.close();

        // 3. Crear y persistir un Rol (necesario para el Turno) y el Turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROLTEST");
        rol.setSalario(10);
        rol.setActivo(true);
        em.persist(rol);

        Turno turno = new Turno();
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        turno.setRol(rol);
        em.persist(turno);
        em.getTransaction().commit();
        int turnoId = turno.getId();
        int empId = emp.getId();
        em.close();

        // 4. Llamar al SA para asignar el turno.
        int result = sa.asignarTurno(turnoId, empId);
        assertEquals(1, result);

        // 5. Verificar en la BD que el turno quedó asignado al empleado.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        Turno turnoAsignado = em.find(Turno.class, turnoId);
        assertNotNull(turnoAsignado.getEmpleado());
        assertEquals(empId, turnoAsignado.getEmpleado().getId());
        em.close();
    }

    @Test
    public void asignacionTurnoExitosaDispEntreMedias() {
        // 1. Crear y persistir un empleado.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("12345678A");  // Asignamos DNI
        emp.setNombre("Empleado1");
        emp.setCorreo("emp1@test.com");
        emp.setActivo(true);
        em.persist(emp);
        em.getTransaction().commit();
        em.close();

        // 2. Crear y persistir una Disponibilidad que cubra el turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        // Suponemos turno mañana de 10:00 a 12:00.
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        // Disponibilidad de 8:00 a 14:00.
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(8).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(14).truncatedTo(ChronoUnit.HOURS));
        Disponibilidad disp = new Disponibilidad(emp, dispInicio, dispFin);
        em.persist(disp);
        em.getTransaction().commit();
        em.close();

        // 3. Crear y persistir un Rol (necesario para el Turno) y el Turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROLTEST");
        rol.setSalario(10);
        rol.setActivo(true);
        em.persist(rol);

        Turno turno = new Turno();
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        turno.setRol(rol);
        em.persist(turno);
        em.getTransaction().commit();
        int turnoId = turno.getId();
        int empId = emp.getId();
        em.close();

        // 4. Llamar al SA para asignar el turno.
        int result = sa.asignarTurno(turnoId, empId);
        assertEquals(1, result);

        // 5. Verificar en la BD que el turno quedó asignado al empleado.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        Turno turnoAsignado = em.find(Turno.class, turnoId);
        assertNotNull(turnoAsignado.getEmpleado());
        assertEquals(empId, turnoAsignado.getEmpleado().getId());
        em.close();
    }

    /**
     * Escenario 2: Disponibilidad insuficiente.
     * Se crea un empleado cuya disponibilidad no cubre el turno (por ejemplo, disponibilidad de 09:00 a 11:00 para un turno de 10:00 a 12:00).
     * Se espera que se retorne -2.
     */
    @Test
    public void asignacionTurnoDisponibilidadInsuficiente() {
        // Crear y persistir empleado.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("22345678B");
        emp.setNombre("Empleado2");
        emp.setCorreo("emp2@test.com");
        emp.setActivo(true);
        em.persist(emp);
        em.getTransaction().commit();
        em.close();

        // Crear y persistir Disponibilidad que NO cubre el turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        // Turno de 10:00 a 12:00.
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        // Disponibilidad de 09:00 a 11:00 (no alcanza a cubrir hasta 12:00).
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(9).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(11).truncatedTo(ChronoUnit.HOURS));
        Disponibilidad disp = new Disponibilidad(emp, dispInicio, dispFin);
        em.persist(disp);
        em.getTransaction().commit();
        em.close();

        // Crear y persistir un Rol y un Turno.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROLTEST2");
        rol.setSalario(10);
        rol.setActivo(true);
        em.persist(rol);
        Turno turno = new Turno();
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        turno.setRol(rol);
        em.persist(turno);
        em.getTransaction().commit();
        int turnoId = turno.getId();
        int empId = emp.getId();
        em.close();

        int result = sa.asignarTurno(turnoId, empId);
        assertEquals(-2, result);
    }

    /**
     * Escenario 3: Turno conflictivo (solapamiento con otro turno asignado).
     * Se crea un empleado que ya tiene un turno asignado que choca con el nuevo turno.
     * Se espera que se retorne -3.
     */
    @Test
    public void asignacionTurnoTurnosConflictivos() {
        // Persistir empleado y un Rol.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("32345678C");
        emp.setNombre("Empleado3");
        emp.setCorreo("emp3@test.com");
        emp.setActivo(true);
        em.persist(emp);
        Rol rol = new Rol();
        rol.setNombre("ROLTEST3");
        rol.setSalario(10);
        rol.setActivo(true);
        em.persist(rol);
        em.getTransaction().commit();
        em.close();

        // Crear y persistir una Disponibilidad amplia.
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Timestamp dispInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(8).truncatedTo(ChronoUnit.HOURS));
        Timestamp dispFin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(18).truncatedTo(ChronoUnit.HOURS));
        Disponibilidad disp = new Disponibilidad(emp, dispInicio, dispFin);
        em.persist(disp);
        em.getTransaction().commit();
        em.close();

        // Crear dos turnos: uno ya asignado (de 10:00 a 12:00) y otro nuevo que solapa (de 11:00 a 13:00).
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Timestamp turno1Inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turno1Fin   = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        Turno turno1 = new Turno();
        turno1.setFechaHoraInicio(turno1Inicio);
        turno1.setFechaHoraFin(turno1Fin);
        turno1.setEmpleado(emp); // Ya asignado al empleado.
        turno1.setRol(rol);
        em.persist(turno1);
        // Nuevo turno conflictivo: de 11:00 a 13:00.
        Timestamp turno2Inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(11).truncatedTo(ChronoUnit.HOURS));
        Timestamp turno2Fin   = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(13).truncatedTo(ChronoUnit.HOURS));
        Turno turno2 = new Turno();
        turno2.setFechaHoraInicio(turno2Inicio);
        turno2.setFechaHoraFin(turno2Fin);
        turno2.setEmpleado(null);
        turno2.setRol(rol);
        em.persist(turno2);
        em.getTransaction().commit();
        int turno2Id = turno2.getId();
        int empId = emp.getId();
        em.close();

        int result = sa.asignarTurno(turno2Id, empId);
        assertEquals(-3, result);
    }

    /**
     * Escenario 4: Turno inexistente.
     * Se intenta asignar un turno con un id que no existe, lo que debe provocar una excepción y retornar -4.
     */
    @Test
    public void asignacionTurnoTurnoNoExiste() {
        // Persistir un empleado.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("42345678D");
        emp.setNombre("Empleado4");
        emp.setCorreo("emp4@test.com");
        emp.setActivo(true);
        em.persist(emp);
        em.getTransaction().commit();
        int empId = emp.getId();
        em.close();

        // Usar un id de turno inexistente, por ejemplo 9999.
        int result = sa.asignarTurno(9999, empId);
        assertEquals(-4, result);
    }

    /**
     * Escenario 5: Empleado inexistente.
     * Se crea un turno válido y se intenta asignarlo a un empleado que no existe.
     * Se espera que se retorne -4.
     */
    @Test
    public void asignacionTurnoEmpleadoNoExiste() {
        // Persistir un Rol y un Turno.
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROLTEST4");
        rol.setSalario(10);
        rol.setActivo(true);
        em.persist(rol);
        Timestamp turnoInicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp turnoFin   = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        Turno turno = new Turno();
        turno.setFechaHoraInicio(turnoInicio);
        turno.setFechaHoraFin(turnoFin);
        turno.setEmpleado(null);
        turno.setRol(rol);
        em.persist(turno);
        em.getTransaction().commit();
        int turnoId = turno.getId();
        em.close();

        // Usar un id de empleado inexistente, por ejemplo 9999.
        int result = sa.asignarTurno(turnoId, 9999);
        assertEquals(-4, result);
    }
}
