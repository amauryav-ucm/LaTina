document.addEventListener('DOMContentLoaded', function() {

    const fechaInicio = {
        campo: document.getElementById('campo-fecha-inicio'),
        calendario: document.getElementById('calendario-inicio'),
        mes: document.getElementById('mes-inicio'),
        dias: document.getElementById('dias-inicio'),
        mes_ant: document.getElementById('mes-ant-inicio'),
        mes_sig: document.getElementById('mes-sig-inicio')
    };

    const horaInicio = {
        campo: document.getElementById('campo-hora-inicio'),
        dropdown: document.getElementById('tiempo-inicio'),
        hora: document.getElementById('hora-inicio'),
        minuto: document.getElementById('minuto-inicio'),
        ampm: document.getElementById('ampm-inicio'),
        boton: document.getElementById('boton-hora-inicio')
    };

    const fechaFin = {
            campo: document.getElementById('campo-fecha-fin'),
            calendario: document.getElementById('calendario-fin'),
            mes: document.getElementById('mes-fin'),
            dias: document.getElementById('dias-fin'),
            mes_ant: document.getElementById('mes-ant-fin'),
            mes_sig: document.getElementById('mes-sig-fin')
    };

    const horaFin = {
        campo: document.getElementById('campo-hora-fin'),
        dropdown: document.getElementById('tiempo-fin'),
        hora: document.getElementById('hora-fin'),
        minuto: document.getElementById('minuto-fin'),
        ampm: document.getElementById('ampm-fin'),
        boton: document.getElementById('boton-hora-fin')
    };

    configCalendario(fechaInicio);
    configCalendario(fechaFin);
    configHora(horaInicio);
    configHora(horaFin);

    document.getElementById('campo-hora-inicio').addEventListener('click', function () {
        document.getElementById('hora-inicio').classList.toggle('open')
    })

    //const timeInputIni = document.getElementById('time-picker');
    //const timeInputPopupIni = document.getElementById('time-picker-popup');

    document.getElementById('fecha-inicio').addEventListener('click', function (){
        document.getElementById('calendario-inicio').classList.toggle('open');

    })

    document.getElementById('campo-hora-fin').addEventListener('click', function () {
            document.getElementById('tiempo-fin').classList.toggle('open')
    })

    //const timeInputFin = document.getElementById('time-picker');
    //const timeInputPopupFin = document.getElementById('time-picker-popup');

    document.getElementById('fecha-fin').addEventListener('click', function (){
            document.getElementById('calendario-fin').classList.toggle('open');

    })



});

function openTimePicker() {
    document.getElementById("time-picker-popup").style.display = "block";
}

function setTime() {
    let hour = document.getElementById("popup-hour").value;
    let minute = document.getElementById("popup-minute").value;
    let ampm = document.getElementById("popup-ampm").value;
    document.getElementById("time-picker").value = `${hour}:${minute} ${ampm}`;
    document.getElementById("time-picker-popup").style.display = "none";
}

function configCalendario(selector){
    let currentDate = new Date();
    let currentMonth = currentDate.getMonth();
    let currentYear = currentDate.getFullYear();
    let selectedDate = null;

    // Abrir/cerrar el calendario
    selector.campo.addEventListener('click', function() {
        selector.calendario.classList.toggle('open');
        generateCalendar(selector, currentMonth, currentYear);
    });

    // Cerrar el calendario al hacer clic fuera
    document.addEventListener('click', function(e) {
        if (!selector.campo.contains(e.target) && !selector.calendario.contains(e.target)) {
            selector.calendario.classList.remove('open');
        }
    });

    // Navegación por meses
    selector.mes_ant.addEventListener('click', function() {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        generateCalendar(selector, currentMonth, currentYear);
    });

    selector.mes_sig.addEventListener('click', function() {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        generateCalendar(selector, currentMonth, currentYear);
    });
}

// Formatear la fecha para mostrar en el input
function formatDate(date) {
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

// Generar el calendario
function generateCalendar(selector, month, year) {

    let selectedDate = null;

    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
        'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

    // Actualizar el encabezado del mes y año
    selector.mes.textContent = `${monthNames[month]} ${year}`;

    // Limpiar los días anteriores
    selector.dias.innerHTML = '';

    // Obtener el primer día del mes
    const firstDay = new Date(year, month, 1).getDay();
    // Ajustar para que la semana comience en lunes (0 = lunes, 6 = domingo)
    const firstDayAdjusted = firstDay === 0 ? 6 : firstDay - 1;

    // Obtener el último día del mes
    const lastDay = new Date(year, month + 1, 0).getDate();

    // Crear los días vacíos al principio
    for (let i = 0; i < firstDayAdjusted; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'day empty';
        selector.dias.appendChild(emptyDay);
    }

    // Crear los días del mes
    for (let day = 1; day <= lastDay; day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'day';
        dayElement.textContent = day.toString();

        // Marcar el día actual
        const today = new Date();
        if (day === today.getDate() && month === today.getMonth() && year === today.getFullYear()) {
            dayElement.classList.add('today');
        }

        // Marcar el día seleccionado
        if (selectedDate && day === selectedDate.getDate() && month === selectedDate.getMonth() && year === selectedDate.getFullYear()) {
            dayElement.classList.add('selected');
        }

        // Evento para seleccionar un día
        dayElement.addEventListener('click', function() {
            selectedDate = new Date(year, month, day);
            selector.campo.value = formatDate(selectedDate);
            selector.calendario.classList.remove('open');

            //Consigo la fecha de hoy sin horas
            let today = new Date();
            today.setHours(0, 0, 0, 0);

            if (selectedDate < today) {
                mostrarMensaje("Elige una fecha actual o posterior");
                //dateInput.value = "";
                //campo-fecha-inicio.value = "";
                selector.campo.value = "";
                selectedDate = null;
            }

            // Actualizar la visualización del calendario
            generateCalendar(selector, month, year);
        });

        selector.dias.appendChild(dayElement);
    }
}

function configHora(selector){

    for (let h = 1; h <= 12; h++) {
        let option = document.createElement("option");
        option.value = h;
        option.textContent = h;
        selector.hora.appendChild(option);
    }

    selector.campo.addEventListener('click', function () {
        selector.dropdown.classList.toggle('open');
    })

    selector.boton.addEventListener('click', function (){
        let hour = selector.hora.value;
        let minute = selector.minuto.value;
        let ampm = selector.ampm.value;
        selector.campo.value = `${hour}:${minute} ${ampm}`;
        selector.dropdown.classList.remove('open');
    })

    document.addEventListener('click', function (e){
        if(!selector.campo.contains(e.target) && !selector.dropdown.contains(e.target))
            selector.dropdown.classList.remove('open');
    })
}

function mostrarError(mensaje, fechaInicio, fechaFin, horaInicio, horaFin) {
    const popup = document.querySelector(".popup-overlay");
    popup.style.display = "flex";
    popup.classList.add("show");
    document.getElementById("popup-message").innerText = mensaje;

    // Restaurar los valores en el formulario
    document.getElementById("campo-fecha-inicio").value = fechaInicio;
    document.getElementById("campo-hora-inicio").value = horaInicio;
    document.getElementById("campo-fecha-fin").value = fechaFin;
    document.getElementById("campo-hora-fin").value = horaFin;

    // Quitar clases de error de todos los campos primero
    document.querySelectorAll("input").forEach(input => input.classList.remove("error"));
}


function cargarRol() {
    // Se muestra un mensaje de carga y se deshabilita el combo
    const rolSelect = document.getElementById("rol");
    rolSelect.innerHTML = '<option value="" selected>Cargando rol...</option>';
    rolSelect.disabled = true;



    //Llamamos a la función de Java para obtener rol

    waitForJavaBridge(() => {
        console.log("Java bridge is ready!");
        window.java.accion("OBTENER_TODOS_LOS_ROLES", null);
    });

}

function waitForJavaBridge(callback) {
    if (window.java && window.java.accion) {
        callback();
    } else {
        console.log("Waiting for Java bridge...");
        setTimeout(() => waitForJavaBridge(callback), 100);
    }
}

function cargarRolesAux(rol, id) {
    const rolSelect = document.getElementById("rol");
    if (rol) {
        let option = document.createElement("option");
        option.value = id;
        option.textContent = rol;
        rolSelect.appendChild(option);
    } else {
        rolSelect.innerHTML = '<option value="" selected>No hay roles disponibles</option>';
    }
}


function terminadoDeCargarRoles() {
    const rolSelect = document.getElementById("rol");
    rolSelect.disabled = false;
    let firstOption = rolSelect.querySelector("option");
    if (firstOption) {
        firstOption.textContent = "Selecciona un rol";
        firstOption.value = "";
    }
}
function recogerTurno() {
    // Obtenemos los elementos del formulario
    var turno = {};
    var rolSelect = document.getElementById("rol");
    var dateInput = document.getElementById("campo-fecha-inicio");
    var startHourInput = document.getElementById("campo-hora-inicio");
    var dateOutput = document.getElementById("campo-fecha-fin");
    var endHourInput = document.getElementById("campo-hora-fin");


    // Limpiar errores previos y cerrar mensajes emergentes
    rolSelect.classList.remove("error");
    dateInput.classList.remove("error");
    startHourInput.classList.remove("error");
    endHourInput.classList.remove("error");

    // Validar que todos los campos tengan valor
    let hayError = false;
    if (rolSelect.value.trim() === "") {
        rolSelect.classList.add("error");
        hayError = true;
    }
    if (dateInput.value.trim() === "") {
        dateInput.classList.add("error");
        hayError = true;
    }
    if (dateOutput.value.trim() === "") {
            dateOutput.classList.add("error");
            hayError = true;
    }
    if (startHourInput.value.trim() === "") {
        startHourInput.classList.add("error");
        hayError = true;
    }
    if (endHourInput.value.trim() === "") {
        endHourInput.classList.add("error");
        hayError = true;
    }

    // Si hay algún error, mostramos el mensaje y detenemos el envío
    if (hayError) {
        return;
    }

    // Recoger los datos de los campos
    turno.rol = rolSelect.value.trim();
    turno.fechaInicio = dateInput.value.trim();
    turno.fechaFin = dateOutput.value.trim();
    turno.horaInicio = startHourInput.value.trim();
    turno.horaFin = endHourInput.value.trim();
    turno.nombreRol = rolSelect.selectedOptions[0].textContent.trim();

    // Enviar los datos al backend Java (RegistrarTurno.java)
    enviarTurnoAJava(turno);
    setTimeout(() => location.reload(), 200);

}

function validarFormulario() {
     var rolSelect = document.getElementById("rol");
     var dateInput = document.getElementById("campo-fecha-inicio");
     var startHourInput = document.getElementById("campo-hora-inicio");
     var dateOutput = document.getElementById("campo-fecha-fin");
     var endHourInput = document.getElementById("campo-hora-fin");
     let isValid = true; // Flag para saber si hay errores

    // Validación del select de rol
    if (rolSelect.value === "") {
        rolSelect.classList.add("error");
        isValid = false;
    } else {
        rolSelect.classList.remove("error");
    }

    // Validación del fecha ini
    if (dateInput.value === "") {
        dateInput.classList.add("error");
        isValid = false;
    } else {
        dateInput.classList.remove("error");
    }

    //hora ini
    if (startHourInput.value === "") {
        startHourInput.classList.add("error");
        isValid = false;
    }
    else {
        startHourInput.classList.remove("error");
    }

    //fecha fin
    if (dateOutput.value === "") {
        dateOutput.classList.add("error");
        isValid = false;
    }
    else {
        dateOutput.classList.remove("error");
    }

    //hora fin
    if (endHourInput.value === "") {
        endHourInput.classList.add("error");
        isValid = false;
    }
    else {
        endHourInput.classList.remove("error");
    }


    if (!isValid) {
        mostrarMensaje("Por favor, completa todos los campos.");
    }

    // Combinación de fecha y hora de inicio
    var fechaHoraInicio = new Date(dateInput.value + 'T' + startHourInput.value);
    // Combinación de fecha y hora de fin
    var fechaHoraFin = new Date(dateOutput.value + 'T' + endHourInput.value);

    // Validación de que la fecha y hora de fin sean mayores que las de inicio
    if (fechaHoraFin <= fechaHoraInicio) {
        mostrarMensaje("La fecha y hora de fin deben ser mayores que la fecha y hora de inicio.");
        dateOutput.classList.add("error");
        endHourInput.classList.add("error");
        return false;
    }

    return isValid;
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

function enviarTurnoAJava(turno) {
    if (window.java && window.java.accion) {
        window.java.accion("REGISTRAR_TURNO", turno);
    }
}
