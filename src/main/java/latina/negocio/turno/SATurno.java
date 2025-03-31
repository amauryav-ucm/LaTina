package latina.negocio.turno;
import java.util.List;
public interface SATurno {

    public int asignarTurno(int idTurno, int idEmpleado);
    public List<TTurnoRolEmpleado> listarTurnosPorDia(String fecha);
    public int altaTurno(TTurno tTurno);
}
