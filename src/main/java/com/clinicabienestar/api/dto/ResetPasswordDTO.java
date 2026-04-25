package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "El token no puede estar vacío.")
    private String token;

    @NotBlank(message = "La nueva contraseña no puede estar vacía.")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres.")
    private String newPassword;
}