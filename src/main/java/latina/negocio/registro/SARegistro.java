package latina.negocio.registro;

import latina.negocio.empleado.TEmpleado;

import java.sql.Timestamp;
import java.util.List;

public interface SARegistro {
    public int ficharEntrada(TEmpleado tEmpleado, Timestamp hora);//con la hora y empleado busco el turno;
    public int ficharSalida(TEmpleado tEmpleado, Timestamp hora);//con la hora y empleado busco el turno;
    public int getEstadoRegistro(TEmpleado empleado);

}
