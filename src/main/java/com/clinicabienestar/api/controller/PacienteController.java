// RUTA: src/main/java/com/clinicabienestar/api/controller/PacienteController.java

package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.HistoriaClinica;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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


    @GetMapping("/mi-perfil")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<Paciente> obtenerMiPerfil() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioActual = (Usuario) authentication.getPrincipal();

        return pacienteRepository.findByUsuarioId(usuarioActual.getId())
                .map(ResponseEntity::ok) // Si lo encuentra, devuelve 200 OK con el paciente
                .orElse(ResponseEntity.notFound().build()); // Si no, devuelve 404
    }
    @PutMapping("/mi-perfil")
    @PreAuthorize("hasAuthority('PACIENTE')")
    public ResponseEntity<Paciente> actualizarMiPerfil(@RequestBody Paciente detallesPaciente) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioActual = (Usuario) authentication.getPrincipal();

        return pacienteRepository.findByUsuarioId(usuarioActual.getId())
                .map(pacienteExistente -> {
                    // Actualizamos solo los campos que el paciente puede modificar
                    pacienteExistente.setDni(detallesPaciente.getDni());
                    pacienteExistente.setTelefono(detallesPaciente.getTelefono());
                    pacienteExistente.setDireccion(detallesPaciente.getDireccion());
                    pacienteExistente.setFechaNacimiento(detallesPaciente.getFechaNacimiento());
                    pacienteExistente.setPeso(detallesPaciente.getPeso());
                    pacienteExistente.setAltura(detallesPaciente.getAltura());
                    
                    Paciente pacienteActualizado = pacienteRepository.save(pacienteExistente);
                    return ResponseEntity.ok(pacienteActualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    // ESTE ES EL MÉTODO QUE FALTABA
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'RECEPCIONISTA', 'MEDICO')") // <-- AÑADE ESTA LÍNEA
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