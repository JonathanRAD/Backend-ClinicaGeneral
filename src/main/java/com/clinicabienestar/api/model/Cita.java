// RUTA: src/main/java/com/clinicabienestar/api/model/Cita.java

package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant; 

@Entity
@Data
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant fechaHora; 
    private String motivo;
    private String estado;
    private String consultorio;
    private Integer numeroTurno; 

    @ManyToOne 
    @JoinColumn(name = "paciente_id") 
    private Paciente paciente;

    @ManyToOne 
    @JoinColumn(name = "medico_id")
    private Medico medico;
}