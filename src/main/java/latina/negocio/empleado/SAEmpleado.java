package latina.negocio.empleado;

import java.util.List;

public interface SAEmpleado {
    List<TEmpleado> getEmpleadosDisponibles(int idTurno);
    int altaEmpleado(TEmpleado emp);
}
