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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

            if (turno == null || empleado == null) {
                tx.rollback();
                return -4;
            }

            // Primero comprobamos que el turno está dentro de la disponibilidad del empleado
            // Usamos un algoritmo voraz para tratar de llenar el turno con las disponibilidades
           // Timestamp cubiertoHasta = turno.getFechaHoraInicio();
            List<Disponibilidad> listaDisponibilidades = empleado.getDisponibilidad();
            listaDisponibilidades.sort((d1, d2) -> d1.getFechaInicio().compareTo(d2.getFechaInicio()));

            /*

            En principio esto ya no hace falta, se comprueba antes para la Vista

            for (Disponibilidad disponibilidad : listaDisponibilidades) {
                // Hay un hueco que no se cubre
                if (disponibilidad.getFechaInicio().after(cubiertoHasta) || !cubiertoHasta.before(turno.getFechaHoraFin()))
                    break;
                if (disponibilidad.getFechaFin().after(cubiertoHasta))
                    cubiertoHasta = disponibilidad.getFechaFin();
            }

            assert cubiertoHasta != null;
            if (cubiertoHasta.before(turno.getFechaHoraFin())) {
                // La disponibilidad del empleado no cubre el turno
                tx.rollback();
                return -2;
            }*/

            // Ahora comprobamos que los turnos asignados al empleado no choquen con el nuevo
            List<Turno> listaTurnos = empleado.getTurno();
            for (Turno turnoEmp : listaTurnos) {
                if ((turnoEmp.getFechaHoraInicio().before(turno.getFechaHoraFin()) && turnoEmp.getFechaHoraFin().after(turno.getFechaHoraFin()))
                        || (turno.getFechaHoraInicio().before(turnoEmp.getFechaHoraFin()) && turno.getFechaHoraInicio().after(turnoEmp.getFechaHoraInicio()) )) {
                    tx.rollback();
                    return -3;
                }
            }

            /*
            // Aquí modificamos la disponibilidad del empleado, si está mal, quitar esta parte
            //------------------------------------------------------------------------------------------
            // Primero, combinamos todas las disponibilidades consecutivas
            for (int i = 0; i < listaDisponibilidades.size() - 1; i++) {
                Disponibilidad d1 = listaDisponibilidades.get(i);
                Disponibilidad d2 = listaDisponibilidades.get(i + 1);

                // Comprobar si las disponibilidades son consecutivas y se pueden combinar
                while (d1.getFechaFin().equals(d2.getFechaInicio())) {
                    // Combinar las dos disponibilidades en una
                    d1.setFechaFin(d2.getFechaFin());  // El final de la primera disponibilidad es el final de la segunda
                    em.remove(d2);  // Eliminar la segunda disponibilidad que se ha combinado con la primera
                    listaDisponibilidades.remove(i + 1);  // Eliminar de la lista la segunda disponibilidad
                    if (i + 1 < listaDisponibilidades.size()) {
                        d2 = listaDisponibilidades.get(i + 1); // Obtener la siguiente disponibilidad para seguir combinando
                    } else {
                        break;
                    }
                }
            }

            */

// Ahora procesamos las disponibilidades combinadas
            int i = 0;
            boolean encontrado = false;
            while(i < listaDisponibilidades.size() && !encontrado) {
                Disponibilidad d = listaDisponibilidades.get(i);
                if ((d.getFechaInicio().equals(turno.getFechaHoraInicio()) || d.getFechaInicio().before(turno.getFechaHoraInicio())) && (d.getFechaFin().after(turno.getFechaHoraFin()) || d.getFechaFin().equals(turno.getFechaHoraFin()))) {

                    // Se asigna el turno al empleado
                    turno.setEmpleado(empleado);
                    em.persist(turno);

                    // Caso 1: La disponibilidad se cubre completamente con el turno (eliminación de la disponibilidad)
                    if (d.getFechaInicio().equals(turno.getFechaHoraInicio()) && d.getFechaFin().equals(turno.getFechaHoraFin())) {
                        em.remove(d);  // Elimina la disponibilidad completamente ocupada por el turno
                    }
                    // Caso 2: El turno solo ocupa la parte del inicio de la disponibilidad (recortar la parte inicial)
                    else if (d.getFechaInicio().equals(turno.getFechaHoraInicio())) {
                        d.setFechaInicio(turno.getFechaHoraFin());  // Recorta la parte de la disponibilidad que se cubre al principio
                        em.persist(d);  // Persistir la disponibilidad recortada
                    }
                    // Caso 3: El turno solo ocupa la parte final de la disponibilidad (recortar la parte final)
                    else if (d.getFechaFin().equals(turno.getFechaHoraFin())) {
                        d.setFechaFin(turno.getFechaHoraInicio());  // Recorta la parte de la disponibilidad que se cubre al final
                        em.persist(d);  // Persistir la disponibilidad recortada
                    }
                    // Caso 4: El turno ocupa una parte intermedia de la disponibilidad (dividir la disponibilidad)
                    else {
                        // Creamos una nueva disponibilidad para la parte posterior al turno
                        Disponibilidad nuevaDisponibilidad = new Disponibilidad();
                        nuevaDisponibilidad.setFechaInicio(turno.getFechaHoraFin());
                        nuevaDisponibilidad.setFechaFin(d.getFechaFin());
                        nuevaDisponibilidad.setEmpleado(empleado);
                        em.persist(nuevaDisponibilidad);

                        // Recortamos la disponibilidad original hasta el inicio del turno
                        d.setFechaFin(turno.getFechaHoraInicio());
                        em.persist(d);  // Persistir la disponibilidad recortada
                    }
                    encontrado = true;
                }
                i++;
            }

            if(!encontrado) {
                tx.rollback();
                return -2;
            }

//--------------------------------------------------------------------------------------
            tx.commit();
            return 1; // Operación exitosa
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            return -4; // Error
        }
    }


    @Override
    public List<TTurno> listarTurnosPorDia(String fecha) {
        EntityTransaction tx = null;
        try (EntityManager em = createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            List<TTurno> tturnos = new ArrayList<TTurno>();
            List<Turno> turnos = new ArrayList<Turno>();
            Query q = em.createNamedQuery("Turno.findByDia");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaConvertida = LocalDate.parse(fecha, formatter);
            q.setParameter("dia", fechaConvertida);
            turnos = q.getResultList();
            if (turnos != null) {
                for (Turno turn : turnos) {
                    tturnos.add(new TTurno(turn.getId(), turn.getIdRol(), turn.getFechaHoraInicio(), turn.getFechaHoraFin()));
                }
                em.getTransaction().commit();
                return tturnos;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
