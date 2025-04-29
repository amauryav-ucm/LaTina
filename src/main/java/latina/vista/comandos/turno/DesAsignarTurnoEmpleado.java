package latina.vista.comandos.turno;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class DesAsignarTurnoEmpleado implements  Comando{
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {
            JSObject jsData = (JSObject) datos;


            List<Object> listaDatos = new ArrayList<>();
            int length = (int) jsData.getMember("length");

            for (int i = 0; i < length; i++) {
                listaDatos.add(jsData.getMember(String.valueOf(i)));
            }

            System.out.println("Lista de datos: " + listaDatos);


            int idTurno = Integer.parseInt(listaDatos.get(0).toString().trim());


            String descripcionEmpleado = listaDatos.get(1).toString();
            String dni = extraerDniDeDescripcion(descripcionEmpleado);

            if (dni == null || dni.isEmpty()) {
                mostrarMensajeWeb(vista, "No se pudo extraer el DNI del empleado.");
                return;
            }


            SAEmpleado saEmpleado = SAFactory.getInstance().createSAEmpleado();
            TEmpleado empleado = saEmpleado.readByDNI(dni);

            if (empleado == null) {
                mostrarMensajeWeb(vista, "No se encontró al empleado con DNI: " + dni);
                return;
            }

            int idEmpleado = empleado.getId();


            SATurno saTurno = SAFactory.getInstance().createSATurno();
            int resultado = saTurno.desasignarTurno(idTurno, idEmpleado);


            String mensaje;
            switch (resultado) {
                case 1:
                    mensaje = "Turno desasignado correctamente.";
                    break;
                case -3:
                    mensaje = "El turno no está asignado a este empleado.";
                    break;
                case -4:
                    mensaje = "El turno o el empleado no existen.";
                    break;
                case -5:
                    mensaje = "Error al desasignar el turno. Inténtalo de nuevo.";
                    break;
                default:
                    mensaje = "Ha ocurrido un error desconocido.";
            }

            mostrarMensajeWeb(vista, mensaje);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Extrae el DNI de una cadena como "Juan Pérez Gómez | DNI: 10101010J"
    private String extraerDniDeDescripcion(String texto) {
        if (texto != null && texto.contains("DNI:")) {
            String[] partes = texto.split("DNI:");
            return partes[1].trim();
        }
        return null;
    }

    // Muestra un mensaje en el navegador WebView
    private void mostrarMensajeWeb(VistaPrincipal vista, String mensaje) {
        WebEngine webEngine = vista.getWebView().getEngine();
        webEngine.documentProperty().addListener(new ChangeListener<Document>() {
            @Override
            public void changed(ObservableValue<? extends Document> obs, Document oldDoc, Document newDoc) {
                if (newDoc != null) {
                    webEngine.executeScript(String.format("mostrarMensaje('%s')", mensaje));
                    webEngine.documentProperty().removeListener(this);
                }
            }
        });
    }
}
