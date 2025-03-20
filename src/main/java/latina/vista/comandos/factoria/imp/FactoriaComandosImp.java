package latina.vista.comandos.factoria.imp;

import latina.vista.Eventos;
import latina.vista.comandos.Comando;
import latina.vista.comandos.factoria.FactoriaComandos;
import latina.vista.comandos.rol.RegistrarRol;

public class FactoriaComandosImp extends FactoriaComandos {

    @Override
    public Comando crearComando(Eventos evento) {
        Comando comando = null;

        switch (evento) {
            case REGISTRAR_ROL:
                comando = new RegistrarRol();
                break;

            default:
                break;
        }

        return comando;
    }
}
