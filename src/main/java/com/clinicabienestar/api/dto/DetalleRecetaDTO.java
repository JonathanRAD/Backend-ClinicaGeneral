package com.clinicabienestar.api.dto;

import lombok.Data;

@Data
public class DetalleRecetaDTO {
    private Long id;
    private Long medicamentoId; // Solo mandamos el ID por practicidad
    private String nombreMedicamento; // Para retonar en GET
    private String codigoMedicamento;
    private Integer cantidadSolicitada;
    private String dosis;
    private String frecuencia;
    private String duracion;
}
