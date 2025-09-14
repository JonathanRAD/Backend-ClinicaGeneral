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
    private String token;
}