package com.clinicabienestar.api.dto;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;

@Data
public class CitaDTO {
    
    @NotNull(message = "El ID del paciente no puede ser nulo.")
    private Long pacienteId;

    @NotNull(message = "El ID del médico no puede ser nulo.")
    private Long medicoId;

    @NotNull(message = "La fecha y hora no pueden ser nulas.")
    @Future(message = "La fecha de la cita debe ser en el futuro.")
    private Instant fechaHora; 
    
    @NotBlank(message = "El motivo de la cita no puede estar vacío.")
    private String motivo;
}