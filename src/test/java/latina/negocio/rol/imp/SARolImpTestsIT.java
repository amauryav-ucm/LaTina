package latina.negocio.rol.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import latina.integracion.emfc.EMFContainer;
import latina.integracion.emfc.imp.EMFContainerImp;
import latina.integracion.emfc.imp.EMFContainerImpTest;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.Rol;
import latina.negocio.rol.SARol;
import latina.negocio.rol.TRol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class SARolImpTestsIT {

    private SARol sa;

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
        sa = SAFactory.getInstance().createSARol();
    }

    @Test
    public void registrarRolExitoso() {
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);
        int id = sa.altaRol(tRol);

        //Exito
        assertTrue(id > 0);
    }

    @Test
    public void registarRolRepetido() {
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);
        int id = sa.altaRol(tRol);
        id = sa.altaRol(tRol);
        //Como ya existe el nombre
        /* id = sa.altaRol(tRol);*/
        assertEquals(-1, id);

    }


    @Test
    public void registrarRolSalarioO() {
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);
        //Salario = 0
        tRol.setSalario(0);
        int id = sa.altaRol(tRol);
        assertEquals(-2, id);
    }

    @Test
    public void registrarRolSalarioN() {
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);
        //Salario < 0
        tRol.setSalario(-5);
        int id = sa.altaRol(tRol);
        assertEquals(-2, id);
    }

    @Test
    public void registrarRolNombreIncorrecto() {
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);

        tRol.setNombre("letrado");
        int id = sa.altaRol(tRol);
        assertEquals(-3, id);
    }

    @Test
    public void registrarRolNombreIncorrecto2() {
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);

        tRol.setNombre("CANTANTE1234");
        int id = sa.altaRol(tRol);
        assertEquals(-3, id);
    }

    @Test
    public void registrarRolNombreIncorrecto3() {
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);

        tRol.setNombre(".PIANISTA-");
        int id = sa.altaRol(tRol);
        assertEquals(-3, id);
    }

    @Test
    public void registrarRolNombreVacio() {
        TRol tRol = new TRol("", 8.00, true);

        int id = sa.altaRol(tRol);
        assertEquals(-3, id);
    }

    @Test
    public void registrarRolNombreNull() {
        TRol tRol = new TRol(null, 8.00, true);
        int id = sa.altaRol(tRol);
        assertEquals(-4, id);
    }

    /*@Test //es imposible este caso ya que registrarRol.js ya se encarga de que no pase
    public void registrarRolNombreSoloEspacios() {
        TRol tRol = new TRol("   ", 8.00, true);
        int id = sa.altaRol(tRol);
        assertEquals(-3, id);
    }*/
    @Test
    public void registrarRolNulo() {
        int id = sa.altaRol(null);
        assertEquals(-4, id); // Asumiendo que la excepción da este código
    }

    @Test
    public void verificarRollbackTrasFallo() {
        TRol tRol1 = new TRol("LIMPIEZA", 1000, true);
        sa.altaRol(tRol1);

        TRol tRol2 = new TRol("LIMPIEZA", 1200, true); // Nombre repetido
        int id2 = sa.altaRol(tRol2);
        assertEquals(-1, id2);

        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        long count = (long) em.createQuery("SELECT COUNT(r) FROM Rol r").getSingleResult();
        em.close();

        assertEquals(1, count); // Asegurar que el rollback funcionó y solo hay 1 registro
    }
    //-----------------------------------------------------------------
    //TESTS BUSCAR ROLES
    @Test
    public void buscarRolesExitoso() {
        // Insertar roles de prueba en la base de datos
        TRol tRol1 = new TRol("COCINERO", 6, true);
        sa.altaRol(tRol1);
        TRol tRol2 = new TRol("MAITRE", 5, true);
        sa.altaRol(tRol2);

        // Llamar a buscarRoles() para obtener todos los roles
        List<TRol> roles = sa.buscarRoles();

        // Verificar que los roles fueron encontrados correctamente
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(r -> r.getNombre().equals("COCINERO")));
        assertTrue(roles.stream().anyMatch(r -> r.getNombre().equals("MAITRE")));
    }

    @Test
    public void buscarRolesConUnSoloRol() {
        // Insertar un solo rol
        TRol tRol = new TRol("COCINERO", 6, true);
        sa.altaRol(tRol);

        // Llamar a buscarRoles() y verificar que se devuelva solo un rol
        List<TRol> roles = sa.buscarRoles();

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("COCINERO", roles.get(0).getNombre());
    }

    @Test
    public void buscarRolesRollbackTrasFallo() {
        // Intentar insertar un rol con un nombre incorrecto (debería fallar, por ejemplo, nombre vacío o nulo)
        TRol tRol = new TRol("", 7, true);  // Nombre vacío, debería generar error en la validación
        int id = sa.altaRol(tRol);

        // Verificar que la inserción falló
        assertEquals(-3, id);  // Asumiendo que la validación devuelve -3 en este caso

        // Llamar a buscarRoles() para asegurar que no se haya insertado el rol
        List<TRol> roles = sa.buscarRoles();

        // Verificar que la lista esté vacía porque el rol no fue insertado
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }
}

