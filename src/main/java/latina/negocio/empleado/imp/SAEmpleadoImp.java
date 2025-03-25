package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.dispoinibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
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
            q.setParameter("fechaHoraIni", Timestamp.valueOf(turno.getFechaHoraInicio().toString()));
            q.setParameter("fechaHoraFin", Timestamp.valueOf(turno.getFechaHoraFin().toString()));
            List<Disponibilidad> disponibilidades = q.getResultList();

            for (Disponibilidad dispAux : disponibilidades)
            {
                Empleado empleadoAux = dispAux.getEmpleado();
                TEmpleado emp = new TEmpleado(empleadoAux.getDNI(), empleadoAux.getNombre(), empleadoAux.getApellidos(), empleadoAux.getTelefono(),
                        empleadoAux.getCorreo(), empleadoAux.isActivo());
                emp.setId(empleadoAux.getId());
                listaEmpleados.add(emp);
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




    protected EntityManager crearEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
