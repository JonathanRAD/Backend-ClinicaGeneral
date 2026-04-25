package com.clinicabienestar.api.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecetaDTO {
    private Long id;
    private LocalDateTime fechaEmision;
    private String indicacionesGenerales;
    private List<DetalleRecetaDTO> detalles;
}
