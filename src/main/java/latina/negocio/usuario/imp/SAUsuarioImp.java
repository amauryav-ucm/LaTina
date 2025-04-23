package latina.negocio.usuario.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.usuario.SAUsuario;
import latina.negocio.usuario.TUsuario;
import latina.negocio.usuario.Usuario;

import java.util.List;

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
        } catch (Exception e) {
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

    @Override
    public int iniciarSesion(TUsuario us) {
        EntityTransaction trans = null;
        try (EntityManager em = crearEntityManager()) {
            trans = em.getTransaction();
            trans.begin();
            Query findByNombreUsuario = em.createNamedQuery("Usuario.findByNombreUsuario");
            findByNombreUsuario.setParameter("usuario", us.getUsuario());
            List<Usuario> usuarios = findByNombreUsuario.getResultList();
            trans.commit();
            if (usuarios.isEmpty())
                return -1;
            Usuario usuario = usuarios.get(0);
            if (!usuario.getContrasenya().equals(us.getContrasenya()))
                return -2;
            if (!usuario.isActivo())
                return -3;
            if (usuario.isEsGerente())
                return 2;
            else
                return 1;
        } catch (Exception e) {
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            return -4;
        }
    }

    @Override
    public TEmpleado conseguirEmpleado(TUsuario us)
    {
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        Query q = em.createNamedQuery("Empleado.findByDNI");//Amaury no me pegues ya lo cambiaré jajajajja
        q.setParameter("DNI", us.getUsuario());
        List<Empleado> empleados = q.getResultList();
        em.close();
        Empleado emp = empleados.get(0);
        return new TEmpleado(emp.getId(), emp.getDNI(), emp.getNombre(), emp.getApellidos(), emp.getCorreo(), emp.getTelefono(), true);
    }

    @Override
    public void inicializarGerente() {
        EntityTransaction trans = null;
        try (EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager()) {
            Query q = em.createNamedQuery("Usuario.findGerente");
            List<Usuario> gerentes = q.getResultList();
            if (gerentes.isEmpty()) {
                trans = em.getTransaction();
                Usuario gerente = new Usuario();
                gerente.setUsuario("admin");
                gerente.setContrasenya("admin123");
                gerente.setEsGerente(true);
                gerente.setActivo(true);
                em.getTransaction().begin();
                em.persist(gerente);
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }

        }
    }

    public EntityManager crearEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }

}
