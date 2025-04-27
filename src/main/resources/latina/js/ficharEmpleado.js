document.addEventListener('DOMContentLoaded', function () {
    inicializarPopup();
    //cargarDatosIniciales();
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

function actualizarBotonesFichaje() {
    const haFichado = localStorage.getItem("haFichadoEntrada") === "true";

    const btnEntrada = document.querySelector(".fichar-btn.entrada");
    const btnSalida = document.querySelector(".fichar-btn.salida");

    if (btnEntrada) btnEntrada.disabled = haFichado;
    if (btnSalida) btnSalida.disabled = !haFichado;

    // Estilos como si estuviera deshabilitado
    if (btnEntrada) btnEntrada.classList.toggle("disabled", haFichado);
    if (btnSalida) btnSalida.classList.toggle("disabled", !haFichado);
}

function cargarDatosIniciales() {
    // Actualizar los botones según el estado actual
    actualizarBotonesFichaje();
}

function recogerDatosFichaje(tipo) {
    const datosFichaje = {
        tipo: tipo,
        fecha: new Date().toISOString(),
        hora: formatearHora(new Date()), //creo q esto no es necesario, con la fecha basta
        //empleadoId: obtenerIdEmpleado()
        empleadoId: localStorage.getItem("idEmpleado"),
        usuario: localStorage.getItem("usuario")
    };

    // Validación básica
    if (!datosFichaje.empleadoId) {
        mostrarMensaje("Error: No se pudo identificar al empleado");
        return null;
    }

    return datosFichaje;
}

function ficharEntrada() {
    const datosFichaje = recogerDatosFichaje('entrada');
    if (!datosFichaje) return;

    datosFichaje.haFichadoEntrada = true;

    enviarFichaje(datosFichaje);
}

function ficharSalida() {
    const datosFichaje = recogerDatosFichaje('salida');
    if (!datosFichaje) return;

    // Mostrar mensaje al usuario
    //mostrarMensaje(`Salida registrada a las ${datosFichaje.hora}`);
    datosFichaje.haFichadoEntrada = false;

    enviarFichaje(datosFichaje);
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
            //registrarEnHistorialLocal(datos);
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

        /*setTimeout(() => {
            if (popup.classList.contains('show')) cerrarMensaje();
        }, 3000);*/
    }
    waitForJavaBridge(() => {
        window.java.accion(
            "BUSCAR_TURNO_A_FICHAR",
            {
                usuario: localStorage.getItem("usuario"),
            }
        );
    })

}

function updateTime() {
    const now = new Date();
    const timeElement = document.getElementById('current-time');
    const dateElement = document.getElementById('current-date');

    // Format time as HH:MM:SS
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    timeElement.textContent = `${hours}:${minutes}:${seconds}`;

    // Format date in Spanish format
    const options = {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    };
    dateElement.textContent = now.toLocaleDateString('es-ES', options);
}

// Update time every second
updateTime();
setInterval(updateTime, 1000);


function cerrarMensaje() {
    const popup = document.getElementById('popup');
    if (popup) {
        popup.classList.remove('show');
        setTimeout(() => {
            popup.style.display = 'none';
        }, 300);
    }
}

window.onload = () => {
    waitForJavaBridge(() => {
        window.java.accion(
            "BUSCAR_TURNO_A_FICHAR",
            {
                usuario: localStorage.getItem("usuario"),
            }
        );
    })
}

function waitForJavaBridge(callback) {
    if (window.java && window.java.accion)
        callback();
    else {
        setTimeout(() => waitForJavaBridge(callback), 100);
    }
}

function recibirEstadoFichaje(result) {
    if (result == 3) {
        localStorage.setItem("haFichadoEntrada", true);
    } else {
        localStorage.setItem("haFichadoEntrada", false);
    }
    actualizarBotonesFichaje();
    if (result == 2) {
        mostrarMensaje("Se ha fichado la salida automaticamente. Recuerda siempre fichar tu salida");
    }

}

function recibirTurnoAFichar(turno){
    document.getElementById("info-turno").innerHTML = `Turno a fichar: <br> ${turno.fechaHora} <br> ${turno.rol}`;
    if(turno.hayTurno == 1){
        window.java.accion(
            "OBTENER_ESTADO_FICHAJE",
            {
                usuario: localStorage.getItem("usuario"),
            });
    }
    else //Tiene sentido?
    {
        const btnEntrada = document.querySelector(".fichar-btn.entrada");
        const btnSalida = document.querySelector(".fichar-btn.salida");

        btnEntrada.disabled = true;
        btnSalida.disabled = true;
    }
}
