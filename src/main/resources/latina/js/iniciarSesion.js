const _inputUsuario = document.getElementById('input-usuario');
const _inputPsswd = document.getElementById('input-contrasenya')

document.addEventListener('DOMContentLoaded', function() {
    if (window.java && window.java.accion)
            window.java.accion('INICIALIZAR_GERENTE', {});
}

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

// Añade esto a tu archivo JavaScript
document.getElementById('toggle-password').addEventListener('click', function() {
    const passwordInput = document.getElementById('input-contrasenya');
    const icon = document.getElementById('icon-password');

    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        icon.src = "../images/eye-slash-solid.svg"; // Cambia al icono de ojo cerrado
    } else {
        passwordInput.type = "password";
        icon.src = "../images/eye-solid.svg"; // Cambia al icono de ojo abierto
    }
});

//  elimina la clase "error" del input de contraseña cuando el usuario comienza a escribir en él
_inputPsswd.addEventListener('input', () => _inputPsswd.classList.remove('error'));

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