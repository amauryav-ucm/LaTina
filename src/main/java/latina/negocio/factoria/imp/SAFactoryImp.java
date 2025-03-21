package latina.negocio.factoria.imp;

import latina.negocio.dispoinibilidad.SADisponibilidad;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.SARol;
import latina.negocio.rol.imp.SARolImp;

public class SAFactoryImp extends SAFactory{

    @Override
    public SARol createSARol() {
        return new SARolImp();
    }

    @Override
    public SADisponibilidad createSADisponibilidad() {
        return null;
    }

}
