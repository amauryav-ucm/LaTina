document.addEventListener('DOMContentLoaded', function () {
    inicializarPopup();
    cargarDatosIniciales();
});

function inicializarPopup() {
    const popup = document.getElementById('popup');
    if (popup) {
        popup.style.display = 'none';
        const btnCerrar = popup.querySelector('.popup-button');
        if (btnCerrar) {
            btnCerrar.addEventListener('click', cerrarMensaje);
        }
    }
}

function cargarDatosIniciales() {
    // Aquí puedes cargar datos necesarios al iniciar
}

function ficharEntrada() {
    const fecha = new Date();
    const datosFichaje = {
        tipo: 'entrada',
        fecha: fecha.toISOString(),
        hora: formatearHora(fecha),
        empleadoId: obtenerIdEmpleado()
    };

    if (!validarFichaje('entrada', datosFichaje)) return;

    mostrarMensaje(`Entrada registrada a las ${datosFichaje.hora}`);
    enviarFichaje(datosFichaje);
}

function ficharSalida() {
    const fecha = new Date();
    const datosFichaje = {
        tipo: 'salida',
        fecha: fecha.toISOString(),
        hora: formatearHora(fecha),
        empleadoId: obtenerIdEmpleado()
    };

    if (!validarFichaje('salida', datosFichaje)) return;

    mostrarMensaje(`Salida registrada a las ${datosFichaje.hora}`);
    enviarFichaje(datosFichaje);
}

function validarFichaje(tipo, datos) {
    if (!datos.empleadoId) {
        mostrarMensaje("Error: No se pudo identificar al empleado");
        return false;
    }
    return true;
}

function formatearHora(fecha) {
    return fecha.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

function obtenerIdEmpleado() {
    if (document.body.hasAttribute('data-empleado-id')) {
        return document.body.getAttribute('data-empleado-id');
    }

    const inputHidden = document.getElementById('empleado-id');
    if (inputHidden) {
        return inputHidden.value;
    }

    if (window.java && window.java.obtenerIdEmpleado) {
        return window.java.obtenerIdEmpleado();
    }

    console.error("No se pudo obtener el ID del empleado");
    return null;
}

function enviarFichaje(datos) {
    if (window.java && window.java.accion) {
        try {
            window.java.accion("REGISTRAR_FICHAJE", datos);
            registrarEnHistorialLocal(datos);
            return;
        } catch (e) {
            console.error("Error al enviar a Java:", e);
        }
    }

    enviarFichajePorFetch(datos);
}

function enviarFichajePorFetch(datos) {
    fetch('/api/fichajes', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(datos),
    })
        .then(response => {
            if (!response.ok) throw new Error('Error en el servidor');
            return response.json();
        })
        .then(data => {
            console.log('Fichaje registrado:', data);
            registrarEnHistorialLocal(datos);
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarMensaje("Error al registrar el fichaje. Se guardó localmente.");
            registrarEnHistorialLocal(datos);
        });
}

function registrarEnHistorialLocal(datos) {
    try {
        const historial = JSON.parse(localStorage.getItem('historialFichajes') || '[]');
        historial.push(datos);
        localStorage.setItem('historialFichajes', JSON.stringify(historial));
    } catch (e) {
        console.error("Error al guardar en localStorage:", e);
    }
}

function mostrarMensaje(mensaje) {
    const popup = document.getElementById('popup');
    const popupMessage = document.getElementById('popup-message');

    if (popup && popupMessage) {
        popupMessage.textContent = mensaje;
        popup.style.display = 'flex';
        setTimeout(() => popup.classList.add('show'), 10);

        setTimeout(() => {
            if (popup.classList.contains('show')) cerrarMensaje();
        }, 3000);
    }
}

function cerrarMensaje() {
    const popup = document.getElementById('popup');
    if (popup) {
        popup.classList.remove('show');
        setTimeout(() => {
            popup.style.display = 'none';
        }, 300);
    }
}
