package latina.integracion.emfc.imp;

import latina.integracion.emfc.EMFContainer;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EMFContainerImp extends EMFContainer {

    private EntityManagerFactory emf;

    public EMFContainerImp() {
        Map<String, String> persistenceMap = new HashMap<String, String>();
        File configFile = new File("config.properties");
        System.out.println("Archivo config.properties: " + configFile.getAbsolutePath());
        if(configFile.exists()){
            System.out.println("Cargando propiedades desde el archivo config.properties");
            Properties properties = new Properties();
            try {
                properties.load(configFile.toURI().toURL().openStream());
                persistenceMap.put("jakarta.persistence.jdbc.url", properties.getProperty("db.url"));
                persistenceMap.put("jakarta.persistence.jdbc.user", properties.getProperty("db.user"));
                persistenceMap.put("jakarta.persistence.jdbc.password", properties.getProperty("db.password"));
                emf = Persistence.createEntityManagerFactory("LaTinaCode", persistenceMap);
                System.out.println(emf.getProperties().get("jakarta.persistence.jdbc.url"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            emf = Persistence.createEntityManagerFactory("LaTinaCode");
        }
    }

    @Override
    public EntityManagerFactory getEMF() {
        return emf;
    }
}
