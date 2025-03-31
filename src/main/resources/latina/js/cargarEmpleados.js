function cargarEmpleadosAux(empleado, idEmpleado) {
    let comboBox = document.getElementById("name")
    if (empleado) {
        let option = document.createElement("option");
        option.value = id;
        option.textContent = empleado;
        comboBox.appendChild(option);
    }
    else {
        comboBox.innerHTML = '<option value="" selected>No hay empleados disponibles</option>';
    }
}