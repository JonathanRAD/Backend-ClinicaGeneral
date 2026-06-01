package com.clinicabienestar.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class FacturaDTO {

    @NotNull(message = "El ID de la cita no puede ser nulo.")
    private Long citaId;
    
    @NotBlank(message = "El estado no puede estar vacío.")
    private String estado;
    
    private BigDecimal montoPagado; 
    
    private BigDecimal monto; 

    @NotNull(message = "La lista de detalles no puede ser nula.")
    @Valid
    private List<DetalleFacturaDTO> detalles = new ArrayList<>();
}