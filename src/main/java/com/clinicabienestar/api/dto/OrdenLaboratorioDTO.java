package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.NotBlank; 
import lombok.Data;

@Data
public class OrdenLaboratorioDTO {
    @NotBlank(message = "El tipo de examen no puede estar vacío.")
    private String tipoExamen;
    
    private String observaciones; 
}