package latina.negocio.factoria.imp;

import latina.negocio.dispoinibilidad.SADisponibilidad;
import latina.negocio.factoria.SAFactory;
import latina.negocio.rol.SARol;
import latina.negocio.rol.imp.SARolImp;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.imp.SATurnoImp;

public class SAFactoryImp extends SAFactory{

    @Override
    public SARol createSARol() {
        return new SARolImp();
    }

    @Override
    public SADisponibilidad createSADisponibilidad() {
        return null;
    }

    @Override
    public SATurno createSATurno() {
        return new SATurnoImp();
    }

}
