package latina.vista.comandos.usuario;

import latina.VistaPrincipal;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.usuario.TUsuario;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;

import java.sql.Timestamp;
import java.time.Instant;

public class ObtenerEstadoRegistro implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        JSObject jsData = (JSObject) datos;
        //Recupera el usuario del localStorage
        String usuario = jsData.getMember("usuario").toString();

        //Crea TUsuario
        TUsuario user = new TUsuario(usuario, "", false, true);
        //Devuelve el empleado desde el SA
        TEmpleado empleado = SAFactory.getInstance().createSAUsuario().conseguirEmpleado(user);

        int result = SAFactory.getInstance().createSARegistro().getEstadoRegistro(empleado);
        vista.getWebView().getEngine().executeScript(String.format("recibirEstadoFichaje(%d)", result));

        }
}
