package com.clinicabienestar.api.dto;

import lombok.Data;

@Data
public class SeguroMedicoDTO {
    private String nombreAseguradora;
    private String numeroPoliza;
    private String cobertura;
}