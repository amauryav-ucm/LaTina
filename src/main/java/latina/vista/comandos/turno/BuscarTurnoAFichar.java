package latina.vista.comandos.turno;

import latina.VistaPrincipal;
import latina.negocio.empleado.TEmpleado;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.TTurnoRolEmpleado;
import latina.negocio.usuario.TUsuario;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BuscarTurnoAFichar implements Comando {
    @Override
    public void ejecutar(Object datos, VistaPrincipal vista) {
        JSObject jsData = (JSObject) datos;
        //Recupera el usuario del localStorage
        String usuario = jsData.getMember("usuario").toString();

        //Crea TUsuario
        TUsuario user = new TUsuario(usuario, "", false, true);
        //Devuelve el empleado desde el SA
        TEmpleado empleado = SAFactory.getInstance().createSAUsuario().conseguirEmpleado(user);
        TTurnoRolEmpleado turno = SAFactory.getInstance().createSATurno().buscarTurnoAFicharEmpleado(empleado);

        String parametroTurno = "No se ha encontrado un turno para fichar";
        String textoRol = "";
        if (turno != null) {
            parametroTurno = formatearHoras(turno.getFechaHoraInicio()) + " → " + formatearHoras(turno.getFechaHoraFin());
            textoRol = turno.getNombreRol();
        }
        vista.getWebView().getEngine().executeScript(String.format("recibirTurnoAFichar({hayTurno: %d, fechaHora: '%s', rol: '%s'})", turno==null?0:1, parametroTurno, textoRol));

    }

    private String formatearHoras(Timestamp fecha) {
        LocalDateTime localDateTime = fecha.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Formatear la fecha a String
        return localDateTime.format(formatter);
    }
}
