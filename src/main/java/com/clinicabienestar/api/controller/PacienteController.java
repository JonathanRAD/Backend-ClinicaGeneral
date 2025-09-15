// RUTA: src/main/java/com/clinicabienestar/api/controller/PacienteController.java

package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:4200") // ¡Importante! Permite la conexión desde Angular
public class PacienteController {

    @Autowired // Spring se encarga de inyectar una instancia del repositorio
    private PacienteRepository pacienteRepository;

    // Endpoint para OBTENER TODOS los pacientes (GET /api/pacientes)
    @GetMapping
    public List<Paciente> obtenerTodosLosPacientes() {
        return pacienteRepository.findAll();
    }

    // Endpoint para CREAR un nuevo paciente (POST /api/pacientes)
    @PostMapping
    public Paciente crearPaciente(@RequestBody Paciente paciente) {
        return pacienteRepository.save(paciente);
    }

    // Endpoint para ACTUALIZAR un paciente (PUT /api/pacientes/{id})
    @PutMapping("/{id}")
    public ResponseEntity<Paciente> actualizarPaciente(@PathVariable Long id, @RequestBody Paciente detallesPaciente) {
        return pacienteRepository.findById(id)
                .map(paciente -> {
                    paciente.setDni(detallesPaciente.getDni());
                    paciente.setNombres(detallesPaciente.getNombres());
                    paciente.setApellidos(detallesPaciente.getApellidos());
                    paciente.setFechaNacimiento(detallesPaciente.getFechaNacimiento());
                    paciente.setTelefono(detallesPaciente.getTelefono());
                    paciente.setPeso(detallesPaciente.getPeso());
                    paciente.setAltura(detallesPaciente.getAltura());
                    paciente.setRitmoCardiaco(detallesPaciente.getRitmoCardiaco());
                    Paciente pacienteActualizado = pacienteRepository.save(paciente);
                    return ResponseEntity.ok(pacienteActualizado);
                }).orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para ELIMINAR un paciente (DELETE /api/pacientes/{id})
    @DeleteMapping("/{id}")
public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) { // Renombrado para consistencia
    return pacienteRepository.findById(id)
            .map(paciente -> {
                pacienteRepository.delete(paciente);
                // Construimos explícitamente la respuesta correcta
                return ResponseEntity.noContent().<Void>build();
            }).orElse(ResponseEntity.notFound().build());
}
}