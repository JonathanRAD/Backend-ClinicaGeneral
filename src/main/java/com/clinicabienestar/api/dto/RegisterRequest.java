// RUTA: src/main/java/com/clinicabienestar/api/dto/RegisterRequest.java
package com.clinicabienestar.api.dto;

import com.clinicabienestar.api.model.Rol;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String nombres;   // <-- NUEVO CAMPO
    private String apellidos; // <-- NUEVO CAMPO
    private Rol rol;
}