document.addEventListener('DOMContentLoaded', function() {
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
const saveTurnoBtn = document.getElementById('save-turno');

// Inicializar modal de Bootstrap
const turnoModal = new bootstrap.Modal(document.getElementById('turnoModal'));

// Inicializar calendario
initCalendar();

// Agregar datos de ejemplo
addSampleData();

// Manejadores de eventos
prevWeekBtn.addEventListener('click', () => navigateWeek(-1));
nextWeekBtn.addEventListener('click', () => navigateWeek(1));
currentWeekBtn.addEventListener('click', goToCurrentWeek);
saveTurnoBtn.addEventListener('click', saveTurno);

// Función para inicializar el calendario
function initCalendar() {
    renderCalendarHeader();
    renderWeek(getWeekDates(currentDate));
}

// Función para renderizar los encabezados del calendario
function renderCalendarHeader() {
    // Limpiar contenedor de días de semana
    if (weekContainer.querySelectorAll('.day-header').length === 0) {
        weekDays.forEach(day => {
            const dayHeader = document.createElement('div');
            dayHeader.className = 'day-header';
            dayHeader.textContent = day;
            weekContainer.appendChild(dayHeader);
        });
    }
}

// Función para obtener las fechas de la semana actual
function getWeekDates(date) {
    const week = [];
    // Obtener el domingo de la semana actual
    const firstDay = new Date(date);
    const day = firstDay.getDay(); // 0 para lunes, 1 para martes, etc.
    const offset = day === 0 ? -6 : 1 - day;
        firstDay.setDate(firstDay.getDate() + offset);

    // Generar los 7 días de la semana
    for (let i = 0; i < 7; i++) {
        const currentDay = new Date(firstDay);
        currentDay.setDate(firstDay.getDate() + i);
        week.push(currentDay);
    }

    return week;
}

// Función para renderizar la semana
function renderWeek(weekDates) {
    // Limpiar contenedor de días
    const dayElements = weekContainer.querySelectorAll('.day');
    dayElements.forEach(el => el.remove());

    // Renderizar días
    weekDates.forEach(date => {
        renderDay(date);
    });

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

    const addTurnoBtn = document.createElement('button');
    addTurnoBtn.className = 'add-turno';
    addTurnoBtn.innerHTML = '+';
    addTurnoBtn.addEventListener('click', (e) => {
        e.stopPropagation(); // Evita que otros eventos interfieran
        openTurnoForm(date);
    });

    // Cabecera con número y botón
    dayHeader.appendChild(dayNumber);
    dayHeader.appendChild(addTurnoBtn);

    const dayContent = document.createElement('div');
    dayContent.className = 'day-content';

    // Cargar turnos para este día
    if (turnos[dateKey]) {
        turnos[dateKey].sort((a, b) => a.time.localeCompare(b.time));

        turnos[dateKey].forEach(turno => {
            const turnoEl = document.createElement('div');
            turnoEl.className = 'turno';
            const formattedTime = formatTime(turno.time);

            turnoEl.innerHTML = `
                <div class="fw-bold">${formattedTime}</div>
                <div>${turno.title}</div>
            `;

            turnoEl.addEventListener('click', () => {
                alert(`Turno: ${turno.title}\nHora: ${formattedTime}\n${turno.description || ''}`);
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

    const formattedStart = `${startDate.getDate()} de ${monthNames[startDate.getMonth()]}`;
    const formattedEnd = `${endDate.getDate()} de ${monthNames[endDate.getMonth()]}`;

    weekTitle.textContent = `Semana del ${formattedStart} al ${formattedEnd}, ${endDate.getFullYear()}`;
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

// Abrir formulario para agregar turno
function openTurnoForm(date) {
    selectedDate = date;

    // Limpiar formulario
    document.getElementById('turno-title').value = '';
    document.getElementById('turno-time').value = '';
    document.getElementById('turno-desc').value = '';

    // Actualizar título del modal
    document.getElementById('turnoModalLabel').textContent =
        `Agregar Turno - ${date.getDate()} de ${monthNames[date.getMonth()]}`;

    // Mostrar modal
    turnoModal.show();
}

// Guardar turno
function saveTurno() {
    const titleInput = document.getElementById('turno-title');
    const timeInput = document.getElementById('turno-time');
    const descInput = document.getElementById('turno-desc');

    if (!selectedDate || !titleInput.value || !timeInput.value) {
        alert('Por favor complete los campos requeridos');
        return;
    }

    const dateKey = formatDate(selectedDate);
    if (!turnos[dateKey]) {
        turnos[dateKey] = [];
    }

    turnos[dateKey].push({
        title: titleInput.value,
        time: timeInput.value,
        description: descInput.value
    });

    // Actualizar calendario
    renderWeek(getWeekDates(currentDate));

    // Cerrar modal
    turnoModal.hide();
}

// Agregar datos de ejemplo para mostrar
function addSampleData() {
    // Fecha actual
    const today = new Date();
    const todayKey = formatDate(today);

    // Ejmplo para crear turnos hoy
    turnos[todayKey] = [
        {
            title: 'María García - Camarera',
            time: '09:00',
            description: 'Turno mañana'
        },
        {
            title: 'Juan Pérez - Cocinero',
            time: '14:30',
            description: 'Turno tarde'
        }
    ];
}

// Utilidades
function formatDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function formatTime(timeString) {
    // Convertir formato 24h a 12h
    const [hours, minutes] = timeString.split(':');
    const hour = parseInt(hours);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const hour12 = hour % 12 || 12;
    return `${hour12}:${minutes} ${ampm}`;
}

function isSameDay(date1, date2) {
    return date1.getDate() === date2.getDate() &&
           date1.getMonth() === date2.getMonth() &&
           date1.getFullYear() === date2.getFullYear();
}
});