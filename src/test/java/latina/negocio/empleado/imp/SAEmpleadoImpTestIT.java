package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SAEmpleadoImpTestIT {

    private SAEmpleadoImp saEmpleadoImp;
    private EntityManager em;  // Declarar como variable de instancia para usarla en cada prueba

    @BeforeEach
    public void setUp() {
        try {
            Field instancia = EMFContainer.class.getDeclaredField("emfc");
            instancia.setAccessible(true);
            instancia.set(null, new EMFContainerImpTest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        saEmpleadoImp = new SAEmpleadoImp();

        // Crear un nuevo EntityManager para cada prueba para evitar problemas de persistencia entre ellas
        em = EMFContainer.getInstance().getEMF().createEntityManager();
    }
    
    @Test
    public void testBuscarEmpleadosConDatos() {
        // Iniciar transacción
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // Crear empleado para probar
        Empleado empleado = new Empleado();
        empleado.setDNI("12345678A");
        empleado.setNombre("Juan");
        empleado.setCorreo("juan.perez@email.com");
        empleado.setTelefono("666777888");
        empleado.setActivo(true);
        em.persist(empleado);

        tx.commit(); // Confirmar la transacción

        // Llamar al método que buscará los empleados en la base de datos
        List<TEmpleado> empleados = saEmpleadoImp.buscarEmpleados();

        // Verificar que la lista no esté vacía
        assertNotNull(empleados);
        assertEquals(1, empleados.size());

        // Verificar que los datos del primer empleado sean correctos
        assertEquals("12345678A", empleados.get(0).getDNI());
        assertEquals("Juan", empleados.get(0).getNombre());
    }

    @Test
    public void testBuscarEmpleadosSinDatos() {
        // No crear ningún empleado, así se asegura que no haya datos en la BD

        // Llamar al método que buscará los empleados en la base de datos
        List<TEmpleado> empleados = saEmpleadoImp.buscarEmpleados();

        // Verificar que la lista esté vacía
        assertNotNull(empleados);
        assertEquals(0, empleados.size());
    }

    @Test
    public void testBuscarEmpleadosConVariosEmpleados() {
        // Iniciar transacción
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // Crear y persistir más empleados
        Empleado empleado1 = new Empleado();
        empleado1.setDNI("12345678A");
        empleado1.setNombre("Juan");
        empleado1.setCorreo("juan.perez@email.com");
        empleado1.setTelefono("666777888");
        empleado1.setActivo(true);
        em.persist(empleado1);

        Empleado empleado2 = new Empleado();
        empleado2.setDNI("23456789B");
        empleado2.setNombre("Maria");
        empleado2.setCorreo("maria.perez@email.com");
        empleado2.setTelefono("666777999");
        empleado2.setActivo(true);
        em.persist(empleado2);

        Empleado empleado3 = new Empleado();
        empleado3.setDNI("34567890C");
        empleado3.setNombre("Carlos");
        empleado3.setCorreo("carlos.perez@email.com");
        empleado3.setTelefono("666777000");
        empleado3.setActivo(true);
        em.persist(empleado3);

        tx.commit(); // Confirmar la transacción

        // Llamar al método que buscará los empleados en la base de datos
        List<TEmpleado> empleados = saEmpleadoImp.buscarEmpleados();

        // Verificar que se han encontrado los 3 empleados
        assertNotNull(empleados);
        assertEquals(3, empleados.size());

        // Verificar que los datos de los empleados sean correctos
        assertEquals("12345678A", empleados.get(0).getDNI());
        assertEquals("Juan", empleados.get(0).getNombre());

        assertEquals("23456789B", empleados.get(1).getDNI());
        assertEquals("Maria", empleados.get(1).getNombre());

        assertEquals("34567890C", empleados.get(2).getDNI());
        assertEquals("Carlos", empleados.get(2).getNombre());
    }

    @AfterEach
    public void cleanUp() {
        // Limpiar la base de datos después de cada prueba
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // Eliminar todos los empleados de la base de datos para mantener los tests independientes
        em.createQuery("DELETE FROM Empleado e").executeUpdate();

        tx.commit(); // Confirmar la transacción
        em.close();  // Cerrar el EntityManager después de cada prueba
    }
}
