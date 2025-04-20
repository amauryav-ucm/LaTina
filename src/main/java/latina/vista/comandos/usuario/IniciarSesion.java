package latina.vista.comandos.usuario;

import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.usuario.TUsuario;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;

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
                    vista.getWebView().getEngine().executeScript("mostrarMensaje('Vaya... La interfaz del empleado aún no ha sido creada. Vuelve mas tarde')");
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
