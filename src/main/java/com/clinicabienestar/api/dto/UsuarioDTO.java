package com.clinicabienestar.api.dto;

import com.clinicabienestar.api.model.Rol;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private Rol rol;
    private LocalDateTime fechaRegistro;
}