document.addEventListener('DOMContentLoaded', function() {
    const fechaInicio = {
        campo: document.getElementById('campo-fecha-inicio'),
        calendario: document.getElementById('calendario-inicio'),
        mes: document.getElementById('mes-inicio'),
        dias: document.getElementById('dias-inicio'),
        mes_ant: document.getElementById('mes-ant-inicio'),
        mes_sig: document.getElementById('mes-sig-inicio')
    };

    const fechaFin = {
        campo: document.getElementById('dateInput'),
        calendario: document.getElementById('calendarDropdown'),
        mes: document.getElementById('monthYear'),
        dias: document.getElementById('calendarDays'),
        mes_ant: document.getElementById('prevMonth'),
        mes_sig: document.getElementById('nextMonth')
    };

    configCalendario(fechaInicio);
    configCalendario(fechaFin);

    document.getElementById('campo-hora-inicio').addEventListener('click', function () {
        document.getElementById('hora-inicio').classList.toggle('open')
    })

    const timeInput = document.getElementById('time-picker');
    const timeInputPopup = document.getElementById('time-picker-popup');






    document.getElementById('fecha-inicio').addEventListener('click', function (){
        document.getElementById('calendario-inicio').classList.toggle('open');

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

            // Actualizar la visualización del calendario
            generateCalendar(selector, month, year);
        });

        selector.dias.appendChild(dayElement);
    }
}