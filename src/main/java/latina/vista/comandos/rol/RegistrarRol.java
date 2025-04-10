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
            TRol t = new TRol(((JSObject)datos).getMember("nombre").toString(),
                    Double.parseDouble(((JSObject)datos).getMember("salario").toString()), true);

            SARol rol = SAFactory.getInstance().createSARol();
            int result = rol.altaRol(t);
            String name = t.getNombre();
            String mensaje = "";

            if (result >= 0) mensaje = "El rol " + name + " se ha registrado correctamente";
            else if (result == -1) mensaje = "Ya existe un rol con el nombre introducido";
            else if (result == -2) mensaje = "El salario debe ser un número positivo";
            else if (result == -3) mensaje = "El rol debe estar en mayúsculas y sin números";
            else if (result == -4) mensaje = "Ha ocurrido un error al registrar el rol";
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
