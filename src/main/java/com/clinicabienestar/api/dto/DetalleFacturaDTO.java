package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class DetalleFacturaDTO {
    
    @NotBlank(message = "La descripción del servicio no puede estar vacía.")
    private String descripcionServicio;

    @NotNull(message = "La cantidad no puede ser nula.")
    @Min(value = 1, message = "La cantidad debe ser como mínimo 1.")
    private int cantidad;

    @NotNull(message = "El precio unitario no puede ser nulo.")
    @Min(value = 0, message = "El precio unitario no puede ser negativo.")
    private BigDecimal precioUnitario;
}