package latina.negocio.registro.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.registro.Registro;
import latina.negocio.registro.SARegistro;
import latina.negocio.rol.Rol;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
import java.util.List;

public class SARegistroImp implements SARegistro {
    //10101010J', 1, 'Pérez Gómez', 'correoo@example.com', 'Juan', '123456788');
    TEmpleado emple = new TEmpleado("10101010J","Juan", "Pérez Gómez","correoo@example.com","123456788", true , false);
    Timestamp now = new Timestamp(System.currentTimeMillis());
    int a = ficharEntrada(emple, now);

    @Override
    public int ficharEntrada(TEmpleado tEmpleado, Timestamp hora) {
        EntityManager em = null;
        EntityTransaction trans = null;
        try {
            em = createEntityManager();
            trans = em.getTransaction();
            trans.begin();
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            Query q = em.createNamedQuery("Empleado.findByDNI");
            q.setParameter("dni", tEmpleado.getDNI());
            List<Empleado> emp = q.getResultList();
            if(emp.isEmpty()){
                trans.rollback();
                return -1; // EL EMPLEADO NO EXISTE
            }else{
                Registro reg = new Registro(emp.get(0),ahora, 0);
                em.persist(reg);
            }

            trans.commit();

            return 1; //FUE CORRECTAMENTE

        } catch (Exception e) {
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            e.printStackTrace();
            return -4; // Código de error para excepción general
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
