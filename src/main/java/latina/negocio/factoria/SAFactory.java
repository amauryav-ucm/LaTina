package latina.negocio.factoria;

import latina.negocio.factoria.imp.SAFactoryImp;
import latina.negocio.rol.SARol;

public abstract class SAFactory {

    private static SAFactory instance;

    public static SAFactory getInstance(){
        if(instance == null) instance = new SAFactoryImp();
        return instance;
    }

    public abstract SARol createSARol();
}
