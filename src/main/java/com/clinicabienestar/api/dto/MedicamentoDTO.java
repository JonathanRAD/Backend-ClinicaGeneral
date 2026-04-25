package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class MedicamentoDTO {

    private Long id;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "La forma farmacéutica es obligatoria")
    private String formaFarmaceutica;

    @NotBlank(message = "La concentración es obligatoria")
    private String concentracion;

    @Min(value = 0, message = "El precio unitario no puede ser negativo")
    private Double precioUnitario;

    private String estado;
    
    // Lista de lotes para cuando se cree un medicamento con su primer stock
    private List<LoteMedicamentoDTO> lotes;
}
