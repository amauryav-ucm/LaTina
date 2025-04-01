package latina.vista.comandos.turno;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.vista.comandos.Comando;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class VerTurnosSemanales implements Comando {

    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {

            //String fechaStr = ((JSObject) datos).getMember("fecha").toString();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime lunesLocalDate = LocalDateTime.parse((String) datos, formatter);
            Timestamp lunes = Timestamp.valueOf(lunesLocalDate);

            LocalDateTime domingoLocalDate = lunesLocalDate.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).toLocalDate().atTime(23, 59, 59);
            // Convierte de nuevo a Timestamp

            SATurno saTurno = SAFactory.getInstance().createSATurno();
            List<TTurno> turnos = saTurno.getTurnosSemana(lunes);
            List<TTurno> turnosDivididos = new ArrayList<>();
            //Si un turno dura varios dias dividirlo en varios turnos
            //y las horas que sean fuera de esta semana no cogerlas
            for (TTurno turno : turnos) {
                LocalDateTime inicio = turno.getFechaHoraInicio().toLocalDateTime();
                LocalDateTime fin = turno.getFechaHoraFin().toLocalDateTime();

                while (!inicio.toLocalDate().isAfter(fin.toLocalDate())) {
                    LocalDateTime nuevoFin;

                    if (inicio.isBefore(lunesLocalDate)) {
                        inicio = lunesLocalDate;
                    }

                    // Ajustar fin si está después del domingo
                    if (fin.isAfter(domingoLocalDate)) {
                        fin = domingoLocalDate;
                    }

                    if (inicio.toLocalDate().isEqual(fin.toLocalDate())) {
                        // Último segmento del turno
                        nuevoFin = fin;
                    } else {
                        // Parte del turno que se extiende hasta las 23:59 del día actual
                        nuevoFin = inicio.toLocalDate().atTime(23, 59, 59);
                    }

                    // Crear y añadir nuevo turno fragmentado
                    turnosDivididos.add(new TTurno(
                            turno.getIdTurno(),
                            turno.getIdRol(),
                            Timestamp.valueOf(inicio),
                            Timestamp.valueOf(nuevoFin),
                            turno.getIdEmpleado()
                    ));

                    // Avanzar al siguiente día a las 00:00
                    inicio = inicio.toLocalDate().plusDays(1).atStartOfDay();
                }
            }

            WebEngine webEngine = vista.getWebView().getEngine();
            for (TTurno turno : turnosDivididos) {
                String turnoJson = "{"
                        + "\"id\": " + turno.getIdTurno() + ","
                        + "\"fechaHoraInicio\": \"" + turno.getFechaHoraInicio().toString() + "\","
                        + "\"fechaHoraFin\": \"" + turno.getFechaHoraFin().toString() + "\","
                        + "\"dniEmpleado\": \"" + turno.getIdEmpleado() + "\","
                        + "\"rol\": \"" + turno.getIdRol() + "\""
                        + "}";
                webEngine.executeScript(String.format("cargarTurnosAux('%s', %d)", turnoJson, turno.getIdTurno()));
            }
            //String finalJson = jsonTurnos.toString();

            // Se añade un listener para mostrar los turnos cuando el documento esté listo
            /*webEngine.documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> obs, Document oldDoc, Document newDoc) {
                    if (newDoc != null) {
                        //webEngine.executeScript(String.format("mostrarTurnosSemanales(%s)", finalJson));
                        webEngine.documentProperty().removeListener(this);
                    }
                }
            });*/

        } catch (Exception e) {
            e.printStackTrace();

            WebEngine webEngine = vista.getWebView().getEngine();
            webEngine.executeScript("mostrarMensaje('Error al obtener los turnos semanales')");
        }
    }
}