// RUTA: src/main/java/com/clinicabienestar/api/dto/ResultadoLaboratorioDTO.java
package com.clinicabienestar.api.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ResultadoLaboratorioDTO {
    private Long ordenId;
    private LocalDate fechaResultado;
    private String descripcion; // <-- Corregido para coincidir
    private String valores;     // <-- Corregido para coincidir
    private String conclusiones; // <-- Corregido para coincidir
}