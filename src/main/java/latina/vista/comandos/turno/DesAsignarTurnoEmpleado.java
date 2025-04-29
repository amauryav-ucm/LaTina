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

public class DesAsignarTurnoEmpleado implements  Comando{
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        /*JSObject jsData = (JSObject) object;

        // Convertir el JSObject a una lista en Java
        List<Object> listaDatos = new ArrayList<>();
        int length = (int) jsData.getMember("length"); // Obtener la longitud del array

        for (int i = 0; i < length; i++) {
            listaDatos.add(jsData.getMember(String.valueOf(i))); // Extraer cada valor
        }

        System.out.println("Lista de datos: " + listaDatos);*/
    }
}
