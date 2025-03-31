package latina.vista.comandos.disponibilidad;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.vista.comandos.Comando;

import java.util.List;

public class RellenarEmpleadosRegistrarDisponibildad implements Comando {
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {

        SAEmpleado saEmpleado = SAFactory.getInstance().createSAEmpleado();
        List<TEmpleado> l_empleados = saEmpleado.buscarEmpleados(); //Obtiene la lista de empleados disponibles
        WebEngine webEngine = vista.getWebView().getEngine();
        if (!l_empleados.isEmpty()) { //Si la lista no esta vacia
            for (TEmpleado empleado : l_empleados) {
                String nombre = empleado.getNombre() + " " + empleado.getApellidos() + " | DNI: " + empleado.getDNI();
                int id = empleado.getId();
                webEngine.executeScript(String.format("cargarEmpleadosAux('%s', %d)", nombre, id));
            }
            webEngine.executeScript("terminadoDeCargarEmpleados();");
        }
        else { //La lista esta vacia
            webEngine.executeScript("cargarEmpleadosAux(null, null);");
        }
    }
}
