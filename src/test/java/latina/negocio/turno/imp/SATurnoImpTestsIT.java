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
     * Escenario 1: Alta de turno exitosa sin empleado.
     * Se crea un rol válido y se da de alta un turno sin empleado asignado.
     * Se comprueba que el turno se ha creado correctamente en la BD.
     */
    @Test
    public void altaTurnoExitosoSinEmpleado() {
        // 1. Crear y persistir un rol usando saRol
        TRol rol = new TRol("CAMARERO", 6 , true);
        int rolId = saRol.altaRol(rol);
        assertTrue(rolId > 0);

        // 2. Crear un TTurno sin empleado
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(0); // Sin empleado

        // Fechas para el turno: mañana de 10:00 a 12:00
        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicio);
        tTurno.setFechaHoraFin(fin);

        // 3. Llamar al SA para dar de alta el turno
        int result = sa.altaTurno(tTurno);
        assertTrue(result > 0);
    }

    /**
     * Escenario 2: Alta de turno exitosa con empleado.
     * Se crea un rol válido, un empleado válido y se da de alta un turno con empleado asignado.
     * Se comprueba que el turno se ha creado correctamente en la BD.
     */
    @Test
    public void altaTurnoExitosoConEmpleado() {
        // 1. Crear y persistir un rol
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("MAITRE");
        rol.setSalario(5);
        rol.setActivo(true);
        em.persist(rol);

        // 2. Crear y persistir un empleado
        Empleado emp = new Empleado();
        emp.setDNI("12345678Z");
        emp.setNombre("EmpleadoAltaTurno");
        emp.setCorreo("empalta@test.com");
        emp.setActivo(true);
        em.persist(emp);

        em.getTransaction().commit();
        int rolId = rol.getId();
        int empId = emp.getId();
        em.close();

        // 3. Crear un TTurno con empleado
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(empId);

        // Fechas para el turno: mañana de 14:00 a 16:00
        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(14).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(16).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicio);
        tTurno.setFechaHoraFin(fin);

        // 4. Llamar al SA para dar de alta el turno
        int result = sa.altaTurno(tTurno);

        // El resultado debe ser positivo (ID del turno creado)
        assertTrue(result > 0);

        // 5. Verificar en la BD que el turno se ha creado correctamente
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        Turno turnoCreado = em.find(Turno.class, result);

        assertNotNull(turnoCreado);
        assertEquals(rolId, turnoCreado.getRol().getId());
        assertNotNull(turnoCreado.getEmpleado());
        assertEquals(empId, turnoCreado.getEmpleado().getId());
        assertEquals(inicio, turnoCreado.getFechaHoraInicio());
        assertEquals(fin, turnoCreado.getFechaHoraFin());
        em.close();
    }

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

    /**
     * Escenario 5: Empleado no encontrado.
     * Se intenta crear un turno con un ID de empleado que no existe.
     * Se espera que se retorne -3.
     */
    @Test
    public void altaTurnoEmpleadoNoExistente() {
        // 1. Crear y persistir un rol usando saRol
        TRol rol = new TRol("GERENTE", 10, true);
        int rolId = saRol.altaRol(rol);
        assertTrue(rolId > 0);

        // 2. Crear un TTurno con empleado inexistente
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(9999); // ID que no existe

        Timestamp inicio = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp fin = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicio);
        tTurno.setFechaHoraFin(fin);

        int result = sa.altaTurno(tTurno);
        assertEquals(-3, result);
    }


    /**
     * Escenario 6: Solapamiento de turnos.
     * Se crea un empleado con un turno ya asignado y se intenta asignarle otro turno que se solapa.
     * Se espera que se retorne -4.
     */
    @Test
    public void altaTurnoSolapamiento() {
        // 1. Crear y persistir un rol y un empleado
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROL_ALTA_TURNO5");
        rol.setSalario(15);
        rol.setActivo(true);
        em.persist(rol);

        Empleado emp = new Empleado();
        emp.setDNI("98765432Y");
        emp.setNombre("EmpleadoSolape");
        emp.setCorreo("empsolape@test.com");
        emp.setActivo(true);
        em.persist(emp);

        // 2. Crear un turno existente de 10:00 a 12:00
        Timestamp inicioExistente = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp finExistente = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));

        Turno turnoExistente = new Turno();
        turnoExistente.setFechaHoraInicio(inicioExistente);
        turnoExistente.setFechaHoraFin(finExistente);
        turnoExistente.setEmpleado(emp);
        turnoExistente.setRol(rol);
        em.persist(turnoExistente);

        em.getTransaction().commit();
        int rolId = rol.getId();
        int empId = emp.getId();
        em.close();

        // 3. Intentar crear un nuevo turno que se solapa (de 11:00 a 13:00)
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(empId);

        Timestamp inicioNuevo = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(11).truncatedTo(ChronoUnit.HOURS));
        Timestamp finNuevo = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(13).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicioNuevo);
        tTurno.setFechaHoraFin(finNuevo);

        int result = sa.altaTurno(tTurno);

        assertEquals(-4, result);
    }

    /**
     * Escenario 7: Alta de turno con fechas límite.
     * Prueba el caso en que un turno termina exactamente cuando otro comienza.
     * Esto no debería considerarse solapamiento.
     */
    @Test
    public void altaTurnoFechasLimite() {
        // 1. Crear y persistir un rol y un empleado
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        Rol rol = new Rol();
        rol.setNombre("ROL_ALTA_TURNO6");
        rol.setSalario(15);
        rol.setActivo(true);
        em.persist(rol);

        Empleado emp = new Empleado();
        emp.setDNI("11223344X");
        emp.setNombre("EmpleadoLimite");
        emp.setCorreo("emplimite@test.com");
        emp.setActivo(true);
        em.persist(emp);

        // 2. Crear un turno existente de 08:00 a 10:00
        Timestamp inicioExistente = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(8).truncatedTo(ChronoUnit.HOURS));
        Timestamp finExistente = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));

        Turno turnoExistente = new Turno();
        turnoExistente.setFechaHoraInicio(inicioExistente);
        turnoExistente.setFechaHoraFin(finExistente);
        turnoExistente.setEmpleado(emp);
        turnoExistente.setRol(rol);
        em.persist(turnoExistente);

        em.getTransaction().commit();
        int rolId = rol.getId();
        int empId = emp.getId();
        em.close();

        // 3. Intentar crear un nuevo turno que comienza exactamente cuando termina el otro (10:00 a 12:00)
        TTurno tTurno = new TTurno();
        tTurno.setIdRol(rolId);
        tTurno.setIdEmpleado(empId);

        Timestamp inicioNuevo = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS));
        Timestamp finNuevo = Timestamp.valueOf(LocalDateTime.now().plusDays(1).withHour(12).truncatedTo(ChronoUnit.HOURS));
        tTurno.setFechaHoraInicio(inicioNuevo);
        tTurno.setFechaHoraFin(finNuevo);

        int result = sa.altaTurno(tTurno);

        // Según la implementación, esto no debería considerarse solapamiento
        // El resultado debería ser positivo (ID del turno creado)
        assertTrue(result > 0);

        // Verificar que se creó correctamente
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        Turno turnoCreado = em.find(Turno.class, result);

        assertNotNull(turnoCreado);
        assertEquals(rolId, turnoCreado.getRol().getId());
        assertEquals(empId, turnoCreado.getEmpleado().getId());
        assertEquals(inicioNuevo, turnoCreado.getFechaHoraInicio());
        assertEquals(finNuevo, turnoCreado.getFechaHoraFin());
        em.close();
    }

}
