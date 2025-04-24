function recogerDatos() {
    var rol = {};
    var nombre = document.getElementById("name");
    var salario = document.getElementById("wage");

    // Limpiar errores previos y el mensaje del popup
    nombre.classList.remove("error");
    salario.classList.remove("error");
    cerrarMensaje(); // Evitar que el mensaje predeterminado se muestre si hay error

    // Verificar si los campos están vacíos
    let hayError = false;

    if (nombre.value.trim() === "") {
        nombre.classList.add("error");  // Agregar clase de error
        hayError = true;
    }
    if (salario.value.trim() === "" || isNaN(salario.value.trim())) {
        salario.classList.add("error");  // Agregar clase de error
        hayError = true;
    }

    // Si hay errores, no continuar con el envío
    if (hayError) {
        return;
    }

    // Si todos los campos son correctos, enviar los datos
    rol.nombre = nombre.value.trim();
    rol.salario = salario.value.trim();

    // Enviar los datos al backend Java (RegistrarRol.java)
    enviarAJava(rol);

    setTimeout(() => location.reload(), 200);
}

function mostrarMensaje(mensaje) {
    const popup = document.getElementById("popup");
    popup.style.display = "flex";
    document.getElementById("popup-message").innerText = mensaje;
    setTimeout(() => popup.classList.add("show"), 10);
}

function enviarAJava(rol){
    if (window.java && window.java.accion) {
                window.java.accion("REGISTRAR_ROL", rol);
            }
}

function mostrarError(mensaje, nombreRol, salarioRol, camposError) {
    const popup = document.querySelector(".popup-overlay");
    popup.style.display = "flex";
    popup.classList.add("show");
    document.getElementById("popup-message").innerText = mensaje;

    // Restaurar los valores en el formulario
    document.getElementById("name").value = nombreRol;
    document.getElementById("wage").value = salarioRol;

    // Quitar clases de error de todos los campos primero
    document.querySelectorAll("input").forEach(input => input.classList.remove("error"));

    // Agregar la clase de error solo a los campos que fallaron
    camposError.forEach(id => {
        const campo = document.getElementById(id);
        if (campo) campo.classList.add("error");
    });
}


function cerrarMensaje() {
    const popup = document.getElementById("popup");
    popup.classList.remove("show");
    setTimeout(() => popup.style.display = "none", 300);
}

// Añade esta función a tu archivo registrarRol.js
document.addEventListener('DOMContentLoaded', function() {
    const nombreInput = document.getElementById('name');

    // Convierte a minúsculas mientras el usuario escribe
    nombreInput.addEventListener('input', function() {
        // Guarda la posición del cursor antes de modificar el valor
        const cursorPos = this.selectionStart;

        // Convierte el texto a minúsculas
        const valorOriginal = this.value;
        const valorMayusc = valorOriginal.toUpperCase();

        // Si realmente hubo un cambio (evita bucles infinitos)
        if (valorOriginal !== valorMayusc) {
            this.value = valorMayusc;

            // Restaura la posición del cursor
            this.setSelectionRange(cursorPos, cursorPos);
        }
    });
});