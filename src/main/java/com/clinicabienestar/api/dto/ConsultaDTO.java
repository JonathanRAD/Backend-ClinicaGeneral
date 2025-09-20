// RUTA: src/main/java/com/clinicabienestar/api/dto/ConsultaDTO.java

package com.clinicabienestar.api.dto;

import lombok.Data;

@Data
public class ConsultaDTO {
    private String motivo;
    private String diagnostico;
    private String tratamiento;
    private Long medicoId;
}