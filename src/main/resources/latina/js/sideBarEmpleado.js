function inicializarSidebar() {
    var sidebarContainer = document.getElementById('sidebar-container');
    var nombreUsuario = localStorage.getItem("usuario") || "Usuario";
    // Crear el contenido de la sidebar mediante JavaScript
    var sidebarHTML = `
        <button id="desplegarSidebar" class="desplegar-btn">☰</button>
        <div class="sidebar" id="sidebar">
            <ul class="sidebar-menu">
                <li><a href="ventanaPrincipalEmpleado.html" onclick="java.changeScene('ventanaPrincipalEmpleado.html')">Inicio</a></li>
                <li><a href="registrarDisponibilidadEmpleado.html" onclick="java.changeScene('registrarDisponibilidadEmpleado.html')">Registrar disponibilidad</a></li>
                <li><a href="verTurnosParaEmpleado.html" onclick="java.changeScene('verTurnosParaEmpleado.html')">Ver turnos</a></li>
                <li><a href="ficharEntradaParaEmpleado.html" onclick="java.changeScene('ficharEntradaParaEmpleado.html')">Fichar</a></li>
            </ul>

            <div class="sidebar-footer">
                <span>${nombreUsuario}</span>
            </div>
        </div>
    `;

    sidebarContainer.innerHTML = sidebarHTML;

    // Añadir evento al botón para alternar la barra lateral
    document.getElementById("desplegarSidebar").addEventListener("click", desplazarSidebar);
}

// Función para alternar la visibilidad de la barra lateral
function desplazarSidebar() {
    var sidebar = document.getElementById("sidebar");
    sidebar.classList.toggle("sidebar-collapsed");
}

// Ejecutar cuando se carga la página
document.addEventListener("DOMContentLoaded", function() {
    inicializarSidebar();
});