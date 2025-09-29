package com.clinicabienestar.api.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DetalleFacturaDTO {
    private String descripcionServicio;
    private int cantidad;
    private BigDecimal precioUnitario;
}