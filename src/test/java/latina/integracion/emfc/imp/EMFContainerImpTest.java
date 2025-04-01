package latina.integracion.emfc.imp;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import latina.integracion.emfc.EMFContainer;

import static org.junit.jupiter.api.Assertions.*;

public class EMFContainerImpTest extends EMFContainer implements AutoCloseable {

    private EntityManagerFactory emf;

    public EMFContainerImpTest() {
        emf = Persistence.createEntityManagerFactory("LaTinaCodeTest");
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