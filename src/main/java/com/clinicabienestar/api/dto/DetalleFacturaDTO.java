package com.clinicabienestar.api.dto;

import lombok.Data;

@Data
public class DetalleFacturaDTO {
    private String descripcionServicio;
    private int cantidad;
    private Double precioUnitario;
}