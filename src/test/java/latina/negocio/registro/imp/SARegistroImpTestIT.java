package latina.negocio.registro.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.registro.Registro;
import latina.negocio.registro.SARegistro;
import latina.negocio.registro.imp.SARegistroImp;
import latina.negocio.turno.Turno;
import latina.negocio.rol.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SARegistroImpTestIT {

    private SARegistro saRegistro;

    @BeforeEach
    public void setUp() {
        try {
            var f = EMFContainer.class.getDeclaredField("emfc");
            f.setAccessible(true);
            f.set(null, new EMFContainerImpTest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        saRegistro = new SARegistroImp();
        // limpiar tablas en BD de prueba
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Registro").executeUpdate();
            em.createQuery("DELETE FROM Turno").executeUpdate();
            em.createQuery("DELETE FROM Rol").executeUpdate();
            em.createQuery("DELETE FROM Empleado").executeUpdate();
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }
    }

    @Test
    public void testFicharEntrada_EmpleadoNoExiste() {
        TEmpleado tEmp = new TEmpleado("10101010J","Juan","Pérez","juan@example.com","1234",true);
        Timestamp now = Timestamp.from(Instant.now());

        int res = saRegistro.ficharEntrada(tEmp, now);
        assertEquals(-1, res);
    }

    @Test
    public void testFicharEntrada_RegistroYaExiste() {
        TEmpleado tEmp = new TEmpleado("10101011K","Ana","García","ana@example.com","5678",true);
        Timestamp now = Timestamp.from(Instant.now());
        // crear empleado y registro abierto
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);
            Registro open = new Registro(emp, now, 0);
            em.persist(open);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharEntrada(tEmp, now);
        assertEquals(-2, res);
    }

    @Test
    public void testFicharEntrada_FueraDeVentana() {
        TEmpleado tEmp = new TEmpleado("10101012L","Luis","Martín","luis@example.com","9012",true);
        Timestamp now = Timestamp.valueOf("2025-04-27 18:44:59");
        // crear empleado, rol y turno que no cubre hora+15min
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);
            Rol rol = new Rol();
            rol.setNombre("ROL_TEST");
            rol.setSalario(1000);
            rol.setActivo(true);
            em.persist(rol);
            Turno t = new Turno();
            t.setEmpleado(emp);
            t.setRol(rol);
            t.setFechaHoraInicio(Timestamp.valueOf("2025-04-27 10:00:00"));
            t.setFechaHoraFin(Timestamp.valueOf("2025-04-27 18:00:00"));
            em.persist(t);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharEntrada(tEmp, now);
        assertEquals(-3, res);
    }

    @Test
    public void testFicharEntrada_Exactamente15MinAntesInicio() {
        TEmpleado tEmp = new TEmpleado("10101013M","María","Pérez","maria@example.com","3456",true);
        Timestamp now = Timestamp.valueOf("2025-04-27 18:45:00");
        // crear empleado, rol y turno que inicia a las 19:00
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);
            Rol rol = new Rol();
            rol.setNombre("ROL_TEST");
            rol.setSalario(1000);
            rol.setActivo(true);
            em.persist(rol);
            Turno t = new Turno();
            t.setEmpleado(emp);
            t.setRol(rol);
            t.setFechaHoraInicio(Timestamp.valueOf("2025-04-27 19:00:00"));
            t.setFechaHoraFin(Timestamp.valueOf("2025-04-27 23:00:00"));
            em.persist(t);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharEntrada(tEmp, now);
        assertEquals(1, res);
    }

    @Test
    public void testFicharEntrada_Exactamente15MinAntesFin() {
        TEmpleado tEmp = new TEmpleado("10101014N","Pedro","López","pedro@example.com","7890",true);
        Timestamp now = Timestamp.valueOf("2025-04-27 18:45:00");
        // crear empleado, rol y turno que termina a las 19:00
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);
            Rol rol = new Rol();
            rol.setNombre("ROL_TEST");
            rol.setSalario(1000);
            rol.setActivo(true);
            em.persist(rol);
            Turno t = new Turno();
            t.setEmpleado(emp);
            t.setRol(rol);
            t.setFechaHoraInicio(Timestamp.valueOf("2025-04-27 15:00:00"));
            t.setFechaHoraFin(Timestamp.valueOf("2025-04-27 19:00:00"));
            em.persist(t);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharEntrada(tEmp, now);
        assertEquals(-3, res);
    }

    @Test
    public void testFicharEntrada_15Min1SegundoAntesFin() {
        TEmpleado tEmp = new TEmpleado("10101015O","Lucía","González","lucia@example.com","1122",true);
        Timestamp now = Timestamp.valueOf("2025-04-27 18:44:59");
        // crear empleado, rol y turno que termina a las 19:00
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);
            Rol rol = new Rol();
            rol.setNombre("ROL_TEST");
            rol.setSalario(1000);
            rol.setActivo(true);
            em.persist(rol);
            Turno t = new Turno();
            t.setEmpleado(emp);
            t.setRol(rol);
            t.setFechaHoraInicio(Timestamp.valueOf("2025-04-27 15:00:00"));
            t.setFechaHoraFin(Timestamp.valueOf("2025-04-27 19:00:00"));
            em.persist(t);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharEntrada(tEmp, now);
        assertEquals(1, res);
    }

    // Nuevos tests solicitados:

    @Test
    public void testFicharEntrada_EmpleadoExistenteYValido() {
        TEmpleado tEmp = new TEmpleado("10101016P","Raúl","Suárez","raul@example.com","3344",true);
        Timestamp now = Timestamp.valueOf("2025-04-27 12:00:00");
        // turno cubre ahora +15min (12:15)
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);
            Rol rol = new Rol(); rol.setNombre("ROL_OK"); rol.setSalario(800); rol.setActivo(true);
            em.persist(rol);
            Turno t = new Turno();
            t.setEmpleado(emp);
            t.setRol(rol);
            t.setFechaHoraInicio(Timestamp.valueOf("2025-04-27 11:00:00"));
            t.setFechaHoraFin(Timestamp.valueOf("2025-04-27 20:00:00"));
            em.persist(t);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }
        int res = saRegistro.ficharEntrada(tEmp, now);
        assertEquals(1, res);
    }

    @Test
    public void testFicharEntrada_PersistenciaBD() {
        TEmpleado tEmp = new TEmpleado("10101017Q","Eva","Ramírez","eva@example.com","5566",true);
        Timestamp now = Timestamp.valueOf("2025-04-27 14:30:00");
        // crear emp y turno
        EntityManager em0 = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx0 = em0.getTransaction();
        try {
            tx0.begin();
            Empleado emp = new Empleado(tEmp);
            em0.persist(emp);
            Rol rol = new Rol(); rol.setNombre("ROL_PERSIST"); rol.setSalario(900); rol.setActivo(true);
            em0.persist(rol);
            Turno t = new Turno(); t.setEmpleado(emp); t.setRol(rol);
            t.setFechaHoraInicio(Timestamp.valueOf("2025-04-27 10:00:00"));
            t.setFechaHoraFin(Timestamp.valueOf("2025-04-27 18:00:00"));
            em0.persist(t);
            tx0.commit();
        } finally {
            if (tx0.isActive()) tx0.rollback();
            em0.close();
        }
        // realizar fichaje
        int res = saRegistro.ficharEntrada(tEmp, now);
        assertEquals(1, res);
        // verificar registro persistido
        EntityManager em1 = EMFContainer.getInstance().getEMF().createEntityManager();
        try {
            Query qEmp = em1.createQuery("SELECT e FROM Empleado e WHERE e.DNI=:dni");
            qEmp.setParameter("dni", tEmp.getDNI());
            Empleado empBD = (Empleado) qEmp.getSingleResult();

            Query q = em1.createQuery("SELECT r FROM Registro r WHERE r.empleado.id=:id ORDER BY r.hInicio DESC");
            q.setParameter("id", empBD.getId());
            q.setMaxResults(1);
            Registro rLast = (Registro) q.getSingleResult();
            assertNotNull(rLast);
            assertEquals(tEmp.getDNI(), rLast.getEmpleado().getDNI());
            assertEquals(0, rLast.getnHoras());
        } finally {
            em1.close();
        }
    }
    @Test
    public void testFicharSalida_EmpleadoNoExiste() {
        TEmpleado tEmp = new TEmpleado("11111111A", "Alba", "Martínez", "alba@example.com", "pass", true);
        Timestamp now = Timestamp.from(Instant.now());

        int res = saRegistro.ficharSalida(tEmp, now);
        assertEquals(-1, res);
    }

    @Test
    public void testFicharSalida_SinRegistroAbierto() {
        TEmpleado tEmp = new TEmpleado("22222222B", "Carlos", "Gómez", "carlos@example.com", "pass", true);
        // crear empleado sin registros abiertos
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        Timestamp now = Timestamp.from(Instant.now());
        int res = saRegistro.ficharSalida(tEmp, now);
        assertEquals(-2, res);
    }

    @Test
    public void testFicharSalida_HoraAntesDeInicio() {
        TEmpleado tEmp = new TEmpleado("33333333C", "Diana", "Ruiz", "diana@example.com", "pass", true);
        Timestamp hInicio = Timestamp.valueOf("2025-04-27 10:00:00");
        Timestamp hFinInvalid = Timestamp.valueOf("2025-04-27 09:00:00");
        // crear empleado + registro abierto
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);

            Registro r = new Registro(emp, hInicio, 0);
            em.persist(r);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharSalida(tEmp, hFinInvalid);
        assertEquals(-3, res);
    }

    @Test
    public void testFicharSalida_ExitoSinTurno() {
        TEmpleado tEmp = new TEmpleado("44444444D", "Elena", "Sánchez", "elena@example.com", "pass", true);
        Timestamp hInicio = Timestamp.valueOf("2025-04-27 08:00:00");
        Timestamp hFin = Timestamp.valueOf("2025-04-27 12:30:00");
        // crear empleado + registro abierto
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);

            Registro r = new Registro(emp, hInicio, 0);
            em.persist(r);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharSalida(tEmp, hFin);
        assertEquals(1, res);
    }

    @Test
    public void testFicharSalida_ExitoConTurnoYSalario() {
        TEmpleado tEmp = new TEmpleado("55555555E", "Francisco", "Torres", "francisco@example.com", "pass", true);
        Timestamp hInicio = Timestamp.valueOf("2025-04-27 09:00:00");
        Timestamp hFin = Timestamp.valueOf("2025-04-27 14:15:00");
        // crear empleado + rol + turno + registro
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);

            Rol rol = new Rol();
            rol.setNombre("ROL_TEST");
            rol.setSalario(20); // 20 euros la hora
            rol.setActivo(true);
            em.persist(rol);

            Turno turno = new Turno();
            turno.setEmpleado(emp);
            turno.setRol(rol);
            turno.setFechaHoraInicio(Timestamp.valueOf("2025-04-27 09:00:00"));
            turno.setFechaHoraFin(Timestamp.valueOf("2025-04-27 17:00:00"));
            em.persist(turno);

            Registro r = new Registro(emp, hInicio, 0);
            r.setTurno(turno);
            em.persist(r);

            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharSalida(tEmp, hFin);
        assertEquals(1, res);

        // comprobar datos persistidos
        EntityManager em1 = EMFContainer.getInstance().getEMF().createEntityManager();
        try {
            Query qEmp = em1.createQuery("SELECT e FROM Empleado e WHERE e.DNI=:dni");
            qEmp.setParameter("dni", tEmp.getDNI());
            Empleado empBD = (Empleado) qEmp.getSingleResult();

            Query qReg = em1.createQuery("SELECT r FROM Registro r WHERE r.empleado.id=:id ORDER BY r.hInicio DESC");
            qReg.setParameter("id", empBD.getId());
            qReg.setMaxResults(1);
            Registro reg = (Registro) qReg.getSingleResult();

            assertNotNull(reg.gethFin());
            assertTrue(reg.getnHoras() > 0);

            double expectedHours = 5.5; // 5 horas 15 min => redondea a 5.5
            double expectedSalary = expectedHours * 20; // salario por hora
            assertEquals(expectedHours, reg.getnHoras(), 0.01);
            assertEquals(expectedSalary, reg.getSalario(), 0.01);
        } finally {
            em1.close();
        }
    }
}
