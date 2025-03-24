package latina.vista.comandos.Turno;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.vista.comandos.Comando;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class ObtenerTurnosPorDiaInterfaz implements Comando {
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        SATurno sr = SAFactory.getInstance().createSATurno();
        List<TTurno> tturnos = sr.listarTurnosPorDia((String) object);
        WebEngine webEngine = vista.getWebView().getEngine();
                // Llamar a la función para cada turno individualmente
        if(!tturnos.isEmpty())
        {
            for (TTurno turno : tturnos) {

                String parametroTurno = "Turno " + turno.getIdTurno() + " | " + formatearHoras(turno.getFechaHoraInicio()) + " → " + formatearHoras(turno.getFechaHoraFin()) + " | Rol " + turno.getIdRol() ;
                webEngine.executeScript(String.format("cargarTurnosAux('%s', %d)", parametroTurno, turno.getIdTurno()));
            }
            webEngine.executeScript("terminadoDeCargar();");
        }
        else
            webEngine.executeScript("cargarTurnosAux(null, null);");
    }

    private String formatearHoras(Timestamp fecha)
    {
        LocalDateTime localDateTime = fecha.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Formatear la fecha a String
        return localDateTime.format(formatter);
    }
}

