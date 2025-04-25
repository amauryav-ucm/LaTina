package latina.negocio.registro.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.registro.Registro;
import latina.negocio.registro.SARegistro;
import latina.negocio.registro.TRegistro;

import java.sql.Timestamp;
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
        EntityTransaction trans = null;
        try {
            em = createEntityManager();
            trans = em.getTransaction();
            trans.begin();


            Query q = em.createNamedQuery("Empleado.findByDNI");
            q.setParameter("DNI", tEmpleado.getDNI());
            List<Empleado> emp = q.getResultList();

            if(emp.isEmpty()){
                trans.rollback();
                return -1; // EL EMPLEADO NO EXISTE
            }

            Empleado empleado = emp.get(0);

            Query q2 = em.createNamedQuery("Registro.findByEmpleado");
            q2.setParameter("empleadoId", empleado.getId());
            q2.setMaxResults(1);
            List<Registro> registros = q2.getResultList();


             if (!registros.isEmpty()) {
                trans.rollback();
                return -2; // Ya hay un fichaje de entrada
            }else{
                Registro reg = new Registro(emp.get(0), hora, 0);
                em.persist(reg);
            }

            trans.commit();

            return 1; //FUE CORRECTAMENTE

        } catch (Exception e) {
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            e.printStackTrace();
            return -4; // Código de error para excepción general
        } finally {
            if (em != null) {
                em.close();
            }
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
