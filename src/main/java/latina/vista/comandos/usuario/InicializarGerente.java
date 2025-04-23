package latina.vista.comandos.usuario;

import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.vista.comandos.Comando;

public class InicializarGerente implements Comando {
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        SAFactory.getInstance().createSAUsuario().inicializarGerente();
    }
}
