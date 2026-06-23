package com.clinicabienestar.api.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private LocalDateTime fecha;
    private Long usuarioId;
    private String usuarioEmail;
    private String usuarioNombre;
    private String rol;
    private String accion;
    private String entidad;
    private Long entidadId;
    private String descripcion;
    private String datosAnteriores;
    private String datosNuevos;
    private String ipAddress;
}
