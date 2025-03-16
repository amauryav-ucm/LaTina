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


function enviarAJava(rol){
    if (window.java && window.java.accion) {
                window.java.accion("REGISTRAR_ROL", rol);
            }
}

function cerrarMensaje() {
    const popup = document.getElementById("popup");
    popup.classList.remove("show");
    setTimeout(() => popup.style.display = "none", 300);
}

function volver(){
    if(window.java && window.java.changeScene){
        window.java.changeScene("ventanaPrincipal.html");
    }
}