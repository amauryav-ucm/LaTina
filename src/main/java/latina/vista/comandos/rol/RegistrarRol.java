package latina.vista.comandos.rol;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.SARol;
import latina.negocio.rol.TRol;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

public class RegistrarRol implements Comando {

    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {

        try {
            String nombreRol = ((JSObject)datos).getMember("nombre").toString();
            Double salarioRol =  Double.parseDouble(((JSObject)datos).getMember("salario").toString());
            TRol t = new TRol(nombreRol, salarioRol, true);

            SARol rol = SAFactory.getInstance().createSARol();
            int result = rol.altaRol(t);
            String mensaje = "";
            boolean error = false;
            String camposError = "[]";

            if (result >= 0) mensaje = "El rol " + nombreRol + " se ha registrado correctamente";
            else
            {
                if (result == -1) mensaje = "Ya existe un rol con el nombre introducido";
                else if (result == -2)
                {
                    mensaje = "El salario debe ser un número positivo";
                    camposError = "['wage']";
                }
                else if (result == -3)
                {
                    mensaje = "El rol solo puede contener letras";
                    camposError = "['name']";
                }
                else if (result == -4) mensaje = "Ha ocurrido un error al registrar el rol";
                else mensaje = "Error desconocido";
                error = true;
            }


            WebEngine webEngine = vista.getWebView().getEngine();
            String finalMensaje = mensaje;
            boolean finalError= error;
            String finalCamposError = camposError;


            // Se añade un listener para mostrar el mensaje cuando el documento esté listo
            webEngine.documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> obs, Document oldDoc, Document newDoc) {
                    if (newDoc != null) {
                        if (finalError) {
                            webEngine.executeScript(String.format(
                                    "mostrarError('%s', '%s', '%s', %s)",
                                    finalMensaje, nombreRol, salarioRol, finalCamposError
                            ));
                        } else {
                            webEngine.executeScript(String.format("mostrarMensaje('%s')", finalMensaje));
                        }                        webEngine.documentProperty().removeListener(this);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
