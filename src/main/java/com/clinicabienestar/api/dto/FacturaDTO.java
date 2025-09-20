package com.clinicabienestar.api.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class FacturaDTO {
    private Long citaId;
    private String estado;
    private Double montoPagado; // <-- NUEVO CAMPO
    private List<DetalleFacturaDTO> detalles = new ArrayList<>(); 
}