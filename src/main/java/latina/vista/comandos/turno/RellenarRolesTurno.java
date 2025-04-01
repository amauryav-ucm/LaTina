package latina.vista.comandos.turno;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.SARol;
import latina.negocio.rol.TRol;
import latina.vista.comandos.Comando;

import java.util.List;

public class RellenarRolesTurno implements Comando {

    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        SARol saRol = SAFactory.getInstance().createSARol();
        List<TRol> l_rol = saRol.buscarRoles(); //Obtiene la lista de empleados disponibles
        WebEngine webEngine = vista.getWebView().getEngine();
        if (!l_rol.isEmpty()) { //Si la lista no esta vacia
            for (TRol rol : l_rol) {
                String nombre = rol.getNombre();
                int id = rol.getId();
                webEngine.executeScript(String.format("cargarRolesAux('%s', %d)", nombre, id));
            }
            webEngine.executeScript("terminadoDeCargarRoles();");
        }
        else { //La lista esta vacia
            webEngine.executeScript("cargarRolesAux(null, null);");
        }
    }
}
