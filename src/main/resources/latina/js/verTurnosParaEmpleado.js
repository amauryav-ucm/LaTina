// Variables globales
let currentDate = new Date();
let selectedDate = null;
let turnos = {}; // Almacenará los turnos en memoria

// Días y meses en español
const weekDays = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];
const monthNames = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];

// Referencias DOM
const weekContainer = document.getElementById('week-container');
const weekTitle = document.getElementById('week-title');
const prevWeekBtn = document.getElementById('prev-week');
const nextWeekBtn = document.getElementById('next-week');
const currentWeekBtn = document.getElementById('current-week');


document.addEventListener('DOMContentLoaded', function () {



// Manejadores de eventos
    prevWeekBtn.addEventListener('click', () => navigateWeek(-1));
    nextWeekBtn.addEventListener('click', () => navigateWeek(1));
    currentWeekBtn.addEventListener('click', goToCurrentWeek);

});


function agregarTurnoAlDia(turno, dia) {
    let dateKey = formatDate(new Date(dia)); // Formatea la fecha para que coincida con la estructura de turnos

    if (!turnos[dateKey]) {
        turnos[dateKey] = [];
    }

    turnos[dateKey].push({
        startTime: turno.fechaHoraInicio, // Asegúrate de que el formato sea compatible con tu renderizado. Entiendo ChatGPT
        endTime: turno.fechaHoraFin,
        dniEmpleado: turno.dniEmpleado, // Agrega el DNI del empleado
        rol: turno.rol // Agrega el nombre del rol
    });

}

function terminadoDeCargar(){

}

// Utilidades
function formatDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function cargarTurnos(Lunes, callback) {
    waitForJavaBridge(() => {
        console.log("Java bridge is ready!");
        if (!Lunes) return;
        window.java.accion("OBTENER_TURNOS_SEMANALES_EMPLEADO", Lunes);
        callback();
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


// Función para renderizar la semana
function renderWeek(weekDates) {
    // Limpiar contenedor de días
    const dayElements = weekContainer.querySelectorAll('.day');
    dayElements.forEach(el => el.remove());
    //meter los turnos registrados en el calendario
    const lunes = getWeekDates(currentDate)[0]; // Obtiene el lunes
    const lunesFormateado = `${lunes.getFullYear()}-${String(lunes.getMonth() + 1).padStart(2, '0')}-${String(lunes.getDate()).padStart(2, '0')} 00:00:00`;
    turnos = {};
    cargarTurnos(lunesFormateado, () =>{

        weekDates.forEach(date => {
            renderDay(date);
        });
    });

    // Renderizar días


    // Actualizar título de la semana
    updateWeekTitle(weekDates);
}

function renderDay(date) {
    const dateKey = formatDate(date);

    const day = document.createElement('div');
    day.className = 'day';
    day.dataset.date = dateKey;

    // Marcar día actual
    if (isSameDay(date, new Date())) {
        day.classList.add('current-day');
    }

    // Cabecera del día (número y botón)
    const dayHeader = document.createElement('div');
    dayHeader.className = 'day-header-inner';

    const dayNumber = document.createElement('div');
    dayNumber.className = 'day-number';
    dayNumber.textContent = `${date.getDate()} ${monthNames[date.getMonth()]}`;

    // Cabecera con número y botón
    dayHeader.appendChild(dayNumber);

    const dayContent = document.createElement('div');
    dayContent.className = 'day-content';


    // Cargar turnos para este día ----- Se va a tener que cambiar para verlos desde la bd en vez de la variable global
    if (turnos[dateKey]) {
        turnos[dateKey].sort((a, b) => a.startTime.localeCompare(b.startTime));

        turnos[dateKey].forEach(turno => {
            const turnoEl = document.createElement('div');
            turnoEl.className = 'turno';

            //const formattedStartTime = formatTime(turno.startTime);
            //const formattedEndTime = formatTime(turno.endTime);
            const horaInicio = turno.startTime.split(" ")[1].slice(0, 5);
            const horaFin = turno.endTime.split(" ")[1].slice(0, 5);

            turnoEl.innerHTML = `
                    <div class="fw-bold">
                        ${horaInicio} - ${horaFin} <br>
                        ${turno.rol} <br>
                        ${turno.dniEmpleado}
                    </div>
                `;

            turnoEl.addEventListener('click', () => {
                alert(`Hora: ${formattedStartTime} - ${formattedEndTime}`);
            });

            dayContent.appendChild(turnoEl);
        });
    }

    day.appendChild(dayHeader);
    day.appendChild(dayContent);
    weekContainer.appendChild(day);
}

// Actualizar título de la semana
function updateWeekTitle(weekDates) {
    const startDate = weekDates[0];
    const endDate = weekDates[6];

    const formattedStart = `${startDate.getDate()}`;
    const formattedEnd = `${endDate.getDate()}`;

    weekTitle.textContent = `${formattedStart} - ${formattedEnd} de ${monthNames[endDate.getMonth()]} de ${endDate.getFullYear()}`;
}

function isSameDay(date1, date2) {
    return date1.getDate() === date2.getDate() &&
        date1.getMonth() === date2.getMonth() &&
        date1.getFullYear() === date2.getFullYear();
}


// Función para obtener las fechas de la semana actual
function getWeekDates(date) {
    const week = [];
    const firstDay = new Date(date);
    const day = firstDay.getDay();
    const offset = day === 0 ? -6 : 1 - day;
    firstDay.setDate(firstDay.getDate() + offset);

    for (let i = 0; i < 7; i++) {
        const currentDay = new Date(firstDay);
        currentDay.setDate(firstDay.getDate() + i);
        week.push(currentDay);
    }

    return week;
}

// Función para inicializar el calendario
function initCalendar() {
    //renderCalendarHeader();
    currentDate = new Date();
    renderWeek(getWeekDates(currentDate));
}

// Navegar entre semanas
function navigateWeek(direction) {
    currentDate.setDate(currentDate.getDate() + (direction * 7));
    renderWeek(getWeekDates(currentDate));
}

// Ir a la semana actual
function goToCurrentWeek() {
    currentDate = new Date();
    renderWeek(getWeekDates(currentDate));
}
