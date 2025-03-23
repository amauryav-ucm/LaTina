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

                // Llamar a la función para cargar los turnos dinámicamente
                cargarTurnos(dateInput.value);

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

document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector("form");
    const dateInput = document.getElementById('dateInput');
    const turnoField = document.getElementById('turn');
    const employeeSelect = document.getElementById('name');

    // Evento que detecta cuando se selecciona un empleado en el combobox
    employeeSelect.addEventListener("change", function() {
        // Verifica si se ha seleccionado un empleado
        if (this.value.trim() !== "") {
            // Si se seleccionó un empleado, habilita el campo de fecha
            this.classList.add('selected');
            dateInput.disabled = false;
        } else {
            // Si no se seleccionó un empleado, deshabilita el campo de fecha
            this.classList.remove('selected');
            dateInput.disabled = true;
            dateInput.value = ""; // Limpia el campo de fecha
            turnoField.disabled = true; // Deshabilita el campo de turno
            turnoField.value = ""; // Limpia el campo de turno
        }
    });
});

function validarFormulario() {
    var empleadoSelect = document.getElementById("name");
    var dateInput = document.getElementById("dateInput");
    var turnInput = document.getElementById("turn");
    let isValid = true; // Flag para saber si hay errores

    // Validación del select de empleados
    if (empleadoSelect.value.trim() === "") {
        empleadoSelect.classList.add("error");
        isValid = false;
    } else {
        empleadoSelect.classList.remove("error");
    }

    // Validación del input de fecha
    if (dateInput.value.trim() === "" || empleadoSelect.value.trim() === "") {
        dateInput.classList.add("error");
        isValid = false;
    } else {
        dateInput.classList.remove("error");
    }

    // Validación del turno
    if (turnInput.value.trim() === "" || dateInput.value.trim() === "") {
        turnInput.classList.add("error");
        isValid = false;
    } else {
        turnInput.classList.remove("error");
    }

    if (!isValid) {
        alert("Por favor, completa todos los campos.");
    }

    return isValid;
}
//---------------------------------------------------------------
function recogerTurno() {
    var turno = {};
    var empleado = document.getElementById("name");
    var fecha = document.getElementById("dateInput");
    var turnoSeleccionado = document.getElementById("turn");

    // Limpiar errores previos y el mensaje del popup
    empleado.classList.remove("error");
    fecha.classList.remove("error");
    turnoSeleccionado.classList.remove("error");
    cerrarMensaje(); // Evitar que el mensaje predeterminado se muestre si hay error

    // Verificar si los campos están vacíos
    let hayError = false;

    if (empleado.value.trim() === "") {
        empleado.classList.add("error");
        hayError = true;
    }
    if (fecha.value.trim() === "") {
        fecha.classList.add("error");
        hayError = true;
    }
    if (turnoSeleccionado.value.trim() === "") {
        turnoSeleccionado.classList.add("error");
        hayError = true;
    }

    // Si hay errores, no continuar con el envío
    if (hayError) {
        return;
    }

    // Si todos los campos son correctos, enviar los datos
    turno.empleado = empleado.value.trim();
    turno.fecha = fecha.value.trim();
    turno.turno = turnoSeleccionado.value.trim();

    // Enviar los datos al backend Java (AsignarTurno.java)
    enviarTurnoAJava(turno);

    // Mostrar mensaje de éxito y limpiar el formulario (si es necesario)
    mostrarMensaje("Turno asignado correctamente.");

    // Forzar la recarga o redirección si es necesario
    setTimeout(() => location.reload(), 200);
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

function enviarTurnoAJava(turno_empleado) {
    if (window.java && window.java.accion) {
        window.java.accion("ASIGNAR_TURNO", turno_empleado);
    }
}

function cargarTurnos(fecha) {
     if (!fecha) return; // Si no hay fecha, no hacer nada

     document.getElementById("turn").innerHTML = '<option value="" selected>Selecciona un turno</option>';
     document.getElementById("turn").disabled = true;

     //Llamamos a la función de Java para obtener turnos
     window.java.accion("OBTENER_TURNOS_POR_DIA", fecha);
 }

 function cargarTurnosAux(turno) {
     if (turno) {
         let option = document.createElement("option");
         option.value = turno.id;
         option.textContent = turno;
         document.getElementById("turn").appendChild(option);
         document.getElementById("turn").disabled = false; // Habilitar el comboBox
     } else {
         document.getElementById("turn").innerHTML = '<option value="" selected>No hay turnos disponibles</option>';
     }
 }

