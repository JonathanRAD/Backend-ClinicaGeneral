package com.clinicabienestar.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsultaResponseDTO {
    private Long id;
    private LocalDateTime fechaConsulta;
    private String motivo;
    private String diagnostico;
    private String tratamiento;
    private MedicoDTO medico;
}