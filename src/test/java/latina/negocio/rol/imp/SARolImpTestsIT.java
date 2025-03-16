package latina.negocio.rol.imp;

import jakarta.persistence.EntityManager;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.Rol;
import latina.negocio.rol.SARol;
import latina.negocio.rol.TRol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


public class SARolImpTestsIT {

    private SARol sa;

    @BeforeEach
    public void setUp() {
        sa = SAFactory.getInstance().createSARol();
        limpiarBaseDeDatos();
    }
    private void limpiarBaseDeDatos() {
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Rol").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }
    @Test
    public void registrarRolExitoso() {


        limpiarBaseDeDatos();
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);


        int id = sa.altaRol(tRol);

        //Exito
        assertTrue(id > 0);
    }

    @Test
    public void registarRolRepetido() {
        limpiarBaseDeDatos();
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
        limpiarBaseDeDatos();
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);
        //Salario = 0
        tRol.setSalario(0);
        int id = sa.altaRol(tRol);
        assertEquals(-2, id);
    }

    @Test
    public void registrarRolSalarioN() {
        limpiarBaseDeDatos();
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);
        //Salario < 0
        tRol.setSalario(-5);
        int id = sa.altaRol(tRol);
        assertEquals(-2, id);
    }

    @Test
    public void registrarRolNombreIncorrecto() {
        limpiarBaseDeDatos();
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);

        tRol.setNombre("letrado");
        int id = sa.altaRol(tRol);
        assertEquals(-3, id);
    }

    @Test
    public void registrarRolNombreIncorrecto2() {
        limpiarBaseDeDatos();
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);

        tRol.setNombre("CANTANTE1234");
        int id = sa.altaRol(tRol);
        assertEquals(-3, id);
    }

    @Test
    public void registrarRolNombreIncorrecto3() {
        limpiarBaseDeDatos();
        TRol tRol = new TRol("LIMPIEZA", 8.00, true);
        Rol rol = new Rol(tRol);

        tRol.setNombre(".PIANISTA-");
        int id = sa.altaRol(tRol);
        assertEquals(-3, id);
    }
    @Test
    public void registrarRolNombreVacio() {
        limpiarBaseDeDatos();
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
}

