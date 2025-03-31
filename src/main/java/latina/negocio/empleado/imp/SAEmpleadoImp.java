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


    /// @return Una lista de empleados, una lista vacía si no existen, o null si se produce una excepción
    public List<TEmpleado> buscarEmpleados(){
        EntityTransaction tx = null;
        try (EntityManager em = crearEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            Query queryBuscarEmpleados = em.createNamedQuery("Empleado.findAll");
            List<Empleado> empleados = (List<Empleado>) queryBuscarEmpleados.getResultList();
            List<TEmpleado> resultado = new ArrayList<>();
            for (Empleado e : empleados)
                resultado.add(e.toTransfer());
            return resultado;
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null && tx.isActive())
                tx.rollback();
            return null;
        }
    }

    protected EntityManager crearEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
