document.addEventListener('DOMContentLoaded', function() {
    const dateInput = document.getElementById('dateInput');
    const calendarDropdown = document.getElementById('calendarDropdown');
    const monthYearElement = document.getElementById('monthYear');
    const calendarDays = document.getElementById('calendarDays');
    const prevMonthBtn = document.getElementById('prevMonth');
    const nextMonthBtn = document.getElementById('nextMonth');

    let currentDate = new Date();
    let currentMonth = currentDate.getMonth();
    let currentYear = currentDate.getFullYear();
    let selectedDate = null;

    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                        'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

    // Abrir/cerrar el calendario
    dateInput.addEventListener('click', function() {
        calendarDropdown.classList.toggle('open');
        generateCalendar(currentMonth, currentYear);
    });

    // Cerrar el calendario al hacer clic fuera
    document.addEventListener('click', function(e) {
        if (!dateInput.contains(e.target) && !calendarDropdown.contains(e.target)) {
            calendarDropdown.classList.remove('open');
        }
    });

    // Navegación por meses
    prevMonthBtn.addEventListener('click', function() {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        generateCalendar(currentMonth, currentYear);
    });

    nextMonthBtn.addEventListener('click', function() {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        generateCalendar(currentMonth, currentYear);
    });

    // Generar el calendario
    function generateCalendar(month, year) {
        // Actualizar el encabezado del mes y año
        monthYearElement.textContent = `${monthNames[month]} ${year}`;

        // Limpiar los días anteriores
        calendarDays.innerHTML = '';

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
            calendarDays.appendChild(emptyDay);
        }

        // Crear los días del mes
        for (let day = 1; day <= lastDay; day++) {
            const dayElement = document.createElement('div');
            dayElement.className = 'day';
            dayElement.textContent = day;

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
                dateInput.value = formatDate(selectedDate);
                calendarDropdown.classList.remove('open');

                // Habilita el campo de turno cuando se selecciona una fecha
                document.getElementById("turn").disabled = false;
                // Actualizar la visualización del calendario
                generateCalendar(month, year);
            });

            calendarDays.appendChild(dayElement);
        }
    }


    // Formatear la fecha para mostrar en el input
    function formatDate(date) {
        const day = date.getDate().toString().padStart(2, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    }

    // Generar el calendario inicial
    generateCalendar(currentMonth, currentYear);
});

document.getElementById("name").addEventListener("input", function() {
    let dateField = document.getElementById("dateInput");

    if (this.value.trim() !== "") {
        dateField.disabled = false; // Habilita el campo
    } else {
        dateField.disabled = true; // Deshabilita si está vacío
        dateField.value = "";
    }
});

document.getElementById("dateInput").addEventListener("input", function() {
    let turnField = document.getElementById("turn");

    if (this.value.trim() !== "") {
        turnField.disabled = false; // Habilita el campo
    } else {
        turnField.disabled = true; // Deshabilita si está vacío
    }
});