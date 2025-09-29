package com.clinicabienestar.api.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class CitaDTO {
    private Long pacienteId;
    private Long medicoId;
    private Instant fechaHora; 
    private String motivo;
}