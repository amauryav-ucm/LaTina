package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.dispoinibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SATurnoImp implements SATurno {

    @Override
    public int asignarTurno(int idTurno, int idEmpleado) {
        EntityTransaction tx = null;
        try (EntityManager em = createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            Turno turno = em.find(Turno.class, idTurno);
            Empleado empleado = em.find(Empleado.class, idEmpleado);
            // Primero comprobamos que el turno está dentro de la disponibilidad del emplado
            // Usamos un algoritmo voraz para tratar de llenar el turno con las disponibilidades
            Timestamp cubiertoHasta = turno.getFechaHoraInicio();
            List<Disponibilidad> listaDisponibilidades = empleado.getDisponibilidad();
            listaDisponibilidades.sort((d1, d2) -> d1.getFechaInicio().compareTo(d2.getFechaInicio()));
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
        }
    }

    @Override
    public List<TTurno> listarTurnosPorDia() {
        EntityTransaction tx = null;
        try (EntityManager em = createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            List<TTurno> tturnos = new ArrayList<TTurno>();
            List<Turno> turnos = new ArrayList<Turno>();
            Query q = em.createNamedQuery("Turno.findByDia");
            turnos = q.getResultList();
            if (turnos != null) {
                for (Turno turn : turnos) {
                    Turno turnament = em.find(Turno.class, turn.getId());
                    tturnos.add(new TTurno(turnament.getId(), turnament.getIdRol(), turnament.getFechaHoraFin(), turnament.getFechaHoraFin()));
                }
                em.getTransaction().commit();
                return tturnos;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw e;
        } finally {

        }
    }
    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
