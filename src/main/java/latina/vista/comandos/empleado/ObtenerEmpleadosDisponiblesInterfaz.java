package latina.vista.comandos.empleado;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.vista.comandos.Comando;

import java.util.List;

public class ObtenerEmpleadosDisponiblesInterfaz implements Comando {
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        SAEmpleado se = SAFactory.getInstance().createSAEmpleado();
        int idTurno = Integer.valueOf((String)object);
        List<TEmpleado> templeados = se.getEmpleadosDisponibles(idTurno);
        WebEngine webEngine = vista.getWebView().getEngine();
        if(!templeados.isEmpty())
        {
            for (TEmpleado empleado : templeados) {
                // Aquí pasas cada empleado por separado
                String parametroEmpleado = empleado.getNombre() + " " + empleado.getApellidos() + " | DNI: " + empleado.getDNI();
                webEngine.executeScript(String.format("cargarEmpleadosAux('%s', %d)", parametroEmpleado, empleado.getId()));
            }
            webEngine.executeScript("terminadoDeCargar2();");
        }
        else
            webEngine.executeScript("cargarEmpleadosAux(null, null);");
    }
}
