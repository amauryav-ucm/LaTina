package latina.negocio.disponibilidad.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.disponibilidad.SADisponibilidad;
import latina.negocio.disponibilidad.TDisponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.rol.Rol;

import java.util.List;

public class SADisponibilidadImp implements SADisponibilidad {

    @Override
    public int altaDisponibilidad(TDisponibilidad tDisponibilidad) {
        EntityManager em = null;
        EntityTransaction trans = null;
        int id = 0;
        try {
            em = crearEntityManager();
            trans = em.getTransaction();
            trans.begin();
            Empleado emp = em.find(Empleado.class, tDisponibilidad.getEmpleadoId());

            if(emp == null) {
                trans.rollback();
                return -1;
            }else if(tDisponibilidad.getFechaFin().equals(tDisponibilidad.getFechaInicio())
                    || tDisponibilidad.getFechaFin().before(tDisponibilidad.getFechaInicio())){
                trans.rollback();
                return -2;
            }else{
                Disponibilidad disp = new Disponibilidad(emp, tDisponibilidad);
                em.persist(disp);
                trans.commit();
                id = disp.getId();
            }
        }catch (Exception e) {
            if (trans != null && trans.isActive())
                trans.rollback();
            return -3;
        } finally {
            if (em != null)
                em.close();
        }
        return id;
    }

    @Override
    public void combinarDisponibilidad(TDisponibilidad tDisponibilidad) {

    }

    protected EntityManager crearEntityManager(){
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }

}
