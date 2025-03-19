function inicializarSidebar() {
    var sidebarContainer = document.getElementById('sidebar-container');

    // Crear el contenido de la sidebar mediante JavaScript
    var sidebarHTML = `
        <div class="sidebar">
            <ul class="sidebar-menu">
                <li><a href="ventanaPrincipal.html" onclick="java.changeScene('ventanaPrincipal.html')">Inicio</a></li>
                <li><a href="registrarRol.html" onclick="java.changeScene('registrarRol.html')">Registrar rol</a></li>
                <li><a href="#" onclick="java.changeScene('')">Registrar turno</a></li>
                <li><a href="#" onclick="java.changeScene('')">Registrar empleado</a></li>
                <li><a href="#" onclick="java.changeScene('')">Asignar turno</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
                <li><a href="#" onclick="java.changeScene('')">Etc...</a></li>
            </ul>

            <!-- Sección de inicio de sesión -->
            <div class="sidebar-login">
                <p>
                    USUARIO GERENTE
                </p>
                <img class="imagen-gerente" src= "../images/gerente.png">
            </div>
        </div>
    `;


    sidebarContainer.innerHTML = sidebarHTML;
}

// Ejecutar cuando se carga la página
document.addEventListener("DOMContentLoaded", function() {
    inicializarSidebar();
});