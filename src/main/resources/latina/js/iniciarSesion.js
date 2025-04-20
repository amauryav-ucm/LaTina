const _inputUsuario = document.getElementById('input-usuario');
const _inputPsswd = document.getElementById('input-contrasenya')

function recogerDatos() {
    const usuario = _inputUsuario.value;
    const contrasenya = _inputPsswd.value;
    let _hasError = false;

    if (!usuario) {
        _inputUsuario.classList.add('error');
        _hasError = true;
    }

    if (!contrasenya) {
        _inputPsswd.classList.add('error');
        _hasError = true;
    }

    if (_hasError) return;

    console.log("hola")

    enviarAJava({
        usuario: usuario,
        contrasenya: contrasenya
    });
}

function enviarAJava(obj) {
    if (window.java && window.java.accion)
        window.java.accion('INICIAR_SESION', obj);
}

function mostrarMensaje(mensaje) {
    const popup = document.getElementById("popup");
    popup.style.display = "flex";
    document.getElementById("popup-message").innerText = mensaje;
    setTimeout(() => popup.classList.add("show"), 10);
}

function cerrarMensaje() {
    const popup = document.getElementById("popup");
    popup.classList.remove("show");
    setTimeout(() => popup.style.display = "none", 300);
}

document.getElementById('submit-button').addEventListener('click', recogerDatos);

_inputUsuario.addEventListener('input', (key) => {
    _inputUsuario.classList.remove('error');
    _inputUsuario.value = _inputUsuario.value.replace(' ', '');
});

_inputPsswd.addEventListener('input', () => _inputPsswd.classList.remove('error'))