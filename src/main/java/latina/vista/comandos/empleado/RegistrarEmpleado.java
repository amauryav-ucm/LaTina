package latina.vista.comandos.empleado;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.usuario.SAUsuario;
import latina.negocio.usuario.TUsuario;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

public class RegistrarEmpleado implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {
            String dni = ((JSObject) datos).getMember("dni").toString();
            String nombre = ((JSObject) datos).getMember("nombre").toString();
            String apellidos = ((JSObject) datos).getMember("apellidos").toString();
            String email = ((JSObject) datos).getMember("email").toString();
            String telefono = ((JSObject) datos).getMember("telefono").toString();

            TEmpleado tEmpleado = new TEmpleado(dni, nombre, apellidos, email, telefono, true);
            SAEmpleado sae = SAFactory.getInstance().createSAEmpleado();
            int result = sae.altaEmpleado(tEmpleado);
            String contrasenya = sae.generarContrasenya();
            String mensaje = "";
            boolean error = false;

            // Lista de errores en formato JSON para pasarlo a JavaScript
            String camposError = "[]";

            if (result >= 0) {
                SAUsuario sau = SAFactory.getInstance().createSAUsuario();
                TUsuario us = new TUsuario(dni, contrasenya, false, true);
                int resulado2 = sau.altaUsuario(us);
                if(resulado2 > 0)
                    mensaje = "Se ha registrado el empleado correctamente con contraseña: " + contrasenya;
                else
                    mensaje = "Ha habido un problema creando el usuario del empleado"; //esperemos que esto nunca pase
            } else {
                error = true;
                if (result == -1) mensaje = "Ya existe un empleado con el DNI introducido";
                else if (result == -2) mensaje = "Ya existe un empleado con el correo introducido";
                else if (result == -3) { mensaje = "Formato del DNI erróneo"; camposError = "['dni']"; }
                else if (result == -4) { mensaje = "El campo teléfono solo permite números de 9 dígitos"; camposError = "['telefono']"; }
                else if (result == -5) { mensaje = "El campo nombre solo permite letras y espacios"; camposError = "['nombre']"; }
                else if (result == -6) { mensaje = "El campo apellidos solo permite letras y espacios"; camposError = "['apellidos']"; }
                else if (result == -7) { mensaje = "El campo correo debe tener un formato válido, por ejemplo: usuario@ejemplo.com"; camposError = "['email']"; }
                else mensaje = "Error desconocido";
            }

            WebEngine webEngine = vista.getWebView().getEngine();
            String finalMensaje = mensaje;
            boolean finalError = error;
            String finalCamposError = camposError;

            webEngine.documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> obs, Document oldDoc, Document newDoc) {
                    if (newDoc != null) {
                        if (finalError) {
                            webEngine.executeScript(String.format(
                                    "mostrarError('%s', '%s', '%s', '%s', '%s', '%s', %s)",
                                    finalMensaje, dni, nombre, apellidos, email, telefono, finalCamposError
                            ));
                        } else {
                            webEngine.executeScript(String.format("mostrarMensaje('%s')", finalMensaje));
                        }
                        webEngine.documentProperty().removeListener(this);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
