package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.service.MedicoService; 
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor 
@CrossOrigin(origins = "http://localhost:4200")
public class MedicoController {

    private final MedicoService medicoService; 

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Medico> obtenerTodosLosMedicos() {
        return medicoService.obtenerTodosLosMedicos();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Medico> obtenerMedicoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.obtenerMedicoPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('GESTIONAR_MEDICOS')")
    public ResponseEntity<Medico> crearMedico(@RequestBody Medico medico) {
        Medico nuevoMedico = medicoService.crearMedico(medico);
        return new ResponseEntity<>(nuevoMedico, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('GESTIONAR_MEDICOS')")
    public ResponseEntity<Medico> actualizarMedico(@PathVariable Long id, @RequestBody Medico detallesMedico) {
        Medico medicoActualizado = medicoService.actualizarMedico(id, detallesMedico);
        return ResponseEntity.ok(medicoActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('GESTIONAR_MEDICOS')")
    public ResponseEntity<Void> eliminarMedico(@PathVariable Long id) {
        medicoService.eliminarMedico(id);
        return ResponseEntity.noContent().build();
    }
}