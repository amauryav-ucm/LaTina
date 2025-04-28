package latina.negocio.factoria.imp;

import latina.negocio.disponibilidad.SADisponibilidad;
import latina.negocio.disponibilidad.imp.SADisponibilidadImp;
import latina.negocio.empleado.SAEmpleado;
import latina.negocio.empleado.imp.SAEmpleadoImp;
import latina.negocio.factoria.SAFactory;
import latina.negocio.registro.SARegistro;
import latina.negocio.registro.imp.SARegistroImp;
import latina.negocio.rol.SARol;
import latina.negocio.rol.imp.SARolImp;
import latina.negocio.turno.SATurno;
import latina.negocio.turno.imp.SATurnoImp;
import latina.negocio.usuario.SAUsuario;
import latina.negocio.usuario.imp.SAUsuarioImp;

public class SAFactoryImp extends SAFactory{

    @Override
    public SARol createSARol() {
        return new SARolImp();
    }

    @Override
    public SADisponibilidad createSADisponibilidad() {
        return new SADisponibilidadImp();
    }

    @Override
    public SATurno createSATurno() {
        return new SATurnoImp();
    }

    @Override
    public SAEmpleado createSAEmpleado() {
        return new SAEmpleadoImp();
    }

    @Override
    public SAUsuario createSAUsuario() { return new SAUsuarioImp(); }

    @Override
    public SARegistro createSARegistro() {
        return new SARegistroImp();
    }

}
