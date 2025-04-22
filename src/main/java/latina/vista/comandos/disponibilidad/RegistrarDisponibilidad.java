package latina.vista.comandos.disponibilidad;

import jakarta.persistence.EntityManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.disponibilidad.SADisponibilidad;
import latina.negocio.disponibilidad.TDisponibilidad;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RegistrarDisponibilidad implements Comando {

    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {

        try {
            
            JSObject jsData = (JSObject) datos;

            int empleadoId = Integer.parseInt(jsData.getMember("empleado").toString());
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


            TDisponibilidad t = new TDisponibilidad();
            t.setEmpleadoId(empleadoId);
            t.setFechaInicio(fechaHoraInicio);
            t.setFechaFin(fechaHoraFin);

            SADisponibilidad saDisponibilidad = SAFactory.getInstance().createSADisponibilidad();
            int result = saDisponibilidad.altaDisponibilidad(t);
            String mensaje = "";
            boolean error = false;
            if (result >= 0)
            {
                mensaje = "Disponibilidad registrada correctamente para el empleado " + obtenerNombreyApellidoEmpleadoPorId(empleadoId);
            }
            else
            {
                error = true;
                if (result == -1) mensaje = "No se encontró el empleado con el ID especificado";
                else if (result == -2) mensaje = "La fecha de fin debe ser posterior a la fecha de inicio";
                else if (result == -3) mensaje = "El empleado tiene un turno asignado dentro de ese horario";
                else if (result == -4) mensaje = "La disponibilidad debe comenzar más tarde que la hora actual";
                else if (result == -5) mensaje = "Error al registrar la disponibilidad";
                else if (result == -6) mensaje = "La disponibilidad excede las 24 horas de duración permitidas";
                else mensaje = "Error desconocido";
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
                                    finalMensaje, fechaInicio, horaInicio, fechaFin, horaFin
                            ));
                        }
                        else
                        {
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
            webEngine.executeScript("mostrarMensaje('Error al procesar la solicitud de disponibilidad')");
        }
    }

    private String obtenerNombreyApellidoEmpleadoPorId(int idEmpleado) {
        EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
        try {
            Empleado empleado = em.find(Empleado.class, idEmpleado);
            if (empleado != null) {
                return empleado.getNombre() + " " + empleado.getApellidos();
            } else {
                return "";
            }
        } finally {
            em.close();
        }
    }
}