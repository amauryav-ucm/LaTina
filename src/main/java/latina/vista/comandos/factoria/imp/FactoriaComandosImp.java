package latina.vista.comandos.factoria.imp;

import latina.vista.Eventos;
import latina.vista.comandos.Comando;
import latina.vista.comandos.empleado.ObtenerEmpleadosDisponiblesInterfaz;
import latina.vista.comandos.turno.AsignarTurnoEmpleado;
import latina.vista.comandos.turno.VerTurnosSemanales;
import latina.vista.comandos.factoria.FactoriaComandos;
import latina.vista.comandos.rol.RegistrarRol;
import latina.vista.comandos.turno.ObtenerTurnosPorDiaInterfaz;

public class FactoriaComandosImp extends FactoriaComandos {

    @Override
    public Comando crearComando(Eventos evento) {
        Comando comando = null;

        switch (evento) {
            case REGISTRAR_ROL:
                comando = new RegistrarRol();
                break;
            case ASIGNAR_TURNO:
                comando = new AsignarTurnoEmpleado();
                break;
            case OBTENER_TURNOS_SEMANALES:
                comando = new VerTurnosSemanales();
                break;
            case OBTENER_TURNOS_POR_DIA:
                comando = new ObtenerTurnosPorDiaInterfaz();
                break;
            case OBTENER_EMPLEADOS_DISPONIBLES:
                comando = new ObtenerEmpleadosDisponiblesInterfaz();
                break;
            default:
                break;
        }

        return comando;
    }
}
