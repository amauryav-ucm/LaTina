package latina.negocio.registro;

import latina.negocio.empleado.TEmpleado;

import java.sql.Timestamp;

public interface SARegistro {
    public int ficharEntrada(TEmpleado tEmpleado, Timestamp hora);//con la hora y empleado busco el turno;
}
