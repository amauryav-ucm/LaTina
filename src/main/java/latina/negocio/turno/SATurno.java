package latina.negocio.turno;

import java.sql.Timestamp;
import java.util.List;

public interface SATurno {

    public int asignarTurno(int idTurno, int idEmpleado);

    public List<Turno> getTurnosSemana(Timestamp semana);
}
