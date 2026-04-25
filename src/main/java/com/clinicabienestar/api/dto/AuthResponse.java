// RUTA: src/main/java/com/clinicabienestar/api/dto/AuthResponse.java
package com.clinicabienestar.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    /** JWT token — presente solo cuando el usuario está completamente autenticado. */
    private String token;

    /**
     * true → el backend generó un OTP y lo envió por email; el frontend debe
     * mostrar la pantalla de verificación OTP antes de entregar el token.
     */
    private Boolean requiresOtp;

    /** Email del usuario pendiente de verificación (necesario para el flujo OTP). */
    private String email;
}