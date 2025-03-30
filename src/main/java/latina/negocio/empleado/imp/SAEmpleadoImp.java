package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.turno.Turno;

import java.util.*;

public class SAEmpleadoImp implements SAEmpleado {
    @Override
    public List<TEmpleado> getEmpleadosDisponibles(int idTurno) {
        EntityManager em = null;
        List<TEmpleado> listaEmpleados = new ArrayList<>();
        try {
            em = crearEntityManager();
            //Obtiene los empleados de la tabla Disponibilidad que cubran la fecha y horas completas

            Query q = em.createNamedQuery("Disponibilidad.findByRangoFecha");
            Turno turno = em.find(Turno.class, idTurno);
            q.setParameter("fechaHoraIni", turno.getFechaHoraInicio());
            q.setParameter("fechaHoraFin", turno.getFechaHoraFin());
            List<Disponibilidad> disponibilidades = q.getResultList();

            for (Disponibilidad dispAux : disponibilidades)
            {
                listaEmpleados.add(dispAux.getEmpleado().toTransfer());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return listaEmpleados;
    }

    @Override
    public int altaEmpleado(TEmpleado emp) {
        EntityManager em = null;
        EntityTransaction trans = null;
        int id =0;
        try {
            em = crearEntityManager();
            trans = em.getTransaction();
            trans.begin();

            Query q = em.createNamedQuery("Empleado.findByDNI");
            q.setParameter("DNI", emp.getDNI());
            List<Empleado> empleados = q.getResultList();
            if(!empleados.isEmpty()){
                trans.rollback();
                return -1; //YA HAY EMPLEADEOS CON ESE DNI
            }

            Query q2 = em.createNamedQuery("Empleado.findByCORREO");
            q2.setParameter("CORREO", emp.getCorreo());
            List<Empleado> empleados2 = q2.getResultList();
            if(!empleados2.isEmpty()){
                trans.rollback();
                return -2; //YA HAY EMPLEADEOS CON ESE CORREO ELECTRONICO
            }
            if(!emp.getDNI().matches("\\d{8}[A-Z]")){
                trans.rollback();
                return -3; // DNI EN FORMATO INCORRECTO
            }
            if(!emp.getTelefono().matches("\\d{9}")){
                trans.rollback();
                return -4; // NUM TELEFONO EN FORMATO INCORRECTO
            }
            Empleado employee = new Empleado(emp);
            em.persist(employee);
            trans.commit();
            id = employee.getId();
        } catch (Exception e) {
            e.printStackTrace();
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
