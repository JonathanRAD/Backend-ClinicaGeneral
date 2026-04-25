package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Body del DELETE /api/usuarios/me para confirmar la eliminación con OTP.
 */
@Data
public class EliminarCuentaRequest {

    @NotBlank(message = "El código OTP es requerido.")
    @Pattern(regexp = "\\d{6}", message = "El OTP debe ser de 6 dígitos numéricos.")
    private String otp;
}
