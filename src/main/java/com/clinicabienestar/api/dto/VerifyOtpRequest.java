package com.clinicabienestar.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "El email es requerido.")
    @Email(message = "Email inválido.")
    private String email;

    @NotBlank(message = "El código OTP es requerido.")
    @Pattern(regexp = "\\d{6}", message = "El OTP debe ser de 6 dígitos numéricos.")
    private String otp;
}
