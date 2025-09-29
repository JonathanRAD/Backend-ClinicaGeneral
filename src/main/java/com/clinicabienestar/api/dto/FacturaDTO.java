package com.clinicabienestar.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class FacturaDTO {
    private Long citaId;
    private String estado;
    private BigDecimal montoPagado;
    private BigDecimal monto;
    private List<DetalleFacturaDTO> detalles = new ArrayList<>();
}