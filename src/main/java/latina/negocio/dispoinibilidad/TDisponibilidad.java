package latina.negocio.dispoinibilidad;

public class TDisponibilidad {

    private int id;
    private int empleadoId;
    private String fechaInicio; // Formato DD/MM/YY
    private String fechaFin; // Formato DD/MM/YY
    private String horaInicio; // Formato HH:MM
    private String horaFin; // Formato HH:MM


    public int getId() {return id;    }

    public void setId(int id) {this.id = id;    }

    public int getEmpleadoId() {return empleadoId;    }

    public void setEmpleadoId(int empleadoId) {this.empleadoId = empleadoId;    }

    public String getFechaInicio() {return fechaInicio;    }

    public void setFechaInicio(String fechaInicio) {this.fechaInicio = fechaInicio;    }

    public String getFechaFin() {return fechaFin;    }

    public void setFechaFin(String fechaFin) {this.fechaFin = fechaFin;    }

    public String getHoraInicio() {return horaInicio;    }

    public void setHoraInicio(String horaInicio) {this.horaInicio = horaInicio;    }

    public String getHoraFin() {return horaFin;    }

    public void setHoraFin(String horaFin) {this.horaFin = horaFin;    }

    public TDisponibilidad(){ };

    public TDisponibilidad(int empleadoId, String fechaInicio, String fechaFin, String horaInicio, String horaFin){
        this.empleadoId = empleadoId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }



}
