// RUTA: src/main/java/com/clinicabienestar/api/controller/PacienteController.java

package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.HistoriaClinica;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List; // Asegúrate de que este import exista

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {
    
    @Autowired
    private PacienteRepository pacienteRepository;

    // ESTE ES EL MÉTODO QUE FALTABA
    @GetMapping
    public List<Paciente> obtenerTodosLosPacientes() {
        return pacienteRepository.findAll();
    }
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('RECEPCIONISTA', 'ADMINISTRADOR')")
    public Paciente crearPaciente(@RequestBody Paciente paciente) {
        if (paciente.getHistoriaClinica() == null) {
            HistoriaClinica nuevaHistoria = new HistoriaClinica();
            nuevaHistoria.setFechaCreacion(LocalDate.now());
            
            paciente.setHistoriaClinica(nuevaHistoria);
            nuevaHistoria.setPaciente(paciente);
        }
        return pacienteRepository.save(paciente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> actualizarPaciente(@PathVariable Long id, @RequestBody Paciente detallesPaciente) {
        return pacienteRepository.findById(id)
                .map(paciente -> {
                    paciente.setDni(detallesPaciente.getDni());
                    paciente.setNombres(detallesPaciente.getNombres());
                    paciente.setApellidos(detallesPaciente.getApellidos());
                    paciente.setFechaNacimiento(detallesPaciente.getFechaNacimiento());
                    paciente.setTelefono(detallesPaciente.getTelefono());
                    paciente.setDireccion(detallesPaciente.getDireccion());
                    paciente.setPeso(detallesPaciente.getPeso());
                    paciente.setAltura(detallesPaciente.getAltura());
                    paciente.setRitmoCardiaco(detallesPaciente.getRitmoCardiaco());
                    Paciente pacienteActualizado = pacienteRepository.save(paciente);
                    return ResponseEntity.ok(pacienteActualizado);
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        return pacienteRepository.findById(id)
                .map(paciente -> {
                    pacienteRepository.delete(paciente);
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }
}