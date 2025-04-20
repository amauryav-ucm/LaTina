package latina.negocio.usuario.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.usuario.SAUsuario;
import latina.negocio.usuario.TUsuario;
import latina.negocio.usuario.Usuario;

public class SAUsuarioImp implements SAUsuario {
    @Override
    public int altaUsuario(TUsuario us) {
        EntityManager em = null;
        EntityTransaction trans = null;
        int id = 0;
        try {
            em = crearEntityManager();
            trans = em.getTransaction();
            trans.begin();
            Usuario usuario = new Usuario(us);
            em.persist(usuario);
            trans.commit();
            id = usuario.getId();
        }catch (Exception e) {
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            return -1;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return id;
    }

    protected EntityManager crearEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }

}
