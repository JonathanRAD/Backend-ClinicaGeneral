package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.CitaDTO;
import com.clinicabienestar.api.model.Cita;
import com.clinicabienestar.api.service.CitaService;
import jakarta.validation.Valid; // <-- IMPORTAR
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CitaController {

    private final CitaService citaService;

    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<Cita>> obtenerMisCitas() {
        return ResponseEntity.ok(citaService.obtenerMisCitas());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public List<Cita> obtenerTodasLasCitas() {
        return citaService.obtenerTodasLasCitas();
    }

    @PostMapping("/agendar")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Cita> agendarCitaPaciente(@Valid @RequestBody CitaDTO citaDTO) {
        return new ResponseEntity<>(citaService.agendarCitaPaciente(citaDTO), HttpStatus.CREATED);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody CitaDTO citaDTO) {
        return new ResponseEntity<>(citaService.crearCita(citaDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public ResponseEntity<Cita> actualizarCita(@PathVariable Long id, @Valid @RequestBody CitaDTO citaDTO) {
        return ResponseEntity.ok(citaService.actualizarCita(id, citaDTO));
    }

    @DeleteMapping("/mis-citas/{id}")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> cancelarMiCita(@PathVariable Long id) {
        citaService.cancelarMiCita(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build();
    }
}