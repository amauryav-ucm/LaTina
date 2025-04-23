package latina.vista.comandos.empleado;

import jakarta.persistence.EntityManager;
import latina.VistaPrincipal;
import latina.integracion.emfc.EMFContainer;
import latina.negocio.empleado.Empleado;
import latina.vista.comandos.Comando;
import netscape.javascript.JSObject;

public class ActualizarEstadoFichaje implements Comando {
    @Override
    public void ejecutar(Object object, VistaPrincipal vista) {
        try {
            JSObject datos = (JSObject) object;

            // Extract the employee ID
            int empleadoId = Integer.parseInt(datos.getMember("empleadoId").toString());

            // Get the haFichadoEntrada value
            boolean haFichadoEntrada = Boolean.parseBoolean(datos.getMember("haFichadoEntrada").toString());

            // Update the employee record in the database
            EntityManager em = EMFContainer.getInstance().getEMF().createEntityManager();
            em.getTransaction().begin();

            Empleado empleado = em.find(Empleado.class, empleadoId);
            if (empleado != null) {
                empleado.setHaFichadoEntrada(haFichadoEntrada);
                em.merge(empleado);
            }

            em.getTransaction().commit();
            em.close();

            // Send confirmation back to UI if needed
            vista.getWebView().getEngine().executeScript(
                    "localStorage.setItem('haFichadoEntrada', '" + haFichadoEntrada + "');"
            );

        } catch (Exception e) {
            e.printStackTrace();
            // Handle error and notify the UI
            vista.getWebView().getEngine().executeScript(
                    "mostrarMensaje('Error al registrar el fichaje');"
            );
        }
    }
}