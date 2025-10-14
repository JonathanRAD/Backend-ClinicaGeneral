// RUTA MODIFICADA: src/main/java/com/clinicabienestar/api/controller/HistoriaClinicaController.java
package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.ConsultaDTO;
import com.clinicabienestar.api.dto.HistoriaClinicaDTO;
import com.clinicabienestar.api.model.Consulta;
import com.clinicabienestar.api.model.HistoriaClinica;
import com.clinicabienestar.api.service.HistoriaClinicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/historias")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;

    @GetMapping("/mi-historial")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<HistoriaClinicaDTO> obtenerMiHistoriaClinica() {
        return ResponseEntity.ok(historiaClinicaService.obtenerMiHistoriaClinica());
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO', 'RECEPCIONISTA')")
    public ResponseEntity<HistoriaClinicaDTO> obtenerHistoriaPorPacienteId(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(historiaClinicaService.obtenerHistoriaPorPacienteId(pacienteId));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<HistoriaClinica> actualizarHistoriaClinica(@PathVariable Long id, @RequestBody HistoriaClinicaDTO historiaDTO) {
        return ResponseEntity.ok(historiaClinicaService.actualizarHistoriaClinica(id, historiaDTO));
    }

    @PostMapping("/{historiaId}/consultas")
    // --- CAMBIA ESTA LÍNEA ---
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<Consulta> agregarConsulta(@PathVariable Long historiaId, @RequestBody ConsultaDTO consultaDTO) {
        Consulta consultaGuardada = historiaClinicaService.agregarConsulta(historiaId, consultaDTO);
        return ResponseEntity.ok(consultaGuardada);
    }
}