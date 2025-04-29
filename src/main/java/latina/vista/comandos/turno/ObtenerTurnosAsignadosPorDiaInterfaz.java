package latina.vista.comandos.turno;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurnoRolEmpleado;
import latina.vista.comandos.Comando;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ObtenerTurnosAsignadosPorDiaInterfaz implements Comando {
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        SATurno sr = SAFactory.getInstance().createSATurno();
        List<TTurnoRolEmpleado> tturnos = sr.listarTurnosAsignadosPorDia((String) object);
        WebEngine webEngine = vista.getWebView().getEngine();

        if (!tturnos.isEmpty()) {
            for (TTurnoRolEmpleado turno : tturnos) {
                String parametroTurno = "Turno " + turno.getIdTurno() + " | "
                        + formatearHoras(turno.getFechaHoraInicio()) + " → "
                        + formatearHoras(turno.getFechaHoraFin()) + " | "
                        + turno.getNombreRol();

                String parametroEmpleado = turno.getNombreEmpleado() + " " + turno.getApellidosEmpleado() + " | DNI: " + turno.getDNIEmpleado();
                webEngine.executeScript(String.format("cargarTurnosAux('%s', %d, '%s')", parametroTurno, turno.getIdTurno(), parametroEmpleado));
            }
            webEngine.executeScript("terminadoDeCargar();");
        } else {
            webEngine.executeScript("cargarTurnosAux(null, null);");
        }
    }

    private String formatearHoras(Timestamp fecha) {
        LocalDateTime localDateTime = fecha.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return localDateTime.format(formatter);
    }
}
