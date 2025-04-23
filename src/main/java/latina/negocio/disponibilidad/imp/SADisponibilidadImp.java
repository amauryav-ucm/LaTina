package latina.negocio.disponibilidad.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.disponibilidad.SADisponibilidad;
import latina.negocio.disponibilidad.TDisponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.rol.Rol;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
import java.util.List;

public class SADisponibilidadImp implements SADisponibilidad {

    @Override
    public int altaDisponibilidad(TDisponibilidad tDisponibilidad) {
        EntityManager em = null;
        EntityTransaction trans = null;
        int id = 0;
        try {
            em = crearEntityManager();
            trans = em.getTransaction();
            trans.begin();
            Empleado emp = em.find(Empleado.class, tDisponibilidad.getEmpleadoId());
            Timestamp ahora = new Timestamp(System.currentTimeMillis());

            long diferenciaEnMilisegundos = tDisponibilidad.getFechaFin().getTime() - tDisponibilidad.getFechaInicio().getTime();
            long diferenciaEnMinutos = diferenciaEnMilisegundos / (1000 * 60);
            if(emp == null) {
                trans.rollback();
                return -1;
            }else if(tDisponibilidad.getFechaFin().equals(tDisponibilidad.getFechaInicio())
                    || tDisponibilidad.getFechaFin().before(tDisponibilidad.getFechaInicio())) {
                trans.rollback();
                return -2;
            }else if(tDisponibilidad.getFechaInicio().before(ahora)){
                trans.rollback();
                return -4;
            }else if (diferenciaEnMinutos > 1440) {
                trans.rollback();
                return -6; // Código de error para duración de turno excedida
            }
            else{
                //Para que no se pueda registrar una disponibilidad si el empleado tiene un turno asignado
                Empleado empleado = em.find(Empleado.class, tDisponibilidad.getEmpleadoId());
                List<Turno> listaTurnos = empleado.getTurno();
                for (Turno turnoEmp : listaTurnos) {
                    if (turnoEmp.solapaCon(tDisponibilidad.getFechaInicio(),tDisponibilidad.getFechaFin())) {
                        trans.rollback();
                        return -3;
                    }
                }
                Disponibilidad disp = new Disponibilidad(emp, tDisponibilidad);
                em.persist(disp);
                trans.commit();
                id = disp.getId();
                combinarDisponibilidad(id);
            }
        }catch (Exception e) {
            if (trans != null && trans.isActive())
                trans.rollback();
            e.printStackTrace();
            return -5;
        } finally {
            if (em != null)
                em.close();
        }
        return id;
    }

    @Override
    public void combinarDisponibilidad(int idDisponibilidad) {
        EntityTransaction tx = null;
        try(EntityManager em = crearEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            // Esta es la disponibilidad nueva que hemos insertado
            // Asumo que se llama a esta funcion despues de haber persistido la nueva disponibilidad
            Disponibilidad nuevaDisponibilidad = em.find(Disponibilidad.class, idDisponibilidad);
            // Buscamos todas las disponibilidades del empleado
            List<Disponibilidad> lista = nuevaDisponibilidad.getEmpleado().getDisponibilidad();
            for(Disponibilidad disp : lista){
                if(nuevaDisponibilidad.seDebeUnirCon(disp)) {
                    // Actualizamos las horas de la disponibilidad
                    // Si tiene un tiempo de inicio anterior se actualiza
                    if(disp.getFechaHoraInicio().before(nuevaDisponibilidad.getFechaHoraInicio()))
                        nuevaDisponibilidad.setFechaHoraInicio(disp.getFechaHoraInicio());
                    // Si tiene un tiempo de fin posterior se actualiza
                    if(disp.getFechaHoraFin().after(nuevaDisponibilidad.getFechaHoraFin()))
                        nuevaDisponibilidad.setFechaHoraFin(disp.getFechaHoraFin());
                    // Vamos a borrar esta disponibilidad ya que se ha fusionado con la nueva
                    Query borrarDisponibilidad = em.createNamedQuery("Disponibilidad.delete");
                    borrarDisponibilidad.setParameter("id", disp.getId());
                    borrarDisponibilidad.executeUpdate();
                }
            }
            tx.commit();
        } catch (Exception e) {
            // Lanzamos una excepcion para que la atrape la funcion que llamo a combinarDisponibilidad
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    public EntityManager crearEntityManager(){
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }

}
