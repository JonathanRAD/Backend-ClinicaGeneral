// RUTA: src/main/java/com/clinicabienestar/api/dto/RegisterRequest.java
package com.clinicabienestar.api.dto;

import com.clinicabienestar.api.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String password;

    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombres;

    @NotBlank(message = "El apellido no puede estar vacío.")
    private String apellidos;

    private Rol rol;

    private Set<String> permisos;
}