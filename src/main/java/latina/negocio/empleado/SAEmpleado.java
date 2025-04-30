package latina.negocio.empleado;

import java.util.List;

public interface SAEmpleado {
    List<TEmpleado> getEmpleadosDisponibles(int idTurno);
    int altaEmpleado(TEmpleado emp);

    String generarContrasenya();
    public TEmpleado readByDNI(String dni);
    /// @return Una lista de empleados, una lista vacía si no existen, o null si se produce una excepción
    public List<TEmpleado> buscarEmpleados();

}
