package com.clinicabienestar.api.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEstadisticasDTO {
    private long totalEventos;
    private long eventosHoy;
    private long eventosSemana;
    private Map<String, Long> eventosPorAccion;
    private Map<String, Long> eventosPorEntidad;
    private List<UsuarioActivoDTO> usuariosMasActivos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioActivoDTO {
        private String nombre;
        private String email;
        private long total;
    }
}
