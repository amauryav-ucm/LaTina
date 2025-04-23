package latina.vista.comandos.factoria.imp;

import latina.vista.Eventos;
import latina.vista.comandos.Comando;
import latina.vista.comandos.disponibilidad.RegistrarDisponibilidad;
import latina.vista.comandos.disponibilidad.RellenarEmpleadosRegistrarDisponibildad;
import latina.vista.comandos.empleado.ObtenerEmpleadosDisponiblesInterfaz;
import latina.vista.comandos.empleado.RegistrarEmpleado;
import latina.vista.comandos.turno.AsignarTurnoEmpleado;
import latina.vista.comandos.turno.VerTurnosSemanales;
import latina.vista.comandos.factoria.FactoriaComandos;
import latina.vista.comandos.rol.RegistrarRol;
import latina.vista.comandos.turno.ObtenerTurnosPorDiaInterfaz;
import latina.vista.comandos.turno.RegistrarTurno;
import latina.vista.comandos.turno.RellenarRolesTurno;
import latina.vista.comandos.usuario.InicializarGerente;
import latina.vista.comandos.usuario.IniciarSesion;

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
            case REGISTRAR_EMPLEADO:
                comando = new RegistrarEmpleado();
                break;
            case OBTENER_TODOS_LOS_EMPLEADOS:
                comando = new RellenarEmpleadosRegistrarDisponibildad();
                break;
            case REGISTRAR_DISPONIBILIDAD:
                comando = new RegistrarDisponibilidad();
                break;
            case OBTENER_TODOS_LOS_ROLES:
                comando = new RellenarRolesTurno();
                break;
            case REGISTRAR_TURNO:
                comando = new RegistrarTurno();
                break;
            case INICIAR_SESION:
                comando = new IniciarSesion();
                break;
            case INICIALIZAR_GERENTE:
                comando = new InicializarGerente();
                break;
            default:
                break;
        }

        return comando;
    }
}
