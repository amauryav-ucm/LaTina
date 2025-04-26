package latina.negocio.registro.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.registro.Registro;
import latina.negocio.registro.SARegistro;
import latina.negocio.registro.TRegistro;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class SARegistroImp implements SARegistro {
    //10101010J', 1, 'Pérez Gómez', 'correoo@example.com', 'Juan', '123456788');
    //TEmpleado emple = new TEmpleado("10101010J","Juan", "Pérez Gómez","correoo@example.com","123456788", true , false);
    //Timestamp now = new Timestamp(System.currentTimeMillis());
    //int a = ficharEntrada(emple, now);

    @Override
    public int ficharEntrada(TEmpleado tEmpleado, Timestamp hora) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Recuperar el empleado por DNI
            TypedQuery<Empleado> qEmp = em.createNamedQuery("Empleado.findByDNI", Empleado.class);
            qEmp.setParameter("DNI", tEmpleado.getDNI());
            List<Empleado> listaEmp = qEmp.getResultList();
            if (listaEmp.isEmpty()) {
                tx.rollback();
                return -1; // Empleado no existe
            }
            Empleado empleado = listaEmp.get(0);

            // 2. Verificar que no tenga ya un registro abierto (hFin == null)
            TypedQuery<Registro> qReg = em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class);
            qReg.setParameter("empleadoId", empleado.getId());
            qReg.setMaxResults(1);
            if (!qReg.getResultList().isEmpty()) {
                tx.rollback();
                return -2; // Ya hay un fichaje de entrada sin salida
            }

            // 3. Calcular hora + 15 minutos
            Instant now = hora.toInstant();
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
                return -3; // No hay turno en el intervalo permitido
            }
            Turno turno = listaTurnos.get(0);

            // 6. Crear y persistir el nuevo registro de entrada
            Registro reg = new Registro(empleado, hora, 0);
            reg.setTurno(turno);
            em.persist(reg);

            tx.commit();
            return 1; // Fichaje de entrada correcto

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return -4; // Error general
        } finally {
            if (em != null) em.close();
        }
    }


    public int ficharSalida(TEmpleado tEmpleado, Timestamp hora) {

        //Está mas o menos hecho
        /*
        EntityManager em = null;

        EntityTransaction trans = null;

        try {
            em = createEntityManager();
            trans = em.getTransaction();
            trans.begin();

            // Buscar al empleado
            Query q = em.createNamedQuery("Empleado.findByDNI");
            q.setParameter("DNI", tEmpleado.getDNI());
            List<Empleado> empleados = q.getResultList();

            if (empleados.isEmpty()) {
                trans.rollback();
                return -1; // Empleado no encontrado
            }

            Empleado empleado = empleados.get(0);

            // Buscar el último registro sin hFin
            Query q2 = em.createNamedQuery("Registro.findByEmpleado");
            q2.setParameter("empleadoId", empleado.getId());
            q2.setMaxResults(1);
            List<Registro> registros = q2.getResultList();

            if (registros.isEmpty()) {
                trans.rollback();
                return -2; // No hay entrada activa para cerrar
            }

            Registro registro = registros.get(0);
            registro.sethFin(hora);

            // Calcular nHoras
            long diffMillis = hora.getTime() - registro.gethInicio().getTime();
            int nHoras = (int) (diffMillis / (1000 * 60 * 60)); // redondea hacia abajo
            registro.setnHoras(nHoras);

            // Calcular salario si hay turno asociado, si no dejar en 0
            if (registro.getTurno() != null) {
                registro.setSalario(registro.getTurno().getRol().getSalario() * nHoras);
            } else {
                registro.setSalario(0);
            }

            em.merge(registro);
            trans.commit();
            return 1; // Salida fichada correctamente

        } catch (Exception e) {
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            e.printStackTrace();
            return -4; // Error general
        } finally {
            if (em != null) {
                em.close();
            }
        }
        */
        return -1; //Se quita esto al descomentarlo todo
    }


    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
