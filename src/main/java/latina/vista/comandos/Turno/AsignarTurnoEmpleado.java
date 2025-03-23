package latina.vista.comandos.Turno;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.SARol;
import latina.negocio.rol.TRol;
import latina.negocio.turno.SATurno;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

public class AsignarTurnoEmpleado implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        try {
            //Transfer ... = parsear el JSONObject al transfer

            SATurno satur = SAFactory.getInstance().createSATurno();
            int result = satur.asignarTurno(1, 1);

            String mensaje = "No implementado aun";

            if (result >= 0) mensaje = "Se ha asignado el turno correctamente";
          //  else if (result == -1) mensaje = "Ya existe un rol con el nombre introducido";
            else if (result == -2) mensaje = "El empleado no está disponible para el turno";
            else if (result == -3) mensaje = "El empleado ya tiene uno o más turnos que solapan con el nuevo";
            else if (result == -4) mensaje = "Ha ocurrido un error al asignar el turno";
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
