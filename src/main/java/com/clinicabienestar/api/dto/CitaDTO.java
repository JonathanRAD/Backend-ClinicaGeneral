// RUTA: src/main/java/com/clinicabienestar/api/dto/CitaDTO.java

package com.clinicabienestar.api.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class CitaDTO {
    private Long pacienteId;
    private Long medicoId;
    private Instant fechaHora; // <-- 2. CAMBIA EL TIPO DE DATO A INSTANT
    private String motivo;
}