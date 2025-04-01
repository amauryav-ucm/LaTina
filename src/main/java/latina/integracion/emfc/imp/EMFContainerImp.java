package latina.integracion.emfc.imp;

import latina.integracion.emfc.EMFContainer;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EMFContainerImp extends EMFContainer implements AutoCloseable {

    private EntityManagerFactory emf;

    public EMFContainerImp() {
        emf = Persistence.createEntityManagerFactory("H2Test");
    }

    @Override
    public EntityManagerFactory getEMF() {
        return emf;
    }

    @Override
    public void close() throws Exception {
        emf.close();
    }
}
