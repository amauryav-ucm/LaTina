package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;

import java.util.List;

public class SAEmpleadoImp implements SAEmpleado {
    @Override
    public int altaEmpleado(TEmpleado tEmpleado) {
        EntityManager em = null;
        EntityTransaction trans = null;
        int id = 0;
        try {
            em = crearEntityManager();
            trans = em.getTransaction();
            trans.begin();
            if(!tEmpleado.getDNI().matches("^[0-9]{8}[A-Z]$"))
            {
                trans.rollback();
                return -1;//Formato DNI: 8 numeros y 1 letra mayuscula al final
            }
            Query buscarPorDNI = em.createNamedQuery("Empleado.findByDNI");
            buscarPorDNI.setParameter("DNI", tEmpleado.getDNI());
            List<Object> l = buscarPorDNI.getResultList();
            if(!l.isEmpty() && ((Empleado)l.get(0)).getDNI().equals(tEmpleado.getDNI()))
            {
                trans.rollback();
                return -2;//EL DNI YA EXISTE EN LA BD
            }
            if (!tEmpleado.getNombre().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+"))
            {
                trans.rollback();
                return -3;//Solo se permiten letras(incluyendo ñ y tildes) y espacios
            }
            if (!tEmpleado.getApellidos().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+"))
            {
                trans.rollback();
                return -4;//Solo se permiten letras(incluyendo ñ y tildes) y espacios
            }
            if(!tEmpleado.getCorreo().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$"))
            {
                trans.rollback();
                return -5;//Comprueba que el formato del correo es válido
                //antes del @ permite letras, números, ".", "-", "_" para el nombre
                //@ obligatorio
                //después de @ permite letras, números, ".", "-", "_" para el dominio
                //"." obligatorio
                //después del "." al menos 2 letras y como máximo 6 letras
            }
            Query buscarPorCorreo = em.createNamedQuery("Empleado.findByCorreo");
            buscarPorCorreo.setParameter("correo", tEmpleado.getCorreo());
            l = buscarPorCorreo.getResultList();
            if(!l.isEmpty() && ((Empleado)l.get(0)).getCorreo().equals(tEmpleado.getCorreo()))
            {
                trans.rollback();
                return -6;//EL CORREO YA EXISTE EN LA BD
            }
            if(!tEmpleado.getTelefono().matches("^[0-9]{9}$"))
            {
                trans.rollback();
                return -7;//Formato telefono: solo numeros de 9 digitos
            }

            Empleado miEmpleado = new Empleado(tEmpleado);
            em.persist(miEmpleado);
            trans.commit();
            id = miEmpleado.getId();

        }catch (Exception e) {
            if (trans != null && trans.isActive())
                trans.rollback();
            return -8;
        }finally {
            if (em != null)
                em.close();
        }
        return id;
    }
    protected EntityManager crearEntityManager(){
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }

}
