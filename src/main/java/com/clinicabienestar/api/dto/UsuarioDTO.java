// RUTA: src/main/java/com/clinicabienestar/api/dto/UsuarioDTO.java
package com.clinicabienestar.api.dto;

import com.clinicabienestar.api.model.Rol;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set; // <-- Importar Set

@Data
public class UsuarioDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private Rol rol;
    private LocalDateTime fechaRegistro; 

    private Set<String> permisos; // Se añade el nuevo campo
}