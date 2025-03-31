package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SAEmpleadoImpTestIT {

    private SAEmpleado sa;

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
        sa = SAFactory.getInstance().createSAEmpleado();
    }

    @Test
    public void altaEmpleadoCorrecto(){
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        TEmpleado emp = new TEmpleado("12345678A", "Camilo" , "Suárez", "elsemental@hotmail.com", "651341570", true);
        int id = sa.altaEmpleado(emp);
        assertTrue(id >= 0, "El ID del empleado debe ser mayor o igual a 0");
        Empleado employee = em.find(Empleado.class, id);
        assertNotNull(employee, "El empleado debería haberse guardado en la base de datos");
    }

    @Test
    public void altaEmpleadoRepetidoDNI(){
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        TEmpleado emp = new TEmpleado("12345678A", "Camilo" , "Suárez", "elsemental@hotmail.com", "651341570", true);
        sa.altaEmpleado(emp);
        TEmpleado emp2 = new TEmpleado("12345678A", "Camilo" , "Suárez", "marianoelmarciano@hotmail.com", "651341570", true);
        int id = sa.altaEmpleado(emp2);
        assertTrue(id == -1, "El ID del empleado debe ser mayor o igual a 0");
    }

    @Test
    public void altaEmpleadoRepetidoCorreo(){
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        TEmpleado emp = new TEmpleado("12345678A", "Camilo" , "Suárez", "elsemental@hotmail.com", "651341570", true);
        sa.altaEmpleado(emp);
        TEmpleado emp2 = new TEmpleado("87654321A", "Pedro" , "Pablo", "elsemental@hotmail.com", "651343570", true);
        int id = sa.altaEmpleado(emp2);
        assertTrue(id == -2, "El ID del empleado debe ser mayor o igual a 0");
    }

    @Test
    public void altaEmpleadoDNIIncorrecto(){
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        TEmpleado emp = new TEmpleado("123456789A", "Camilo" , "Suárez", "elsemental@hotmail.com", "651341570", true);
        int id = sa.altaEmpleado(emp);
        assertTrue(id == -3, "El ID del empleado debe ser mayor o igual a 0");
    }

    @Test
    public void altaEmpleadoNumeroIncorrecto(){
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        TEmpleado emp = new TEmpleado("12345677A", "Camilo" , "Suárez", "tumorenito17@hotmail.com", "6513415709", true);
        int id = sa.altaEmpleado(emp);
        assertTrue(id == -4, "El ID del empleado debe ser mayor o igual a 0");
    }

    @Test
    public void altaEmpleadoNombreInCorrecto(){
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        TEmpleado emp = new TEmpleado("12345678A", "C4mil0" , "Suárez", "tumorenito17@hotmail.com", "651341570", true);
        int id = sa.altaEmpleado(emp);
        assertTrue(id == -5, "El ID del empleado debe ser mayor o igual a 0");
    }

    @Test
    public void altaEmpleadoApellidoIncorrecto(){
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        TEmpleado emp = new TEmpleado("12345678A", "Camilo" , "Su4rez", "tumorenito17@hotmail.com", "651341570", true);
        int id = sa.altaEmpleado(emp);
        assertTrue(id == -6, "El ID del empleado debe ser mayor o igual a 0");
    }

    @Test
    public void altaEmpleadoCorreoIncorrecto(){
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        TEmpleado emp = new TEmpleado("12345678A", "Camilo" , "Suarez", "tumorenito17@!@hotmail.com", "651341570", true);
        int id = sa.altaEmpleado(emp);
        assertTrue(id == -7, "El ID del empleado debe ser mayor o igual a 0");
    }
}
