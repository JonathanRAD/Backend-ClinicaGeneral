package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoteMedicamentoDTO {

    private Long id;

    @NotBlank(message = "El nro de lote es obligatorio")
    private String numeroLote;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 de stock")
    private Integer stock;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;
}
