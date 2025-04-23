package latina.negocio.usuario;

import latina.negocio.empleado.TEmpleado;

public interface SAUsuario {
    int altaUsuario(TUsuario us);

    /**
     * Recibe el nombre de usuario y la contraseña a traves de un TUsuario
     * Devuelve un numero positivo indicando el tipo de usuario si las credenciales son correctas
     * o un numero negativo indicando el error si lo hay.
     * <br>
     * 1: Usuario empleado <br>
     * 2: Usuario gerente
     *
     * @returns El numero indicando el resultado
     */
    public int iniciarSesion(TUsuario us);

    public TEmpleado conseguirEmpleado(TUsuario us);

    public void inicializarGerente();

}
