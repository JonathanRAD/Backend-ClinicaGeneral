package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TriajeDTO {

    @NotNull(message = "El peso es obligatorio")
    @Min(value = 1, message = "El peso debe ser mayor a 0")
    private Double peso;

    @NotNull(message = "La altura es obligatoria")
    @Min(value = 1, message = "La altura debe ser mayor a 0")
    private Double altura;

    @NotNull(message = "La temperatura es obligatoria")
    @Min(value = 30, message = "Temperatura demasiado baja")
    @Max(value = 45, message = "Temperatura demasiado alta")
    private Double temperatura;

    @NotBlank(message = "La presión arterial es obligatoria")
    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "Formato de presión arterial incorrecto (ej. 120/80)")
    private String presionArterial;

    @NotNull(message = "El ritmo cardíaco es obligatorio")
    @Min(value = 0, message = "Ritmo cardíaco inválido")
    private Integer ritmoCardiaco;

    @NotNull(message = "La saturación de oxígeno es obligatoria")
    @Min(value = 0, message = "Saturación mínima 0")
    @Max(value = 100, message = "Saturación máxima 100")
    private Integer saturacionOxigeno;

    private Double nivelAzucar;

    @NotBlank(message = "El motivo de consulta es obligatorio")
    private String motivoConsulta;

    private String notasOpcionales;
}
