package latina.negocio.turno.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.registro.Registro;
import latina.negocio.rol.Rol;
import latina.negocio.rol.TRol;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.rol.TRol;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.negocio.turno.TTurnoRolEmpleado;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Calendar;
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
    public int desasignarTurno(int idTurno, int idEmpleado) {
        EntityTransaction tx = null;
        try (EntityManager em = createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();

            // Buscar el turno y el empleado por sus IDs
            Turno turno = em.find(Turno.class, idTurno);
            Empleado empleado = em.find(Empleado.class, idEmpleado);

            // Verificar si el turno o el empleado no existen
            if (turno == null || empleado == null) {
                tx.rollback();
                return -4; // Error: Turno o empleado no encontrados
            }

            // Verificar que el turno esté asignado al empleado
            if (turno.getEmpleado() == null || !turno.getEmpleado().equals(empleado)) {
                tx.rollback();
                return -3; // Error: El turno no está asignado a este empleado
            }

            // Desasignar el turno: quitar la referencia al empleado
            turno.setEmpleado(null);
            em.persist(turno); // Persistir el cambio en la base de datos

            // Restaurar la disponibilidad del empleado
            Disponibilidad nuevaDisponibilidad = new Disponibilidad();
            nuevaDisponibilidad.setFechaHoraInicio(turno.getFechaHoraInicio());
            nuevaDisponibilidad.setFechaHoraFin(turno.getFechaHoraFin());
            nuevaDisponibilidad.setEmpleado(empleado);
            em.persist(nuevaDisponibilidad); // Persistir la nueva disponibilidad

            // Ahora fusionamos esta nueva disponibilidad con las existentes
            // Para evitar duplicados o solapamientos innecesarios
            combinarDisponibilidad(nuevaDisponibilidad.getId(), em);

            tx.commit();
            return 1; // Operación exitosa
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null && tx.isActive()) tx.rollback();
            return -5; // Error genérico
        }
    }

    public void combinarDisponibilidad(int idDisponibilidad, EntityManager em) {


            // Obtener la disponibilidad recién creada
            Disponibilidad nuevaDisponibilidad = em.find(Disponibilidad.class, idDisponibilidad);

            // Buscamos todas las disponibilidades del empleado
            List<Disponibilidad> lista = nuevaDisponibilidad.getEmpleado().getDisponibilidad();

            for (Disponibilidad disp : lista) {
                if (nuevaDisponibilidad.seDebeUnirCon(disp)) {
                    // Actualizamos las horas de la disponibilidad
                    if (disp.getFechaHoraInicio().before(nuevaDisponibilidad.getFechaHoraInicio())) {
                        nuevaDisponibilidad.setFechaHoraInicio(disp.getFechaHoraInicio());
                    }
                    if (disp.getFechaHoraFin().after(nuevaDisponibilidad.getFechaHoraFin())) {
                        nuevaDisponibilidad.setFechaHoraFin(disp.getFechaHoraFin());
                    }

                    // Eliminamos la disponibilidad que se fusionó
                    Query borrarDisponibilidad = em.createNamedQuery("Disponibilidad.delete");
                    borrarDisponibilidad.setParameter("id", disp.getId());
                    borrarDisponibilidad.executeUpdate();
                }
            }

            em.persist(nuevaDisponibilidad);
    }



    @Override
    public List<TTurnoRolEmpleado> listarTurnosPorDia(String fecha) {
        System.out.println(fecha);
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
    public List<TTurnoRolEmpleado> listarTurnosAsignadosPorDia(String fecha) {
        EntityTransaction tx = null;
        try (EntityManager em = createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            List<TTurnoRolEmpleado> tturnos = new ArrayList<>();
            List<Turno> turnos = new ArrayList<>();
            Query q = em.createNamedQuery("Turno.findByDiaAsignados");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaConvertida = LocalDate.parse(fecha, formatter);
            q.setParameter("dia", fechaConvertida);
            turnos = q.getResultList();
            if (turnos != null) {
                for (Turno turn : turnos) {
                    tturnos.add(new TTurnoRolEmpleado(turn.toTransfer(), turn.getRol().toTransfer(),turn.getEmpleado().toTransfer()));
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

            long diferenciaEnMilisegundos = tTurno.getFechaHoraFin().getTime() - tTurno.getFechaHoraInicio().getTime();
            long diferenciaEnMinutos = diferenciaEnMilisegundos / (1000 * 60);
            if (diferenciaEnMinutos > 720) {
                trans.rollback();
                return -4; // Código de error para duración de turno excedida
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


    @Override
    public List<TTurnoRolEmpleado> getTurnosSemana(Timestamp semana) {
        EntityManager em = null;
        List<Turno> turnos = null;
        ArrayList<TTurnoRolEmpleado> listaTurnos = new ArrayList<>();
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
                TTurno turno = a.toTransfer();
                TRol rol = a.getRol().toTransfer();
                TEmpleado empleado = null;
                if(a.getEmpleado() != null){
                   empleado = a.getEmpleado().toTransfer();
                   listaTurnos.add(new TTurnoRolEmpleado(turno, rol, empleado));
                }
                else{
                    listaTurnos.add(new TTurnoRolEmpleado(turno, rol));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        } finally {
            if (em != null) {
                em.close();
            }
        }
        return listaTurnos;
    }

    public TTurnoRolEmpleado buscarTurnoAFicharEmpleado(TEmpleado empleado) {
        EntityTransaction tx = null;
        try(EntityManager em = createEntityManager()){
            tx = em.getTransaction();
            tx.begin();

            // Primero buscamos si hay un turno con registro abierto

            TypedQuery<Registro> qReg = em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class);
            qReg.setParameter("empleadoId", empleado.getId());
            qReg.setMaxResults(1);
            if (!qReg.getResultList().isEmpty()) {
                Turno turnoR = qReg.getResultList().get(0).getTurno();
                TTurnoRolEmpleado result =  new TTurnoRolEmpleado(turnoR.toTransfer(), turnoR.getRol().toTransfer());
                return result; // Ya hay un fichaje de entrada sin salida
            }

            // Sino buscamos el turno del que se registraria la entrada

            // 3. Calcular hora + 15 minutos
            Instant now = Calendar.getInstance().toInstant();
            Instant horaMas15 = now.plus(15, ChronoUnit.MINUTES);
            Timestamp tsHoraMas15 = Timestamp.from(horaMas15);

            // 4. Buscar turno que cubra hora + 15 minutos
            TypedQuery<Turno> qTurno = em.createQuery(
                    "SELECT t FROM Turno t " +
                            "WHERE t.empleado.id = :empId " +
                            "AND t.fechaHoraInicio <= :horaMas15 " +
                            "AND t.fechaHoraFin > :horaMas15",
                    Turno.class
            );
            qTurno.setParameter("empId", empleado.getId());
            qTurno.setParameter("horaMas15", tsHoraMas15);
            List<Turno> listaTurnos = qTurno.getResultList();
            if (listaTurnos.isEmpty()) {
                tx.rollback();
                return null; // No hay turno en el intervalo permitido
            }
            Turno turno = listaTurnos.get(0);

            if(turno.getRegistro()!=null){
                tx.rollback();
                return null; // No hay turno en el intervalo permitido
            }
            TTurnoRolEmpleado result =  new TTurnoRolEmpleado(turno.toTransfer(), turno.getRol().toTransfer());
            tx.commit();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null && tx.isActive()) {tx.rollback();}
            return null;
        }
    }

    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
