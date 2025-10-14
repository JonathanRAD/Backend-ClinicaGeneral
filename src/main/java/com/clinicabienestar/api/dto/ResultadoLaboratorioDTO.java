package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.NotBlank; 
import lombok.Data;
import java.time.LocalDate;

@Data
public class ResultadoLaboratorioDTO {
    private Long ordenId;
    private LocalDate fechaResultado;

    @NotBlank(message = "La descripción no puede estar vacía.")
    private String descripcion;
    
    @NotBlank(message = "Los valores no pueden estar vacíos.")
    private String valores;
    
    @NotBlank(message = "Las conclusiones no pueden estar vacías.")
    private String conclusiones;
}