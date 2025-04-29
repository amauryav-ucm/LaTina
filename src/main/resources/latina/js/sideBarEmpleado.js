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
                <li><a href="ficharEntradaParaEmpleado.html" onclick="java.changeScene('ficharEntradaParaEmpleado.html')">Fichar</a></li>
            </ul>

            <div class="sidebar-footer">
                <span>Empleado con DNI ${nombreUsuario}</span>
                <img src="../images/empleado.png" alt="Icono de empleado" class="sidebar-user-icon">
            </div>
        </div>
    `;

    sidebarContainer.innerHTML = sidebarHTML;

    // Si estamos en una página desconocida o en la raíz, marcar "Inicio" como activo por defecto
        if (paginaActual === '' || !paginaActual.includes('.html')) {
            const inicioLink = document.querySelector('.sidebar-menu li:first-child a');
            if (inicioLink) inicioLink.classList.add('active');
        }

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