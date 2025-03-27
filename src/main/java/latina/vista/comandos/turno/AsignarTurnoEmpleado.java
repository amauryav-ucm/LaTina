package latina.vista.comandos.turno;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class AsignarTurnoEmpleado implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {
            JSObject jsData = (JSObject) datos;

            // Convertir el JSObject a una lista en Java
            List<Object> listaDatos = new ArrayList<>();
            int length = (int) jsData.getMember("length"); // Obtener la longitud del array

            for (int i = 0; i < length; i++) {
                listaDatos.add(jsData.getMember(String.valueOf(i))); // Extraer cada valor
            }

            System.out.println("Lista de datos: " + listaDatos);

            // Ahora puedes usar los valores correctamente

            SATurno satur = SAFactory.getInstance().createSATurno();
            String recibido = String.valueOf(datos);

            // Separar los valores por la coma
            String[] partes = recibido.split(",");

            // Convertir a enteros
            int turn= Integer.parseInt(partes[0].trim());
            int employee = Integer.parseInt(partes[1].trim());
            int result = satur.asignarTurno(turn, employee);

            String mensaje = "No implementado aun";

            if (result >= 0) mensaje = "Se ha asignado el turno correctamente";
           //  else if (result == -1) mensaje = "Ya existe un rol con el nombre introducido";
           // else if (result == -2) mensaje = "El empleado no está disponible para el turno";
            else if (result == -3) mensaje = "El empleado ya tiene uno o más turnos que solapan con el nuevo";
            else if (result == -4) mensaje = "El empleado o el turno no existe";
            else if (result == -5) mensaje = "La persistencia ha fallado";
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
