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

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        TEmpleado tEmp = new TEmpleado("11111111A","Alba","Martínez",
                "alba@example.com","pass",true);
        Timestamp now = Timestamp.from(Instant.now());
        int res = saRegistro.ficharSalida(tEmp, now);
        assertEquals(-1, res);
    }

    @Test
    public void testFicharSalida_SinRegistroAbierto() {
        TEmpleado tEmp = new TEmpleado("22222222B","Carlos","Gómez",
                "carlos@example.com","pass",true);
        // crear empleado sin registros
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
        int res = saRegistro.ficharSalida(tEmp, Timestamp.from(Instant.now()));
        assertEquals(-2, res);
    }

    @Test
    public void testFicharSalida_HoraAntesDeInicio() {
        TEmpleado tEmp = new TEmpleado("33333333C","Diana","Ruiz",
                "diana@example.com","pass",true);
        Timestamp hInicioTurno = Timestamp.valueOf("2025-04-27 10:00:00");
        Timestamp hFinTurno    = Timestamp.valueOf("2025-04-27 12:00:00");
        Timestamp hSalidaAntes  = Timestamp.valueOf("2025-04-27 09:00:00");

        // Persistir empleado+rol+turno+registro abierto
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp);
            em.persist(emp);
            Rol rol = new Rol(); rol.setNombre("R"); rol.setSalario(10.0); rol.setActivo(true);
            em.persist(rol);
            Turno turno = new Turno();
            turno.setEmpleado(emp);
            turno.setRol(rol);
            turno.setFechaHoraInicio(hInicioTurno);
            turno.setFechaHoraFin(hFinTurno);
            em.persist(turno);
            Registro r = new Registro(emp, hInicioTurno, 0);
            r.setTurno(turno);
            em.persist(r);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharSalida(tEmp, hSalidaAntes);
        assertEquals(-3, res);
    }

    @Test
    public void testFicharSalida_ExitoConTurnoYSalario() {
        TEmpleado tEmp = new TEmpleado("55555555E","Francisco","Torres",
                "francisco@example.com","pass",true);
        Timestamp hInicioRegistro = Timestamp.valueOf("2025-04-27 09:00:00");
        Timestamp hSalida          = Timestamp.valueOf("2025-04-27 14:15:00");

        // Persistir empleado + rol + turno + registro
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp); em.persist(emp);
            Rol rol = new Rol(); rol.setNombre("R"); rol.setSalario(20.0); rol.setActivo(true);
            em.persist(rol);
            Turno turno = new Turno();
            turno.setEmpleado(emp);
            turno.setRol(rol);
            turno.setFechaHoraInicio(hInicioRegistro);
            turno.setFechaHoraFin(Timestamp.valueOf("2025-04-27 17:00:00"));
            em.persist(turno);
            Registro r = new Registro(emp, hInicioRegistro, 0);
            r.setTurno(turno); em.persist(r);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharSalida(tEmp, hSalida);
        assertEquals(1, res);

        // Verificar horas y salario en BD (propiedad DNI en mayúsculas)
        EntityManager em2 = EMFContainer.getInstance().getEMF().createEntityManager();
        try {
            Empleado empBD = em2.createQuery(
                            "SELECT e FROM Empleado e WHERE e.DNI = :DNI", Empleado.class)
                    .setParameter("DNI", tEmp.getDNI())
                    .getSingleResult();
            Registro reg = em2.createQuery(
                            "SELECT r FROM Registro r WHERE r.empleado.id = :id ORDER BY r.hInicio DESC",
                            Registro.class)
                    .setParameter("id", empBD.getId())
                    .setMaxResults(1)
                    .getSingleResult();

            assertNotNull(reg.gethFin());
            assertEquals(5.5, reg.getnHoras(), 0.01);
            assertEquals(5.5 * 20.0, reg.getSalario(), 0.01);
        } finally {
            em2.close();
        }
    }

    // — Tests para retorno -4 —

    @Test
    public void testFicharSalida_DentroVentana14Min59s() {
        TEmpleado tEmp = new TEmpleado("66666666F","Luis","Navarro",
                "luis@example.com","pass",true);
        Timestamp hInicioRegistro = Timestamp.valueOf("2025-04-27 09:00:00");
        Timestamp hFinTurno       = Timestamp.valueOf("2025-04-27 17:00:00");
        Timestamp hSalidaOk       = new Timestamp(
                hFinTurno.getTime()
                        + TimeUnit.MINUTES.toMillis(14)
                        + TimeUnit.SECONDS.toMillis(59)
        );

        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp); em.persist(emp);
            Rol rol = new Rol(); rol.setNombre("R"); rol.setSalario(15.0); rol.setActivo(true);
            em.persist(rol);
            Turno turno = new Turno();
            turno.setEmpleado(emp);
            turno.setRol(rol);
            turno.setFechaHoraInicio(hInicioRegistro);
            turno.setFechaHoraFin(hFinTurno);
            em.persist(turno);
            Registro r = new Registro(emp, hInicioRegistro, 0);
            r.setTurno(turno); em.persist(r);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharSalida(tEmp, hSalidaOk);
        assertEquals(1, res);
    }

    @Test
    public void testFicharSalida_JustoEnLimite15Min() {
        TEmpleado tEmp = new TEmpleado("77777777G","Ana","López",
                "ana@example.com","pass",true);
        Timestamp hInicioRegistro = Timestamp.valueOf("2025-04-27 09:00:00");
        Timestamp hFinTurno       = Timestamp.valueOf("2025-04-27 17:00:00");
        Timestamp hSalidaBad      = new Timestamp(
                hFinTurno.getTime()
                        + TimeUnit.MINUTES.toMillis(15)
        );

        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp); em.persist(emp);
            Rol rol = new Rol(); rol.setNombre("R"); rol.setSalario(15.0); rol.setActivo(true);
            em.persist(rol);
            Turno turno = new Turno();
            turno.setEmpleado(emp);
            turno.setRol(rol);
            turno.setFechaHoraInicio(hInicioRegistro);
            turno.setFechaHoraFin(hFinTurno);
            em.persist(turno);
            Registro r = new Registro(emp, hInicioRegistro, 0);
            r.setTurno(turno); em.persist(r);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharSalida(tEmp, hSalidaBad);
        assertEquals(-4, res);
    }

    @Test
    public void testFicharSalida_MuchoTarde() {
        TEmpleado tEmp = new TEmpleado("88888888H","Marta","García",
                "marta@example.com","pass",true);
        Timestamp hInicioRegistro = Timestamp.valueOf("2025-04-27 09:00:00");
        Timestamp hFinTurno       = Timestamp.valueOf("2025-04-27 17:00:00");
        Timestamp hSalidaTooLate  = new Timestamp(
                hFinTurno.getTime()
                        + TimeUnit.HOURS.toMillis(1)
        );

        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Empleado emp = new Empleado(tEmp); em.persist(emp);
            Rol rol = new Rol(); rol.setNombre("R"); rol.setSalario(15.0); rol.setActivo(true);
            em.persist(rol);
            Turno turno = new Turno();
            turno.setEmpleado(emp);
            turno.setRol(rol);
            turno.setFechaHoraInicio(hInicioRegistro);
            turno.setFechaHoraFin(hFinTurno);
            em.persist(turno);
            Registro r = new Registro(emp, hInicioRegistro, 0);
            r.setTurno(turno); em.persist(r);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }

        int res = saRegistro.ficharSalida(tEmp, hSalidaTooLate);
        assertEquals(-4, res);
    }

    // — Tests de calcular_nHoras por reflexión —

    private double invokeCalcular(Timestamp llegada, Timestamp salida, Timestamp inicio, Timestamp fin) throws Exception {
        Method m = SARegistroImp.class
                .getDeclaredMethod("calcular_nHoras",
                        Timestamp.class, Timestamp.class,
                        Timestamp.class, Timestamp.class);
        m.setAccessible(true);
        return (Double) m.invoke(saRegistro, llegada, salida, inicio, fin);
    }

    @Test
    public void testCalcularNHoras_LlegadaAntesInicio_SalidaEnTurno() throws Exception {
        Timestamp llegada = Timestamp.valueOf("2025-04-29 09:50:00");
        Timestamp inicio  = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp salida  = Timestamp.valueOf("2025-04-29 11:20:00");
        Timestamp fin     = Timestamp.valueOf("2025-04-29 12:00:00");
        double hrs = invokeCalcular(llegada, salida, inicio, fin);
        assertEquals(1.5, hrs, 1e-6);
    }

    @Test
    public void testCalcularNHoras_LlegadaEnTurno_SalidaDespuesFin() throws Exception {
        Timestamp llegada = Timestamp.valueOf("2025-04-29 10:30:00");
        Timestamp inicio  = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp fin     = Timestamp.valueOf("2025-04-29 12:00:00");
        Timestamp salida  = Timestamp.valueOf("2025-04-29 12:10:00");
        double hrs = invokeCalcular(llegada, salida, inicio, fin);
        assertEquals(1.5, hrs, 1e-6);
    }

    @Test
    public void testCalcularNHoras_RestoIgual15m() throws Exception {
        Timestamp inicio  = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp llegada = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp salida  = new Timestamp(inicio.getTime() + TimeUnit.MINUTES.toMillis(75));
        Timestamp fin     = Timestamp.valueOf("2025-04-29 12:00:00");
        double hrs = invokeCalcular(llegada, salida, inicio, fin);
        assertEquals(1.5, hrs, 1e-6);
    }

    @Test
    public void testCalcularNHoras_MenosDe15m() throws Exception {
        Timestamp inicio  = Timestamp.valueOf("2025-04-29 10:00:00");
        Timestamp llegada = Timestamp.valueOf("2025-04-29 09:55:00");
        Timestamp salida  = Timestamp.valueOf("2025-04-29 10:09:00");
        Timestamp fin     = Timestamp.valueOf("2025-04-29 12:00:00");
        double hrs = invokeCalcular(llegada, salida, inicio, fin);
        assertEquals(0.0, hrs, 1e-6);
    }
}
