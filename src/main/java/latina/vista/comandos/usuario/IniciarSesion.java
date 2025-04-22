package latina.vista.comandos.usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import javafx.concurrent.Worker;
import latina.VistaPrincipal;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.usuario.TUsuario;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;

import java.util.List;

public class IniciarSesion implements Comando {
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        try {
            String usuario = ((JSObject) object).getMember("usuario").toString();
            String contrasenya = ((JSObject) object).getMember("contrasenya").toString();

            TUsuario us = new TUsuario(usuario, contrasenya, false, false);

            int result = SAFactory.getInstance().createSAUsuario().iniciarSesion(us);

            switch (result) {
                case -1:
                case -2:
                case -3:
                    vista.getWebView().getEngine().executeScript("mostrarMensaje('El nombre de usuario o contraseña son incorrectos')");
                    break;
                case -4:
                    vista.getWebView().getEngine().executeScript("mostrarMensaje('Ha ocurrido un error')");
                    break;
                case 1:
                    TEmpleado emp = SAFactory.getInstance().createSAUsuario().conseguirEmpleado(us);
                    vista.getWebView().getEngine().executeScript("localStorage.setItem('usuario', '" + usuario + "');");
                    vista.getWebView().getEngine().executeScript("localStorage.setItem('idUsuario', '" + emp.getId() + "');");
                    vista.changeScene("ventanaPrincipalEmpleado.html");
                    break;
                case 2:
                    vista.changeScene("ventanaPrincipal.html");
                    break;

            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
