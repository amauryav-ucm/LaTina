function recogerDatos() {

    var empleado = {};
    var dni = document.getElementById("dni");
    var nombre = document.getElementById("nombre");
    var apellidos = document.getElementById("apellidos");
    var email = document.getElementById("email");
    var telefono = document.getElementById("telefono");

    // Limpiar errores previos y el mensaje del popup
    dni.classList.remove("error");
    nombre.classList.remove("error");
    apellidos.classList.remove("error");
    email.classList.remove("error");
    telefono.classList.remove("error");
    cerrarMensaje(); // Evitar que el mensaje predeterminado se muestre si hay error



    // Verificar si los campos están vacíos o son inválidos
    let hayError = false;

    if (dni.value.trim() === "" /*|| !/^\d{8}[A-Z]$/.test(dni.value.trim())*/) {
        dni.classList.add("error");  // Agregar clase de error
        hayError = true;
    }

    if (nombre.value.trim() === "") {
        nombre.classList.add("error");  // Agregar clase de error
        hayError = true;
    }

    if (apellidos.value.trim() === "") {
        apellidos.classList.add("error");  // Agregar clase de error
        hayError = true;
    }

    if (email.value.trim() === "" /*|| !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())*/) {
        email.classList.add("error");  // Agregar clase de error
        hayError = true;
    }

    if (telefono.value.trim() === "" /*|| !/^\d{9}$/.test(telefono.value.trim())*/) {
        telefono.classList.add("error");  // Agregar clase de error
        hayError = true;
    }

    // Si hay errores, no continuar con el envío
    if (hayError) {
        return;
    }


    empleado.dni = dni.value.trim();
    empleado.nombre = nombre.value.trim();
    empleado.apellidos = apellidos.value.trim();
    empleado.email = email.value.trim();
    empleado.telefono = telefono.value.trim();

    enviarAJava(empleado);
    setTimeout(() => location.reload(), 200);

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

function mostrarError(mensaje, dni, nombre, apellidos, email, telefono, camposError) {
    const popup = document.querySelector(".popup-overlay");
    popup.style.display = "flex";
    popup.classList.add("show");
    document.getElementById("popup-message").innerText = mensaje;

    // Restaurar los valores en el formulario
    document.getElementById("dni").value = dni;
    document.getElementById("nombre").value = nombre;
    document.getElementById("apellidos").value = apellidos;
    document.getElementById("email").value = email;
    document.getElementById("telefono").value = telefono;

    // Quitar clases de error de todos los campos primero
    document.querySelectorAll("input").forEach(input => input.classList.remove("error"));

    // Agregar la clase de error solo a los campos que fallaron
    camposError.forEach(id => {
        const campo = document.getElementById(id);
        if (campo) campo.classList.add("error");
    });
}

function enviarAJava(empleado) {
     if (window.java && window.java.accion) {
            window.java.accion("REGISTRAR_EMPLEADO", empleado);
     }
}
