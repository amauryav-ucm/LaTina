package latina.negocio.registro.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.empleado.Empleado;
import latina.negocio.registro.Registro;
import latina.negocio.registro.SARegistro;
import latina.negocio.registro.imp.SARegistroImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SARegistroImpTestIT {

    private SARegistro saRegistro;
    private EntityManager em;

    @BeforeEach
    public void setUp() {
        try {
            Field instancia = EMFContainer.class.getDeclaredField("emfc");
            instancia.setAccessible(true);
            instancia.set(null, new EMFContainerImpTest()); // Base de datos de pruebas
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        em = EMFContainer.getInstance().getEMF().createEntityManager();
        saRegistro = new SARegistroImp();
    }

    @Test
    public void testFicharEntradaEmpleadoExistenteYNoTieneEntrada() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        TEmpleado tEmpleado = new TEmpleado("10000000X", "Ana", "López Ruiz", "ana@example.com", "12345678", true, false);
        Empleado empleado = new Empleado(tEmpleado);
        em.persist(empleado);

        tx.commit();

        Timestamp ahora = new Timestamp(System.currentTimeMillis());
        int resultado = saRegistro.ficharEntrada(tEmpleado, ahora);

        assertEquals(1, resultado); // Éxito
    }

    @Test
    public void testFicharEntradaEmpleadoNoExiste() {
        TEmpleado tEmpleado = new TEmpleado("99999999Z", "Fantasma", "Del Más Allá", "ghost@example.com", "00000000", true, false);
        Timestamp ahora = new Timestamp(System.currentTimeMillis());
        int resultado = saRegistro.ficharEntrada(tEmpleado, ahora);

        assertEquals(-1, resultado); // Empleado no existe
    }

    @Test
    public void testFicharEntradaYaTieneEntradaActiva() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        TEmpleado tEmpleado = new TEmpleado("10000001Y", "Carlos", "Pérez", "carlos@example.com", "87654321", true, false);
        Empleado empleado = new Empleado(tEmpleado);
        em.persist(empleado);

        Registro entradaActiva = new Registro(empleado, new Timestamp(System.currentTimeMillis()), 0);
        em.persist(entradaActiva);

        tx.commit();

        int resultado = saRegistro.ficharEntrada(tEmpleado, new Timestamp(System.currentTimeMillis()));

        assertEquals(-2, resultado); // Ya tiene entrada
    }
    @Test
    public void testFicharEntradaLanzaExcepcion() {

        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.close(); // Cerrado para provocar fallo


        SARegistroImp saRegistro = new SARegistroImp() {
            @Override
            protected EntityManager createEntityManager() {
                return em;
            }
        };


        TEmpleado tEmpleado = new TEmpleado("00000000X", "Error", "Fatal", "error@example.com", "99999999", true, false);
        Timestamp hora = new Timestamp(System.currentTimeMillis());


        int resultado = saRegistro.ficharEntrada(tEmpleado, hora);

        assertEquals(-4, resultado); // Éxito del test: Excepción controlada correctamente
    }
    @Test
    public void ficharEntradaPersisteRegistroEnBD() {
        // Preparar base de datos con un empleado válido
        em.getTransaction().begin();
        TEmpleado tEmp = new TEmpleado("99999999Z", "Laura", "Gómez Pérez", "laura@example.com", "654321987", true, false);
        Empleado empleado = new Empleado(tEmp);
        em.persist(empleado);
        em.getTransaction().commit();

        //  Llamar al método que se quiere testear
        Timestamp ahora = new Timestamp(System.currentTimeMillis());
        int resultado = new SARegistroImp().ficharEntrada(tEmp, ahora);

        assertEquals(1, resultado, "Debe devolver 1 si el fichaje fue exitoso");

        //  Verificar persistencia real del registro
        Query q = em.createQuery("SELECT r FROM Registro r WHERE r.empleado.id = :empId ORDER BY r.hInicio DESC");
        q.setParameter("empId", empleado.getId());
        q.setMaxResults(1);
        Registro registro = (Registro) q.getSingleResult();

        assertEquals(empleado.getId(), registro.getEmpleado().getId(), "El empleado del registro debe coincidir");
        assertEquals(ahora.toLocalDateTime().getHour(), registro.gethInicio().toLocalDateTime().getHour(), "La hora del registro debe coincidir (margen de segundos puede variar)");
        assertEquals(0, registro.getnHoras(), "nHoras debe estar inicializado a 0");
    }


}
