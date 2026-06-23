package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.AuditEstadisticasDTO;
import com.clinicabienestar.api.dto.AuditLogDTO;
import com.clinicabienestar.api.dto.AuditPageResponse;
import com.clinicabienestar.api.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('VER_AUDITORIA')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<AuditPageResponse> obtenerLogs(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditService.obtenerLogs(usuarioId, rol, accion, entidad, fechaInicio, fechaFin, busqueda, pageable));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<AuditEstadisticasDTO> obtenerEstadisticas() {
        return ResponseEntity.ok(auditService.obtenerEstadisticas());
    }

    @GetMapping("/actividad-reciente")
    public ResponseEntity<List<AuditLogDTO>> obtenerActividadReciente() {
        return ResponseEntity.ok(auditService.obtenerActividadReciente());
    }
}
