package latina.negocio.empleado;

import latina.negocio.turno.Turno;

import java.util.List;

public interface SAEmpleado {
    List<Empleado> getEmpleadosDisponibles(Turno turno);
}
