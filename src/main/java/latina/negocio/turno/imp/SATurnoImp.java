package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.rol.Rol;
import latina.negocio.rol.TRol;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.negocio.turno.TTurnoRolEmpleado;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
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
            List<Disponibilidad> listaDisponibilidades = empleado.getDisponibilidad();
            listaDisponibilidades.sort((d1, d2) -> d1.getFechaHoraInicio().compareTo(d2.getFechaHoraInicio()));

            // Ahora comprobamos que los turnos asignados al empleado no choquen con el nuevo
            List<Turno> listaTurnos = empleado.getTurno();
            for (Turno turnoEmp : listaTurnos) {
                if (turnoEmp.solapaCon(turno.getFechaHoraInicio(),turno.getFechaHoraFin())) {
                    tx.rollback();
                    return -3;
                }
            }

// Ahora procesamos las disponibilidades combinadas
            int i = 0;
            boolean encontrado = false;
            while(i < listaDisponibilidades.size() && !encontrado) {
                Disponibilidad d = listaDisponibilidades.get(i);
                if ((d.getFechaHoraInicio().equals(turno.getFechaHoraInicio()) || d.getFechaHoraInicio().before(turno.getFechaHoraInicio())) && (d.getFechaHoraFin().after(turno.getFechaHoraFin()) || d.getFechaHoraFin().equals(turno.getFechaHoraFin()))) {

                    // Se asigna el turno al empleado
                    turno.setEmpleado(empleado);
                    em.persist(turno);

                    // Caso 1: La disponibilidad se cubre completamente con el turno (eliminación de la disponibilidad)
                    if (d.getFechaHoraInicio().equals(turno.getFechaHoraInicio()) && d.getFechaHoraFin().equals(turno.getFechaHoraFin())) {
                        //em.remove(d);  // Elimina la disponibilidad completamente ocupada por el turno
                        Query q = em.createNamedQuery("Disponibilidad.delete");
                        q.setParameter("id", d.getId());
                        q.executeUpdate();
                    }
                    // Caso 2: El turno solo ocupa la parte del inicio de la disponibilidad (recortar la parte inicial)
                    else if (d.getFechaHoraInicio().equals(turno.getFechaHoraInicio())) {
                        d.setFechaHoraInicio(turno.getFechaHoraFin());  // Recorta la parte de la disponibilidad que se cubre al principio
                        em.persist(d);  // Persistir la disponibilidad recortada
                    }
                    // Caso 3: El turno solo ocupa la parte final de la disponibilidad (recortar la parte final)
                    else if (d.getFechaHoraFin().equals(turno.getFechaHoraFin())) {
                        d.setFechaHoraFin(turno.getFechaHoraInicio());  // Recorta la parte de la disponibilidad que se cubre al final
                        em.persist(d);  // Persistir la disponibilidad recortada
                    }
                    // Caso 4: El turno ocupa una parte intermedia de la disponibilidad (dividir la disponibilidad)
                    else {
                        // Creamos una nueva disponibilidad para la parte posterior al turno
                        Disponibilidad nuevaDisponibilidad = new Disponibilidad();
                        nuevaDisponibilidad.setFechaHoraInicio(turno.getFechaHoraFin());
                        nuevaDisponibilidad.setFechaHoraFin(d.getFechaHoraFin());
                        nuevaDisponibilidad.setEmpleado(empleado);
                        em.persist(nuevaDisponibilidad);

                        // Recortamos la disponibilidad original hasta el inicio del turno
                        d.setFechaHoraFin(turno.getFechaHoraInicio());
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
            e.printStackTrace();
            if (tx != null && tx.isActive()) tx.rollback();
            return -5; // Error
        }
    }


    @Override
    public List<TTurnoRolEmpleado> listarTurnosPorDia(String fecha) {
        EntityTransaction tx = null;
        try (EntityManager em = createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            List<TTurnoRolEmpleado> tturnos = new ArrayList<TTurnoRolEmpleado>();
            List<Turno> turnos = new ArrayList<Turno>();
            Query q = em.createNamedQuery("Turno.findByDia");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaConvertida = LocalDate.parse(fecha, formatter);
            q.setParameter("dia", fechaConvertida);
            turnos = q.getResultList();
            if (turnos != null) {
                for (Turno turn : turnos) {
                    tturnos.add(new TTurnoRolEmpleado(turn.toTransfer(), turn.getRol().toTransfer()));
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

    @Override
    public int altaTurno(TTurno tTurno) {
        EntityManager em = null;
        EntityTransaction trans = null;

        try {
            em = createEntityManager();
            trans = em.getTransaction();
            trans.begin();
            Timestamp ahora = new Timestamp(System.currentTimeMillis());

            // 1. Validar rol
            Rol rol = em.find(Rol.class, tTurno.getIdRol());
            if (rol == null) {
                trans.rollback();
                return -1; // Código de error para rol no encontrado
            }


            // 2. Validar fechas
            if (tTurno.getFechaHoraFin().equals(tTurno.getFechaHoraInicio()) ||
                    tTurno.getFechaHoraFin().before(tTurno.getFechaHoraInicio())) {
                trans.rollback();
                return -2; // Código de error para fechas inválidas
            }

            if(tTurno.getFechaHoraInicio().before(ahora)){
                trans.rollback();
                return -3;
            }

            // 3. Crear y persistir turno
            Turno turno = new Turno(tTurno, rol);

            em.persist(turno);
            trans.commit();

            return turno.getId(); // Éxito: devuelve el ID del turno creado

        } catch (Exception e) {
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            e.printStackTrace();
            return -5; // Código de error para excepción general
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }


    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
