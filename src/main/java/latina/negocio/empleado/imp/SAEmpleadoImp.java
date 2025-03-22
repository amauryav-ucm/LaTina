package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.turno.Turno;

import java.util.List;

public class SAEmpleadoImp implements SAEmpleado {
    @Override
    public List<Empleado> getEmpleadosDisponibles(Turno turno) {
        EntityManager em = null;
        List<Empleado> empleados = null;
        try {
            em = crearEntityManager();
            //Obtiene los empleados de la tabla Disponibilidad que cubran la fecha y horas completas
            TypedQuery<Empleado> query = em.createQuery(
                    "SELECT DISTINCT d.empleado FROM Disponibilidad d " +
                            "WHERE d.fechaInicio <= :fechaInicio AND d.fechaFin >= :fechaFin " +
                            "AND d.horaInicio <= :horaInicio AND d.horaFin >= :horaFin",
                    Empleado.class
            );
            //Convierte los valores de turno en strings, para que sean compatibles con la BD
            query.setParameter("fechaInicio", turno.getFechaHoraInicio().toLocalDateTime().toLocalDate().toString());
            query.setParameter("fechaFin", turno.getFechaHoraFin().toLocalDateTime().toLocalDate().toString());
            query.setParameter("horaInicio", turno.getFechaHoraInicio().toLocalDateTime().toLocalTime().toString());
            query.setParameter("horaFin", turno.getFechaHoraFin().toLocalDateTime().toLocalTime().toString());
            empleados = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return empleados;
    }

    protected EntityManager crearEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
