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
        document.getElementById('turno-start-time').value = '';
        document.getElementById('turno-end-time').value = '';

        // Actualizar título del modal
        document.getElementById('turnoModalLabel').textContent =
            `Agregar Turno - ${date.getDate()} de ${monthNames[date.getMonth()]}`;

        // Mostrar modal
        turnoModal.show();
    }

    // Guardar turno
    // Guardar turno
    function saveTurno() {
        const startTimeInput = document.getElementById('turno-start-time');
        const endTimeInput = document.getElementById('turno-end-time');
        const errorMessageDiv = document.getElementById('error-message'); // Contenedor para el mensaje de error

        // Limpiar cualquier mensaje de error previo y las clases de error
        errorMessageDiv.style.display = 'none';
        errorMessageDiv.textContent = '';
        startTimeInput.classList.remove('error');
        endTimeInput.classList.remove('error');

        // Validar los campos
        let hasError = false;
        if (!selectedDate || !startTimeInput.value || !endTimeInput.value) {
            errorMessageDiv.style.display = 'block'; // Mostrar el mensaje de error
            errorMessageDiv.textContent = 'Por favor complete todos los campos requeridos.';
            hasError = true;
        }

        // Si hay error, agregar la clase error a los campos
        if (!startTimeInput.value) {
            startTimeInput.classList.add('error');
        }
        if (!endTimeInput.value) {
            endTimeInput.classList.add('error');
        }

        // Si hay un error, no guardar y retornar
        if (hasError) {
            return;
        }

        // Convertir las horas de inicio y fin a objetos Date para comparación
        const startTime = convertToDateTime(startTimeInput.value);
        const endTime = convertToDateTime(endTimeInput.value);

        // Verificar que la hora de fin no sea anterior a la hora de inicio
        if (endTime <= startTime) {
            errorMessageDiv.style.display = 'block'; // Mostrar el mensaje de error
            errorMessageDiv.textContent = 'La hora de fin debe ser posterior a la hora de inicio.';
            startTimeInput.classList.add('error');
            endTimeInput.classList.add('error');
            return;
        }

        const dateKey = formatDate(selectedDate);
        if (!turnos[dateKey]) {
            turnos[dateKey] = [];
        }

        turnos[dateKey].push({
            startTime: startTimeInput.value,
            endTime: endTimeInput.value
        });

        // Actualizar calendario
        renderWeek(getWeekDates(currentDate));

        // Cerrar modal
        turnoModal.hide();
    }


    // Función para convertir la hora (HH:mm) en un objeto Date para compararlo
    function convertToDateTime(timeString) {
        const [hours, minutes] = timeString.split(':');
        const date = new Date();
        date.setHours(parseInt(hours), parseInt(minutes), 0, 0); // Establece la hora y minutos
        return date;
    }


    // Utilidades
    function formatDate(date) {
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    }

    function formatTime(timeString) {
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

        // Validar al perder el foco
        input.addEventListener('blur', function() {
            const pattern = /^([01]?[0-9]|2[0-3]):([0-5][0-9])$/;
            if (!pattern.test(this.value) && this.value !== '') {
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        });
    });
});
