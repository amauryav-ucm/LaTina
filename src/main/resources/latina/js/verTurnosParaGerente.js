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



    // Inicializar calendario
    initCalendar();

    // Manejadores de eventos
    prevWeekBtn.addEventListener('click', () => navigateWeek(-1));
    nextWeekBtn.addEventListener('click', () => navigateWeek(1));
    currentWeekBtn.addEventListener('click', goToCurrentWeek);

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

    // Función para renderizar la semana
    function renderWeek(weekDates) {
        // Limpiar contenedor de días
        const dayElements = weekContainer.querySelectorAll('.day');
        dayElements.forEach(el => el.remove());
        //meter los turnos registrados en el calendario
        const lunes = getWeekDates(currentDate)[0]; // Obtiene el lunes
        const lunesFormateado = `${lunes.getFullYear()}-${String(lunes.getMonth() + 1).padStart(2, '0')}-${String(lunes.getDate()).padStart(2, '0')} 00:00:00`;
        cargarTurnos(lunesFormateado);

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

                const formattedStartTime = formatTime(turno.startTime);
                const formattedEndTime = formatTime(turno.endTime);

                turnoEl.innerHTML = `
                    <div class="fw-bold">${formattedStartTime} - ${formattedEndTime}</div>
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

    function agregarTurnoAlDia(turno, dia) {
        const dateKey = formatDate(dia); // Formatea la fecha para que coincida con la estructura de turnos

        if (!turnos[dateKey]) {
            turnos[dateKey] = [];
        }

        turnos[dateKey].push({
            startTime: turno.fechaHoraInicio, // Asegúrate de que el formato sea compatible con tu renderizado
            endTime: turno.fechaHoraFin
        });

    }

    // Actualizar título de la semana
    function updateWeekTitle(weekDates) {
        const startDate = weekDates[0];
        const endDate = weekDates[6];

        const formattedStart = `${startDate.getDate()}`;
        const formattedEnd = `${endDate.getDate()}`;

        weekTitle.textContent = `${formattedStart} - ${formattedEnd} de ${monthNames[endDate.getMonth()]} de ${endDate.getFullYear()}`;
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

    function cargarTurnos(Lunes)
    {
        waitForJavaBridge(() => {
            console.log("Java bridge is ready!");
            if (!Lunes) return;
                window.java.accion("OBTENER_TURNOS_SEMANALES", Lunes);
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


    // Utilidades
    function formatDate(date) {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    }

    function isSameDay(date1, date2) {
        return date1.getDate() === date2.getDate() &&
               date1.getMonth() === date2.getMonth() &&
               date1.getFullYear() === date2.getFullYear();
    }
    const timeInputs = document.querySelectorAll('.time-input');

    timeInputs.forEach(input => {
        // Aplicar formato al escribir
        input.addEventListener('input', function(e) {
            let value = this.value.replace(/[^0-9]/g, '');

            if (value.length > 2) {
                value = value.substring(0, 2) + ':' + value.substring(2, 4);
            }

            this.value = value;
        });
    });
});
