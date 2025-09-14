// RUTA: src/main/java/com/clinicabienestar/api/dto/FacturaDTO.java

package com.clinicabienestar.api.dto;

import lombok.Data;

@Data
public class FacturaDTO {
    private Long citaId;
    private Double monto;
    private String estado;
}