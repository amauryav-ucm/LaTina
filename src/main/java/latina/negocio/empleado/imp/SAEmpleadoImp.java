package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.disponibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.turno.Turno;

import java.security.SecureRandom;
import java.util.*;

public class SAEmpleadoImp implements SAEmpleado {
    @Override
    public List<TEmpleado> getEmpleadosDisponibles(int idTurno) {
        EntityManager em = null;
        List<TEmpleado> listaEmpleados = new ArrayList<>();
        try {
            em = crearEntityManager();
            //Obtiene los empleados de la tabla Disponibilidad que cubran la fecha y horas completas

            Query q = em.createNamedQuery("Disponibilidad.findByRangoFecha");
            Turno turno = em.find(Turno.class, idTurno);
            q.setParameter("fechaHoraIni", turno.getFechaHoraInicio());
            q.setParameter("fechaHoraFin", turno.getFechaHoraFin());
            List<Disponibilidad> disponibilidades = q.getResultList();

            for (Disponibilidad dispAux : disponibilidades)
            {
                listaEmpleados.add(dispAux.getEmpleado().toTransfer());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return listaEmpleados;
    }

    @Override
    public int altaEmpleado(TEmpleado emp) {
        EntityManager em = null;
        EntityTransaction trans = null;
        int id =0;
        try {
            em = crearEntityManager();
            trans = em.getTransaction();
            trans.begin();

            Query q = em.createNamedQuery("Empleado.findByDNI");
            q.setParameter("DNI", emp.getDNI());
            List<Empleado> empleados = q.getResultList();
            if(!empleados.isEmpty()){
                trans.rollback();
                return -1; //YA HAY EMPLEADEOS CON ESE DNI
            }

            Query q2 = em.createNamedQuery("Empleado.findByCorreo");
            q2.setParameter("correo", emp.getCorreo());
            List<Empleado> empleados2 = q2.getResultList();
            if(!empleados2.isEmpty()){
                trans.rollback();
                return -2; //YA HAY EMPLEADEOS CON ESE CORREO ELECTRONICO
            }
            if(!emp.getDNI().matches("\\d{8}[A-Z]")){
                trans.rollback();
                return -3; // DNI EN FORMATO INCORRECTO
            }
            if(!emp.getTelefono().matches("\\d{9}")){
                trans.rollback();
                return -4; // NUM TELEFONO EN FORMATO INCORRECTO
            }
            if (!emp.getNombre().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+"))
            {
                trans.rollback();
                return -5;//Solo se permiten letras(incluyendo ñ y tildes) y espacios
            }
            if (!emp.getApellidos().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+"))
            {
                trans.rollback();
                return -6;//Solo se permiten letras(incluyendo ñ y tildes) y espacios
            }
            if (!emp.getCorreo().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                trans.rollback();
                return -7; // CORREO EN FORMATO INCORRECTO
            }
            Empleado employee = new Empleado(emp);
            em.persist(employee);
            trans.commit();
            id = employee.getId();
        } catch (Exception e) {
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
            return -8;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return id;
    }

    @Override
    public String generarContrasenya() {

        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*";
        StringBuilder contrasena = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < 8; i++) {
            int index = secureRandom.nextInt(caracteres.length());
            contrasena.append(caracteres.charAt(index));
        }

        return contrasena.toString();
    }

    /// @return Una lista de empleados, una lista vacía si no existen, o null si se produce una excepción
    public List<TEmpleado> buscarEmpleados(){
        EntityTransaction tx = null;
        try (EntityManager em = crearEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            Query queryBuscarEmpleados = em.createNamedQuery("Empleado.findAll");
            List<Empleado> empleados = (List<Empleado>) queryBuscarEmpleados.getResultList();
            List<TEmpleado> resultado = new ArrayList<>();
            for (Empleado e : empleados)
                resultado.add(e.toTransfer());
            tx.commit();
            return resultado;
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null && tx.isActive())
                tx.rollback();
            return null;
        }
    }
    @Override
    public TEmpleado readByDNI(String dni) {
        EntityManager em = null;
        TEmpleado empleadoTransfer = null;
        try {
            em = crearEntityManager();

            Query query = em.createNamedQuery("Empleado.findByDNI");
            query.setParameter("DNI", dni);
            List<Empleado> empleados = query.getResultList();

            if (empleados != null && !empleados.isEmpty()) {
                Empleado empleado = empleados.get(0);
                empleadoTransfer = empleado.toTransfer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return empleadoTransfer;
    }


    protected EntityManager crearEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
