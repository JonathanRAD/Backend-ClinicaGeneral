// RUTA: src/main/java/com/clinicabienestar/api/model/Cita.java

package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;

    @ManyToOne // Anotación clave: Muchas citas pueden pertenecer a UN paciente
    @JoinColumn(name = "paciente_id") // Define la columna de la clave foránea
    private Paciente paciente;

    @ManyToOne // Muchas citas pueden ser atendidas por UN médico
    @JoinColumn(name = "medico_id")
    private Medico medico;
}