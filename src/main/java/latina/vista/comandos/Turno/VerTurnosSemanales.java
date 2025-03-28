package latina.vista.comandos.Turno;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.negocio.turno.Turno;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VerTurnosSemanales implements Comando {

    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {

            String fechaStr = ((JSObject) datos).getMember("fecha").toString();


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime fechaLocal = LocalDateTime.parse(fechaStr, formatter);
            Timestamp semana = Timestamp.valueOf(fechaLocal);


            SATurno saTurno = SAFactory.getInstance().createSATurno();
            List<Turno> turnos = saTurno.getTurnosSemana(semana);


            StringBuilder jsonTurnos = new StringBuilder("[");
            for (Turno turno : turnos) {
                if (jsonTurnos.length() > 1) {
                    jsonTurnos.append(",");
                }
                jsonTurnos.append(String.format(
                        "{\"id\": %d, \"fechaHoraInicio\": \"%s\", \"fechaHoraFin\": \"%s\", \"empleado\": \"%s\"}",
                        turno.getId(),
                        turno.getFechaHoraInicio(),
                        turno.getFechaHoraFin(),
                        turno.getEmpleado() != null ? turno.getEmpleado().getNombre() : "Sin asignar"
                ));
            }
            jsonTurnos.append("]");

            WebEngine webEngine = vista.getWebView().getEngine();
            String finalJson = jsonTurnos.toString();

            // Se añade un listener para mostrar los turnos cuando el documento esté listo
            webEngine.documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> obs, Document oldDoc, Document newDoc) {
                    if (newDoc != null) {
                        webEngine.executeScript(String.format("mostrarTurnosSemanales(%s)", finalJson));
                        webEngine.documentProperty().removeListener(this);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            WebEngine webEngine = vista.getWebView().getEngine();
            webEngine.executeScript("mostrarMensaje('Error al obtener los turnos semanales')");
        }
    }
}