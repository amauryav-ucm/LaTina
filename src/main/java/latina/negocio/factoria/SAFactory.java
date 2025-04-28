package latina.negocio.factoria;

import latina.negocio.disponibilidad.SADisponibilidad;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.factoria.imp.SAFactoryImp;
import latina.negocio.registro.SARegistro;
import latina.negocio.rol.SARol;
import latina.negocio.turno.SATurno;
import latina.negocio.usuario.SAUsuario;

public abstract class SAFactory {

    private static SAFactory instance;

    public static SAFactory getInstance(){
        if(instance == null) instance = new SAFactoryImp();
        return instance;
    }

    public abstract SARol createSARol();

    public abstract SADisponibilidad createSADisponibilidad();

    public abstract SATurno createSATurno();

    public abstract SAEmpleado createSAEmpleado();

    public abstract SAUsuario createSAUsuario();

    public abstract SARegistro createSARegistro();
}
