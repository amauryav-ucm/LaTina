package latina.vista.comandos.Turno;

import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.negocio.factoria.SAFactory;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.TTurno;
import latina.vista.comandos.Comando;

import java.util.List;


public class ObtenerTurnosPorDiaInterfaz implements Comando {
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        SATurno sr = SAFactory.getInstance().createSATurno();
        List<TTurno> tturnos = sr.listarTurnosPorDia((String) object);
        WebEngine webEngine = vista.getWebView().getEngine();
                // Llamar a la función para cada turno individualmente
        if(tturnos.size() > 0)
        {
            for (TTurno turno : tturnos) {
                // Aquí pasas cada turno por separado, no el JSON completo
                String parametroTurno = "Inicio: " + turno.getFechaHoraInicio() + " Fin: " + turno.getFechaHoraFin();
                webEngine.executeScript(String.format("cargarTurnosAux('%s')", parametroTurno));
            }
        }
        else
            webEngine.executeScript("cargarTurnosAux(null);");
    }
}