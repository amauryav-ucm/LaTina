function inicializarSidebar() {
    var sidebarContainer = document.getElementById('sidebar-container');

    // Crear el contenido de la sidebar mediante JavaScript
    var sidebarHTML = `
        <button id="desplegarSidebar" class="desplegar-btn">☰</button>
        <div class="sidebar" id="sidebar">
            <ul class="sidebar-menu">
                <li><a href="ventanaPrincipal.html" onclick="java.changeScene('ventanaPrincipal.html')">Inicio</a></li>
                <li><a href="registrarRol.html" onclick="java.changeScene('registrarRol.html')">Registrar rol</a></li>
                <li><a href="registrarTurno.html" onclick="java.changeScene('registrarTurno.html')">Registrar turno</a></li>
                <li><a href="registrarEmpleado.html" onclick="java.changeScene('registrarEmpleado.html')">Registrar empleado</a></li>
                <li><a href="registrarDisponibilidad.html" onclick="java.changeScene('registrarDisponibilidad.html')">Registrar disponibilidad empleado</a></li>
                <li><a href="asignarTurnoAEmpleado.html" onclick="java.changeScene('asignarTurnoAEmpleado.html')">Asignar turno</a></li>
                <li><a href="verTurnosParaGerente.html" onclick="java.changeScene('verTurnosParaGerente.html')">Ver turnos</a></li>
            </ul>
            <div class="sidebar-footer">
                <span>Gerente</span>
                <img src="../images/gerente2.png" alt="Icono de gerente" class="sidebar-user-icon">
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