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

            WebEngine webEngine = vista.getWebView().getEngine();

            int result;
            String finalMensaje = "";
            if(tipo.equals("entrada")){
                result = SAFactory.getInstance().createSARegistro().ficharEntrada(empleado, t);

                if (result == 1) finalMensaje = "Entrada registrada correctamente";
                else if (result == -1) finalMensaje = "El empleado no existe";
                else if (result == -2) finalMensaje = "Ya se ha fichado la entrada";
                else if (result == -3) finalMensaje = "Solo se puede fichar con 15 minutos de antelación del turno";
                else finalMensaje = "Error desconocido";

            }
            else if(tipo.equals("salida")) {
                result = SAFactory.getInstance().createSARegistro().ficharSalida(empleado, t);
                if (result == 1) finalMensaje = "Salida registrada correctamente";
                else if (result == -1) finalMensaje = "El empleado no existe";
                else if (result == -2) finalMensaje = "Ya se ha fichado la salida";
                else if (result == -3) finalMensaje = "Solo se puede fichar una vez comience el turno";
                else if (result == -4) finalMensaje = "Solo se puede fichar hasta 15 minutos después de acabar el turno";
                else finalMensaje = "Error desconocido";
            }

            webEngine.executeScript(String.format("mostrarMensaje('%s')", finalMensaje));


        } catch (Exception e) {
            e.printStackTrace();
            //Muestra mensaje de error en caso de excepcion
            WebEngine webEngine = vista.getWebView().getEngine();
            webEngine.executeScript("mostrarMensaje('Error desconocido')");
        }
    }
}
