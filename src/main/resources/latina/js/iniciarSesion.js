const _inputUsuario = document.getElementById('input-usuario');
const _inputPsswd = document.getElementById('input-contrasenya')

function recogerDatos() {
    const _usuario = _inputUsuario.value;
    const _contrasenya = _inputPsswd.value;
    let _hasError = false;

    if (!_usuario) {
        _inputUsuario.classList.add('error');
        _hasError = true;
    }

    if (!_contrasenya) {
        _inputPsswd.classList.add('error');
        _hasError = true;
    }

    if (_hasError) return;

    console.log("hola")

    enviarAJava({
        usuario: _usuario,
        contrasenya: _usuario
    });
}

function enviarAJava(obj) {
    if (window.java && window.java.accion)
        window.java.accion('INICIAR_SESION', obj);
}

document.getElementById('submit-button').addEventListener('click', recogerDatos);

_inputUsuario.addEventListener('input', (key) => {
    _inputUsuario.classList.remove('error');
    _inputUsuario.value = _inputUsuario.value.replace(' ', '');
});

_inputPsswd.addEventListener('input', () => _inputPsswd.classList.remove('error'))