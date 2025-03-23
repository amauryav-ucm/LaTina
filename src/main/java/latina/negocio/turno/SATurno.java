package latina.negocio.turno;
import java.util.List;
public interface SATurno {

    public int asignarTurno(int idTurno, int idEmpleado);
    public List<TTurno> listarTurnosPorDia(String fecha);
}
