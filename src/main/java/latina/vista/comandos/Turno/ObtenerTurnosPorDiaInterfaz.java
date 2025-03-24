package latina.vista.comandos.Turno;

import jakarta.persistence.EntityManager;
import javafx.scene.web.WebEngine;
import latina.VistaPrincipal;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.Rol;
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
            try {
                EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();

                for (TTurno turno : tturnos) {
                    Rol rol = em.find(Rol.class, turno.getIdRol());
                    String parametroTurno = "Turno " + turno.getIdTurno() + " | " + formatearHoras(turno.getFechaHoraInicio()) + " → " + formatearHoras(turno.getFechaHoraFin()) + " | " + rol.getNombre();
                    webEngine.executeScript(String.format("cargarTurnosAux('%s', %d)", parametroTurno, turno.getIdTurno()));
                }
            }catch(Exception e)
            {
                e.printStackTrace();
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

