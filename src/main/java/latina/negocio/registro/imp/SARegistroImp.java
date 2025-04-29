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

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SARegistroImp implements SARegistro {


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

            if(turno.getRegistro()!=null){
                tx.rollback();
                return -3; // No hay turno en el intervalo permitido
            }

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

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1) Recuperar empleado
            TypedQuery<Empleado> qEmp = em.createNamedQuery("Empleado.findByDNI", Empleado.class);
            qEmp.setParameter("DNI", tEmpleado.getDNI());
            List<Empleado> listaEmp = qEmp.getResultList();
            if (listaEmp.isEmpty()) {
                tx.rollback();
                return -1; // Empleado no encontrado
            }
            Empleado empleado = listaEmp.get(0);

            // 2) Recuperar último registro abierto
            TypedQuery<Registro> qReg = em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class);
            qReg.setParameter("empleadoId", empleado.getId());
            qReg.setMaxResults(1);
            List<Registro> listaReg = qReg.getResultList();
            if (listaReg.isEmpty()) {
                tx.rollback();
                return -2; // No hay entrada activa para cerrar
            }
            Registro registro = listaReg.get(0);

            // 3) Cachear datos de turno y tiempos
            Turno turno = registro.getTurno();
            Timestamp inicioTurno = turno.getFechaHoraInicio();
            Timestamp finTurno = turno.getFechaHoraFin();
            long quinceMinMs = TimeUnit.MINUTES.toMillis(15);
            Timestamp limiteSalida = new Timestamp(finTurno.getTime() + quinceMinMs);

            // 4) Validaciones de tiempo
            if (hora.before(inicioTurno)) {
                tx.rollback();
                return -3; // No ha empezado el turno
            }
            if (!hora.before(limiteSalida)) {
                tx.rollback();
                return -4; // Se ha excedido el límite de tiempo
            }

            registro.sethFin(hora);

            double nHoras = calcular_nHoras(registro.gethInicio(), hora, inicioTurno, finTurno);
            registro.setnHoras(nHoras);

            // Calcular salario

            registro.setSalario(registro.getTurno().getRol().getSalario() * nHoras);


            tx.commit();
            return 1; // Salida fichada correctamente
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return -5; // Error general
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public int getEstadoRegistro(TEmpleado empleado) {
        EntityTransaction tx = null;
        try(EntityManager em = createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            TypedQuery<Registro> qReg = em.createNamedQuery("Registro.findLatestOpenByEmpleado", Registro.class);
            qReg.setParameter("empleadoId", empleado.getId());
            qReg.setMaxResults(1);
            if (qReg.getResultList().isEmpty()) {
                tx.commit();
                // No tiene registros abiertos
                return 1;
            }
            Registro reg = qReg.getResultList().get(0);
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            Timestamp limite = new Timestamp(reg.getTurno().getFechaHoraFin().getTime() + TimeUnit.MINUTES.toMillis(15));
            if(ahora.after(limite)) {
                reg.sethFin(limite);
                Turno turno = reg.getTurno();
                double nHoras = calcular_nHoras(reg.gethInicio(), limite, turno.getFechaHoraInicio(), turno.getFechaHoraFin());
                reg.setnHoras(nHoras);
                reg.setSalario(turno.getRol().getSalario() * nHoras);
                tx.commit();
                // Se ha cerrado el registro automaticamente
                return 2;
            }
            else {
                tx.commit();
                // Tiene un registro abierto
                return 3;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null && tx.isActive()) tx.rollback();
            return -4;
        }
    }

    private double calcular_nHoras(Timestamp llegada, Timestamp salida, Timestamp inicio, Timestamp fin){
        // Ajustar dentro del turno
        long inicioMs = Math.max(llegada.getTime(), inicio.getTime());
        long finMs    = Math.min(salida.getTime(),   fin.getTime());
        long diffMs   = finMs - inicioMs;
        if (diffMs <= 0) return 0.0;

        long halfHourMs    = TimeUnit.MINUTES.toMillis(30);
        long thresholdMs   = TimeUnit.MINUTES.toMillis(15);
        long mediasCompletas = diffMs / halfHourMs;
        long restoMs       = diffMs % halfHourMs;
        if (restoMs >= thresholdMs) {
            mediasCompletas++;
        }
        return mediasCompletas * 0.5;
    }

    protected EntityManager createEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
