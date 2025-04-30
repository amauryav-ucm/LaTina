package latina.negocio.turno;

import latina.negocio.empleado.TEmpleado;

import java.sql.Timestamp;
import java.util.List;

import java.util.List;
public interface SATurno {

    public int asignarTurno(int idTurno, int idEmpleado);

    public int desasignarTurno(int idTurno, int idEmpleado);

    public List<TTurnoRolEmpleado> getTurnosSemana(Timestamp semana);
    public List<TTurnoRolEmpleado> listarTurnosPorDia(String fecha);
    public List<TTurnoRolEmpleado> listarTurnosAsignadosPorDia(String fecha);
    public int altaTurno(TTurno tTurno);

    public TTurnoRolEmpleado buscarTurnoAFicharEmpleado(TEmpleado empleado);
}
