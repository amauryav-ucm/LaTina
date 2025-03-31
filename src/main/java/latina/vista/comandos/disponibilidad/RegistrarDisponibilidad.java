package latina.vista.comandos.disponibilidad;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.disponibilidad.SADisponibilidad;
import latina.negocio.disponibilidad.TDisponibilidad;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import java.sql.Timestamp;

public class RegistrarDisponibilidad implements Comando {

    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {

        try {
            
            JSObject jsData = (JSObject) datos;
            int empleadoId = Integer.parseInt(jsData.getMember("empleadoId").toString());
            Timestamp fechaInicio = Timestamp.valueOf(jsData.getMember("fechaInicio").toString());
            Timestamp fechaFin = Timestamp.valueOf(jsData.getMember("fechaFin").toString());

            TDisponibilidad t = new TDisponibilidad();
            t.setEmpleadoId(empleadoId);
            t.setFechaInicio(fechaInicio);
            t.setFechaFin(fechaFin);

            SADisponibilidad saDisponibilidad = SAFactory.getInstance().createSADisponibilidad();
            int result = saDisponibilidad.altaDisponibilidad(t);
            String mensaje = "";

            if (result >= 0) mensaje = "Disponibilidad registrada correctamente con ID: " + result;
            else if (result == -1) mensaje = "No se encontró el empleado con el ID especificado";
            else if (result == -2) mensaje = "La fecha de fin debe ser posterior a la fecha de inicio";
            else if (result == -3) mensaje = "Error al registrar la disponibilidad";
            else mensaje = "Error desconocido";

            WebEngine webEngine = vista.getWebView().getEngine();
            String finalMensaje = mensaje;

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
            // Mostrar mensaje de error en caso de excepción
            WebEngine webEngine = vista.getWebView().getEngine();
            webEngine.executeScript("mostrarMensaje('Error al procesar la solicitud de disponibilidad')");
        }
    }
}