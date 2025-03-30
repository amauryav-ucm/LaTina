package latina.vista.comandos.empleado;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

public class RegistrarEmpleado implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {
            TEmpleado tEmpleado = new TEmpleado(
                    ((JSObject)datos).getMember("dni").toString(),
                    ((JSObject)datos).getMember("nombre").toString(),
                    ((JSObject)datos).getMember("apellidos").toString(),
                    ((JSObject)datos).getMember("email").toString(),
                    ((JSObject)datos).getMember("telefono").toString(),
                    true);
            SAEmpleado sae = SAFactory.getInstance().createSAEmpleado();
            int result = sae.altaEmpleado(tEmpleado);
            String mensaje = "";
            if (result >= 0) mensaje = "Se ha registrado el empleado correctamente con ID: " + result;
            else if (result == -1) mensaje = "Ya existe un empleado con el DNI introducido";
            else if (result == -2) mensaje = "Ya existe un empleado con el correo introducido";
            else if (result == -3) mensaje = "Formato del DNI erróneo";
            else if (result == -4) mensaje = "El campo teléfono solo permite números de 9 dígitos";
            else if (result == -5) mensaje = "El campo nombre solo permite letras y espacios";
            else if (result == -6) mensaje = "El campo apellidos solo permite letras y espacios";
            else if (result == -7) mensaje = "El campo correo debe tener un formato válido, por ejemplo: usuario@ejemplo.com";
            else mensaje = "Error desconocido";

            WebEngine webEngine = vista.getWebView().getEngine();
            String finalMensaje = mensaje;
            // Se añade un listener para mostrar el mensaje cuando el documento esté listo
            webEngine.documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> obs, Document oldDoc, Document newDoc) {
                    if (newDoc != null) {
                        webEngine.executeScript(String.format("mostrarMensaje('%s')", finalMensaje));
                        webEngine.documentProperty().removeListener(this);
                    }
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
