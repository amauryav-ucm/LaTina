package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.negocio.turno.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SATurnoImpTestIT {
    private SATurno sa;

    @BeforeEach
    public void setUp() {
        sa = SAFactory.getInstance().createSATurno();
      //  limpiarBaseDeDatos();
    }

    private void limpiarBaseDeDatos() {
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Turno").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Test
    public void getTurnoSemanalExitoso() {
        //aqui habria q registrar un turno que concuerde con el turno especificado
     //   limpiarBaseDeDatos();
        Timestamp semanaEspecifica = Timestamp.valueOf("2025-03-25 10:30:00");

        List<TTurno> lista = sa.getTurnosSemana(semanaEspecifica);
        if(lista.isEmpty()){
            System.out.println("ERROR");
        }
        //Exito
        assertTrue(!lista.isEmpty());
    }

    @Test
    public void getTurnoSemanalError() {
        //aqui borramos la base de datos para que no hay turnos que listar
        limpiarBaseDeDatos();
        Timestamp semanaEspecifica = Timestamp.valueOf("2025-03-25 10:30:00");

        List<TTurno> lista = sa.getTurnosSemana(semanaEspecifica);
        if(lista.isEmpty()){
            System.out.println("ERROR");
        }
        //Exito
        assertTrue(lista.isEmpty());
    }



}
