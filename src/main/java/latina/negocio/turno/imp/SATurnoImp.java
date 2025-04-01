package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.dispoinibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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
    public List<TTurno> getTurnosSemana(Timestamp semana) {
        EntityManager em = null;
        List<Turno> turnos = null;
        ArrayList<TTurno> listaTurnos = new ArrayList<>();
        try {
            em = createEntityManager();            // Convierte Timestamp(formato de la fecha de Turno) en LocalDateTime para manipularlo
            LocalDateTime semanaLocalDateTime = semana.toLocalDateTime();
            //Declara el inicio y fin de la semana
            LocalDateTime inicio = semanaLocalDateTime.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
            LocalDateTime fin = semanaLocalDateTime.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).toLocalDate().atTime(23, 59, 59);
            // Convierte de nuevo a Timestamp
            Timestamp inicioTimestamp = Timestamp.valueOf(inicio);
            Timestamp finTimestamp = Timestamp.valueOf(fin);
            //Selecciona de la tabla turno todos los que tienen horas en la semana seleccionada
            TypedQuery<Turno> query = em.createQuery(
                    "SELECT t FROM Turno t WHERE " +
                            "(t.fechaHoraInicio BETWEEN :inicio AND :fin OR " +
                            "t.fechaHoraFin BETWEEN :inicio AND :fin OR " +
                            "(t.fechaHoraInicio <= :inicio AND t.fechaHoraFin >= :fin))",
                    Turno.class
            );
            query.setParameter("inicio", inicioTimestamp);
            query.setParameter("fin", finTimestamp);
            //Guarda la lista de los turnos que cumplen la condición
            turnos = query.getResultList();
            for (Turno a: turnos)
            {
                if(a.getEmpleado() != null){
                    listaTurnos.add(new TTurno(a.getId(), a.getRol().getId(), a.getFechaHoraInicio(), a.getFechaHoraFin(), a.getEmpleado().getId()));
                }
                else{
                    listaTurnos.add(new TTurno(a.getId(), a.getRol().getId(), a.getFechaHoraInicio(), a.getFechaHoraFin()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return listaTurnos;
    }

    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
