package latina.vista.controlador.imp;

import latina.VistaPrincipal;
import latina.vista.comandos.factoria.FactoriaComandos;
import latina.vista.controlador.Controlador;
import latina.vista.Eventos;
import latina.vista.comandos.Comando;

public class ControladorImp extends Controlador {

    private final VistaPrincipal vistaP;

    public ControladorImp(VistaPrincipal vista)
    {
        this.vistaP = vista;
    }
    @Override
    public void accion(Eventos evento, Object param) {
        Comando comando = FactoriaComandos.getInstance().crearComando(evento);
        if (comando != null)
            comando.ejecutar(param, this.vistaP);
    }

}