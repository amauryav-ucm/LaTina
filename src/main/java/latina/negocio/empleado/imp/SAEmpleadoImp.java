package latina.negocio.empleado.imp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.dispoinibilidad.Disponibilidad;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.turno.Turno;

import java.sql.Timestamp;
import java.util.*;

public class SAEmpleadoImp implements SAEmpleado {
    @Override
    public List<TEmpleado> getEmpleadosDisponibles(int idTurno) {
        EntityManager em = null;
        List<TEmpleado> listaEmpleados = new ArrayList<>();

        try {
            em = crearEntityManager();
            Turno turno = em.find(Turno.class, idTurno);
            if (turno == null) {

                return listaEmpleados;
            }



            // Buscar TODAS las disponibilidades que INTERSECTEN con el turno
            Query q = em.createQuery("SELECT d FROM Disponibilidad d WHERE d.fechaFin > :fechaHoraIni AND d.fechaInicio < :fechaHoraFin");
            q.setParameter("fechaHoraIni", turno.getFechaHoraInicio());
            q.setParameter("fechaHoraFin", turno.getFechaHoraFin());
            List<Disponibilidad> disponibilidades = q.getResultList();



            //  Agrupar disponibilidades por empleado
            Map<Empleado, List<Disponibilidad>> disponibilidadPorEmpleado = new HashMap<>();
            for (Disponibilidad disp : disponibilidades) {
                disponibilidadPorEmpleado
                        .computeIfAbsent(disp.getEmpleado(), k -> new ArrayList<>())
                        .add(disp);
            }

            //  Verificar si las disponibilidades de cada empleado cubren el turno completo
            for (Map.Entry<Empleado, List<Disponibilidad>> entry : disponibilidadPorEmpleado.entrySet()) {
                Empleado empleado = entry.getKey();
                List<Disponibilidad> dispEmpleado = entry.getValue();

                // Ordenamos disponibilidades por fecha de inicio
                dispEmpleado.sort(Comparator.comparing(Disponibilidad::getFechaInicio));




                // Algoritmo para verificar cobertura completa
                Timestamp cubiertoHasta = turno.getFechaHoraInicio();
                boolean cubreCompleto = false;

                for (Disponibilidad disp : dispEmpleado) {
                    if (disp.getFechaInicio().after(cubiertoHasta)) {

                        break; // Hay un hueco sin cobertura
                    }
                    if (disp.getFechaFin().after(cubiertoHasta)) {
                        cubiertoHasta = disp.getFechaFin(); // Extendemos la cobertura

                    }
                    if (!cubiertoHasta.before(turno.getFechaHoraFin())) {
                        cubreCompleto = true;
                        break;
                    }
                }

                // Si el empleado cubre el turno, lo agregamos a la lista
                if (cubreCompleto) {
                    TEmpleado emp = new TEmpleado(empleado.getDNI(), empleado.getNombre(), empleado.getApellidos(),
                            empleado.getTelefono(), empleado.getCorreo(), empleado.isActivo());
                    emp.setId(empleado.getId());
                    listaEmpleados.add(emp);

                } else {
                   // System.out.println(" El empleado NO cubre el turno.");
                }
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




    protected EntityManager crearEntityManager() {
        return EMFContainer.getInstance().getEMF().createEntityManager();
    }
}
