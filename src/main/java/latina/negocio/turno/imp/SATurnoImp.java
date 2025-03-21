package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.dispoinibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
import java.util.List;

public class SATurnoImp implements SATurno {

    @Override
    public int asignarTurno(int idTurno, int idEmpleado) {
        EntityManager em = null;
        EntityTransaction tx = null;
        int result = -1;
        try {
            em = createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            Turno turno = em.find(Turno.class, idTurno);
            Empleado empleado = em.find(Empleado.class, idEmpleado);
            // Primero comprobamos que el turno esta dentro de la disponibilidad del emplado
            // Usamos un algoritmo voraz para tratar de llenar el turno con las disponibilidades
            Timestamp cubiertoHasta = turno.getFechaHoraInicio();
            List<Disponibilidad> listaDisponibilidades = empleado.getDisponibilidad();
            listaDisponibilidades.sort((d1, d2) -> d1.getHoraInicio().compareTo(d2.getHoraInicio()));
            for (Disponibilidad disponibilidad : listaDisponibilidades) {
                // Estas deberían ser timestamps
                /*
                if(disponibilidad.getHoraInicio()<=cubiertoHasta && cubiertoHasta<disponibilidad.getHoraFin())
                    cubiertoHasta = disponibilidad.getHoraFin();
                 */
            }
            assert cubiertoHasta != null;
            if (cubiertoHasta.before(turno.getFechaHoraFin())) {
                // La disponibilidad del empleado no cubre el turno
                tx.rollback();
                return -2;
            }
            // Ahora vamos a comprobar que los turnos asignados al empleado no choquen con el nuevo
            List<Turno> listaTurnos = empleado.getTurno();
            boolean turnosCompatibles = true;
            for (Turno turnoEmp : listaTurnos) {
                if (turnoEmp.getFechaHoraInicio().before(turno.getFechaHoraFin()) || turno.getFechaHoraInicio().before(turno.getFechaHoraFin())) {
                    turnosCompatibles = false;
                    break;
                }
            }
            if (!turnosCompatibles) {
                // Hay un turno ya asignado que choca con el nuevo
                tx.rollback();
                return -3;
            }
            turno.setEmpleado(empleado);
            tx.commit();
            // Resultado OK
            return 1;
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                tx.rollback();
            return -4;
        } finally {
            if (em != null) em.close();
        }
    }

    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
