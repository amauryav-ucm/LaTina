package latina.vista.comandos.turno;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import latina.VistaPrincipal;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import latina.negocio.factoria.SAFactory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.scene.web.WebEngine;
import org.w3c.dom.Document;


public class RegistrarTurno implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {

            JSObject jsData = (JSObject) datos;

            int rolId = Integer.parseInt(jsData.getMember("rol").toString());
            String fechaInicio = jsData.getMember("fechaInicio").toString();
            String fechaFin = jsData.getMember("fechaFin").toString();
            String horaInicio = jsData.getMember("horaInicio").toString();
            String horaFin = jsData.getMember("horaFin").toString();
            // Define formatters
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a", Locale.ENGLISH);

            String fechaHoraIniStr = fechaInicio + " " + horaInicio;
            String fechaHoraFinStr = fechaFin + " " + horaFin;

            // Convert to java.sql.Timestamp
            Timestamp fechaHoraInicio = Timestamp.valueOf(LocalDateTime.parse(fechaHoraIniStr, dateTimeFormatter));
            Timestamp fechaHoraFin = Timestamp.valueOf(LocalDateTime.parse(fechaHoraFinStr, dateTimeFormatter));


            TTurno t = new TTurno();
            t.setIdRol(rolId);
            t.setFechaHoraInicio(fechaHoraInicio);
            t.setFechaHoraFin(fechaHoraFin);

            SATurno saTurno = SAFactory.getInstance().createSATurno();
            int result = saTurno.altaTurno(t);

            String mensaje = "";
            boolean error = false;

            if (result >= 0) mensaje = "El turno se ha registrado correctamente";
             else
            {
                if (result == -1) mensaje = "No se encontró el rol especificado";
                else if (result == -2) mensaje = "La fecha de fin debe ser posterior a la fecha de inicio";
                else if (result == -3) mensaje = "La fecha de inicio no puede ser anterior a la hora actual";
                else if (result == -4) mensaje = "El turno excede las 12 horas de duración permitidas";
                else if (result == -5) mensaje = "Error al registrar el turno";
                else mensaje = "Error desconocido";
                error = true;
            }


            WebEngine webEngine = vista.getWebView().getEngine();
            String finalMensaje = mensaje;

            boolean finalError = error;
            webEngine.documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> obs, Document oldDoc, Document newDoc) {
                    if (newDoc != null) {
                        if (finalError) {
                            webEngine.executeScript(String.format(
                                    "mostrarError('%s', '%s', '%s', '%s', '%s')",
                                    finalMensaje, fechaInicio, fechaFin, horaInicio, horaFin
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
            // Mostrar mensaje de error en caso de excepción
            WebEngine webEngine = vista.getWebView().getEngine();
            webEngine.executeScript("mostrarMensaje('Error al procesar la solicitud de turno')");
        }
    }
}
