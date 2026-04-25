package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsultaDTO {
    @NotBlank(message = "El motivo de la consulta no puede estar vacío.")
    private String motivo;

    @NotBlank(message = "El diagnóstico no puede estar vacío.")
    private String diagnostico;

    @NotBlank(message = "El tratamiento no puede estar vacío.")
    private String tratamiento;

    @NotNull(message = "El ID del médico no puede ser nulo.")
    private Long medicoId;
}