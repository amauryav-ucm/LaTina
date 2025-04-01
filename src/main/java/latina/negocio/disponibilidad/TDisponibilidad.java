package latina.negocio.disponibilidad;

import java.sql.Timestamp;

public class TDisponibilidad {

    private int id;
    private int empleadoId;
    private Timestamp fechaInicio;
    private Timestamp fechaFin;



    public int getId() {return id;    }

    public void setId(int id) {this.id = id;    }

    public int getEmpleadoId() {return empleadoId;    }

    public void setEmpleadoId(int empleadoId) {this.empleadoId = empleadoId;    }

    public Timestamp getFechaInicio() {return fechaInicio;    }

    public void setFechaInicio(Timestamp fechaInicio) {this.fechaInicio = fechaInicio;    }

    public Timestamp getFechaFin() {return fechaFin;    }

    public void setFechaFin(Timestamp fechaFin) {this.fechaFin = fechaFin;    }

    public TDisponibilidad(){ };

    public TDisponibilidad(int empleadoId, Timestamp fechaInicio, Timestamp fechaFin){
        this.empleadoId = empleadoId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;

    }



}
