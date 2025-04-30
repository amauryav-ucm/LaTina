package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.Rol;
import latina.negocio.rol.SARol;
import latina.negocio.rol.TRol;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.negocio.turno.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SATurnoImpTestsIT {

    private SATurno sa;
    private SARol saRol;

    @BeforeEach
    public void setUp() {
        // Base de datos exclusiva para tests, se crean las tablas antes de cada test y se borran despues
        // Hace falta crear un nuevo esquema llamado bdlatinatest
        try {
            Field instancia = EMFContainer.class.getDeclaredField("emfc");
            instancia.setAccessible(true);
            instancia.set(null, new EMFContainerImpTest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        sa = SAFactory.getInstance().createSATurno();
        saRol = SAFactory.getInstance().createSARol();
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
        // Deberia quedarse sin disponibilidad, pero el remove no funciona
        assertEquals(0, turnoAsignado.getEmpleado().getDisponibilidad().size());
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
    //-------------------------------------------------------------------
    //TESTS ALTA TURNO


    /**
     * Escenario 3: Rol no encontrado.
     * Se intenta crear un turno con un ID de rol que no existe.
     * Se espera que se retorne -1.
     */
    @Test
    public void altaTurnoRolNoExistente() {
        // Crear un TTurno con un rol inexistente
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(9999); // ID que no existe
        tTurno.setIdEmpleado(0);

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicio);
        tTurno.setFechaHoraFin(fin);

        int result = sa.altaTurno(tTurno);

        assertEquals(-1, result);
    }

    /**
     * Escenario 4: Fechas inválidas.
     * Se intenta crear un turno con fechas inválidas (fin antes que inicio o iguales).
     * Se espera que se retorne -2.
     */
    @Test
    public void altaTurnoFechasInvalidas() {
        // 1. Crear y persistir un rol usando saRol
        TRol rol = new TRol("CAMARERO", 4, true);
        int rolId = saRol.altaRol(rol);
        assertTrue(rolId > 0);

        // 2. Caso 1: Fechas iguales
        TTurno tTurno1 = new TTurno();
        tTurno1.setIdRol(rolId);
        tTurno1.setIdEmpleado(0);

        Timestamp mismaFecha = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        tTurno1.setFechaHoraInicio(mismaFecha);
        tTurno1.setFechaHoraFin(mismaFecha);

        int result1 = sa.altaTurno(tTurno1);
        assertEquals(-2, result1);

        // 3. Caso 2: Fecha fin antes que inicio
        TTurno tTurno2 = new TTurno();
        tTurno2.setIdRol(rolId);
        tTurno2.setIdEmpleado(0);

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        tTurno2.setFechaHoraInicio(inicio);
        tTurno2.setFechaHoraFin(fin);

        int result2 = sa.altaTurno(tTurno2);
        assertEquals(-2, result2);
    }

    @Test
    public void altaTurnoExitoso() {
        // Crear un rol y un empleado válidos
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();

        Rol rol = new Rol();
        rol.setNombre("ROL_TEST_OK");
        rol.setSalario(20);
        rol.setActivo(true);
        em.persist(rol);

        Empleado emp = new Empleado();
        emp.setDNI("22334455Y");
        emp.setNombre("Empleado Test");
        emp.setCorreo("test@empresa.com");
        emp.setActivo(true);
        em.persist(emp);

        em.getTransaction().commit();
        int rolId = rol.getId();
        int empId = emp.getId();
        em.close();

        // Crear turno válido
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(empId);

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(2).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(2).withHour(12).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicio);
        tTurno.setFechaHoraFin(fin);

        int result = sa.altaTurno(tTurno);
        assertTrue(result > 0);
    }

    @Test
    public void altaTurnoFechaInicioPasada() {
        // Crear un rol y un empleado válidos
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();

        Rol rol = new Rol();
        rol.setNombre("ROL_TEST_INVALID_DATE");
        rol.setSalario(25);
        rol.setActivo(true);
        em.persist(rol);

        Empleado emp = new Empleado();
        emp.setDNI("55667788Z");
        emp.setNombre("Empleado Fecha Inválida");
        emp.setCorreo("fecha@test.com");
        emp.setActivo(true);
        em.persist(emp);

        em.getTransaction().commit();
        int rolId = rol.getId();
        int empId = emp.getId();
        em.close();

        // Crear turno con fecha de inicio en el pasado
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(empId);

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().minusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().minusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicio);
        tTurno.setFechaHoraFin(fin);

        int result = sa.altaTurno(tTurno);
        assertEquals(-3, result); // Supongamos que -3 indica fecha inválida
    }

    @Test
    public void altaTurno12HorasExactas() {
        // Crear un rol y un empleado válidos
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();

        Rol rol = new Rol();
        rol.setNombre("ROL_TEST_OK");
        rol.setSalario(20);
        rol.setActivo(true);
        em.persist(rol);

        Empleado emp = new Empleado();
        emp.setDNI("22334455Y");
        emp.setNombre("Empleado Test");
        emp.setCorreo("test@empresa.com");
        emp.setActivo(true);
        em.persist(emp);

        em.getTransaction().commit();
        int rolId = rol.getId();
        int empId = emp.getId();
        em.close();

        // Crear turno válido
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(empId);

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(1).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(13).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicio);
        tTurno.setFechaHoraFin(fin);

        int result = sa.altaTurno(tTurno);
        assertTrue(result > 0);
    }

    @Test
    public void altaTurnoMas12Horas() {
        // Crear un rol y un empleado válidos
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();

        Rol rol = new Rol();
        rol.setNombre("ROL_TEST_INVALID_DATE");
        rol.setSalario(25);
        rol.setActivo(true);
        em.persist(rol);

        Empleado emp = new Empleado();
        emp.setDNI("55667788Z");
        emp.setNombre("Empleado Fecha Inválida");
        emp.setCorreo("fecha@test.com");
        emp.setActivo(true);
        em.persist(emp);

        em.getTransaction().commit();
        int rolId = rol.getId();
        int empId = emp.getId();
        em.close();

        // Crear turno con fecha de inicio en el pasado
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(empId);

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(13).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicio);
        tTurno.setFechaHoraFin(fin);

        int result = sa.altaTurno(tTurno);
        assertEquals(-4, result); // Supongamos que -4 indica que se han excedido las 24 horas permitidas
    }

    //-------------------------------------------------------------------
    //TESTS DESASIGNAR TURNO

    @Test
    public void desasignacionTurnoExitosa() {
        // Crear y persistir un empleado
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("98765432B");
        emp.setNombre("Empleado2");
        emp.setCorreo("emp2@test.com");
        emp.setActivo(true);
        em.persist(emp);
        em.getTransaction().commit();
        em.close();

        // Crear y persistir un rol y un turno
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROLTEST2");
        rol.setSalario(15);
        rol.setActivo(true);
        em.persist(rol);

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(14).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(16).truncatedTo(ChronoUnit.HOURS));
        Turno turno = new Turno();
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        turno.setRol(rol);
        turno.setEmpleado(emp);
        em.persist(turno);
        em.getTransaction().commit();
        int turnoId = turno.getId();
        int empId = emp.getId();
        em.close();

        // Llamar al SA para desasignar el turno
        int result = sa.desasignarTurno(turnoId, empId);
        assertEquals(1, result);

        // Verificar que el turno ya no tenga empleado
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        Turno turnoDesasignado = em.find(Turno.class, turnoId);
        assertNull(turnoDesasignado.getEmpleado());

        // Verificar que se haya creado una nueva disponibilidad
        Empleado empleadoRefrescado = em.find(Empleado.class, empId);
        List<Disponibilidad> disponibilidades = empleadoRefrescado.getDisponibilidad();
        assertEquals(1, disponibilidades.size());
        assertEquals(inicio, disponibilidades.get(0).getFechaHoraInicio());
        assertEquals(fin, disponibilidades.get(0).getFechaHoraFin());
        em.close();
    }

    @Test
    public void desasignacionTurnoNoExiste() {
        // Crear y persistir un empleado válido
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp = new Empleado();
        emp.setDNI("11112222C");
        emp.setNombre("Empleado3");
        emp.setCorreo("emp3@test.com");
        emp.setActivo(true);
        em.persist(emp);
        em.getTransaction().commit();
        int empId = emp.getId();
        em.close();

        // Usar ID de turno inexistente
        int result = sa.desasignarTurno(-999, empId);
        assertEquals(-4, result);
    }

    @Test
    public void desasignacionTurnoEmpleadoIncorrecto() {
        // Crear dos empleados
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Empleado emp1 = new Empleado();
        emp1.setDNI("44445555D");
        emp1.setNombre("Empleado4");
        emp1.setCorreo("emp4@test.com");
        emp1.setActivo(true);
        em.persist(emp1);

        Empleado emp2 = new Empleado();
        emp2.setDNI("66667777E");
        emp2.setNombre("Empleado5");
        emp2.setCorreo("emp5@test.com");
        emp2.setActivo(true);
        em.persist(emp2);

        Rol rol = new Rol();
        rol.setNombre("ROLTEST3");
        rol.setSalario(20);
        rol.setActivo(true);
        em.persist(rol);

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(2).withHour(8).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(2).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Turno turno = new Turno();
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        turno.setRol(rol);
        turno.setEmpleado(emp1);
        em.persist(turno);
        em.getTransaction().commit();
        int turnoId = turno.getId();
        int emp2Id = emp2.getId();
        em.close();

        // Intentar desasignar con el empleado incorrecto
        int result = sa.desasignarTurno(turnoId, emp2Id);
        assertEquals(-3, result);
    }



}
