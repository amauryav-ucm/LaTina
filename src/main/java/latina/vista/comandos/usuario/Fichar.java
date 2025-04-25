package latina.vista.comandos.usuario;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.usuario.TUsuario;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;

import java.sql.Timestamp;
import java.time.Instant;

public class Fichar implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {
            JSObject jsData = (JSObject) datos;
            //Recupera el usuario del localStorage
            String usuario = jsData.getMember("usuario").toString();
            String fechaIso = jsData.getMember("fecha").toString();
            Instant instant = Instant.parse(fechaIso);
            Timestamp t = Timestamp.from(instant);
            String tipo = jsData.getMember("tipo").toString();

            //Crea TUsuario
            TUsuario user = new TUsuario(usuario, "", false, true);
            //Devuelve el empleado desde el SA
            TEmpleado empleado = SAFactory.getInstance().createSAUsuario().conseguirEmpleado(user);

            //Llama al SARegistro para fichar la entrada

            int result = -1;
            if(tipo.equals("entrada")){
                result = SAFactory.getInstance().createSARegistro().ficharEntrada(empleado, t);
                WebEngine webEngine = vista.getWebView().getEngine();

                if (result == 1) {
                    webEngine.executeScript("mostrarMensaje('Entrada registrada correctamente')");
                }
                else if(result == -2){
                    webEngine.executeScript("mostrarMensaje('Ya se ha fichado la entrada')");
                }
                else {
                    webEngine.executeScript("mostrarMensaje('Error al registrar la entrada')");
                }
            }
            else if(tipo.equals("salida")) {
                result = SAFactory.getInstance().createSARegistro().ficharSalida(empleado, t);
            }


        } catch (Exception e) {
            e.printStackTrace();
            //Muestra mensaje de error en caso de excepcion
            WebEngine webEngine = vista.getWebView().getEngine();
            webEngine.executeScript("mostrarMensaje('Error desconocido')");
        }
    }
}
