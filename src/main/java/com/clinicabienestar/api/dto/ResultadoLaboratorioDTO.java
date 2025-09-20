package com.clinicabienestar.api.dto;

import lombok.Data;

@Data
public class ResultadoLaboratorioDTO {
    private String descripcion;
    private String valores;
    private String conclusiones;
}