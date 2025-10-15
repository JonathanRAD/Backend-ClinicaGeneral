package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.SeguroMedicoDTO;
import com.clinicabienestar.api.model.SeguroMedico;
import com.clinicabienestar.api.service.SeguroMedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seguros")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SeguroMedicoController {

    private final SeguroMedicoService seguroMedicoService;

    @PostMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA') or hasAuthority('GESTIONAR_PACIENTES')")
    public ResponseEntity<SeguroMedico> guardarSeguro(@PathVariable Long pacienteId, @RequestBody SeguroMedicoDTO seguroDTO) {
        SeguroMedico seguroGuardado = seguroMedicoService.guardarSeguro(pacienteId, seguroDTO);
        return ResponseEntity.ok(seguroGuardado);
    }
    
    @PutMapping("/{seguroId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA') or hasAuthority('GESTIONAR_PACIENTES')")
    public ResponseEntity<SeguroMedico> actualizarSeguro(@PathVariable Long seguroId, @RequestBody SeguroMedicoDTO seguroDTO) {
        SeguroMedico seguroActualizado = seguroMedicoService.actualizarSeguro(seguroId, seguroDTO);
        return ResponseEntity.ok(seguroActualizado);
    }

    @GetMapping("/validar/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'MEDICO', 'CAJERO') or hasAuthority('VER_PACIENTES')")
    public ResponseEntity<Map<String, Object>> validarSeguroPorPaciente(@PathVariable Long pacienteId) {
        Map<String, Object> response = seguroMedicoService.validarSeguroPorPaciente(pacienteId);
        return ResponseEntity.ok(response);
    }
}