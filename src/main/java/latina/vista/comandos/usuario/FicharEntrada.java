package latina.vista.comandos.usuario;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.usuario.TUsuario;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;

import java.sql.Timestamp;

public class FicharEntrada implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {
            JSObject jsData = (JSObject) datos;
            //Recupera el usuario del localStorage
            String usuario = jsData.getMember("usuario").toString();
            //Crea TUsuario
            TUsuario user = new TUsuario(usuario, "", false, true);
            //Devuelve el empleado desde el SA
            TEmpleado empleado = SAFactory.getInstance().createSAUsuario().conseguirEmpleado(user);

            Timestamp entrada = new Timestamp(System.currentTimeMillis());

            //Llama al SARegistro para fichar la entrada
            int result = SAFactory.getInstance().createSARegistro().ficharEntrada(empleado, entrada);
            WebEngine webEngine = vista.getWebView().getEngine();
            if (result == 1) {
                webEngine.executeScript("mostrarMensaje('Entrada registrada correctamente')");
            } else {
                webEngine.executeScript("mostrarMensaje('Error al registrar la entrada')");
            }

        } catch (Exception e) {
            e.printStackTrace();
            //Muestra mensaje de error en caso de excepcion
            WebEngine webEngine = vista.getWebView().getEngine();
            webEngine.executeScript("mostrarMensaje('Error desconocido')");
        }
    }
}
