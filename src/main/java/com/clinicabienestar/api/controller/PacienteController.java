// RUTA: src/main/java/com/clinicabienestar/api/controller/PacienteController.java

package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.HistoriaClinica;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.clinicabienestar.api.repository.PacienteRepository;
import com.clinicabienestar.api.dto.PacienteDTO; 
import com.clinicabienestar.api.dto.SeguroMedicoDTO; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {
    
    @Autowired
    private PacienteRepository pacienteRepository;


    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Paciente> obtenerMiPerfil() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioActual = (Usuario) authentication.getPrincipal();

        return pacienteRepository.findByUsuarioId(usuarioActual.getId())
                .map(ResponseEntity::ok) // Si lo encuentra, devuelve 200 OK con el paciente
                .orElse(ResponseEntity.notFound().build()); // Si no, devuelve 404
    }
    @PutMapping("/mi-perfil")
    @PreAuthorize("hasRole('PACIENTE')")
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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'MEDICO')")
    public List<PacienteDTO> obtenerTodosLosPacientes() {
        return pacienteRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    private PacienteDTO convertirADTO(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setId(paciente.getId());
        dto.setNombres(paciente.getNombres());
        dto.setApellidos(paciente.getApellidos());
        dto.setDni(paciente.getDni());
        dto.setTelefono(paciente.getTelefono());
        dto.setFechaNacimiento(paciente.getFechaNacimiento());
        dto.setPeso(paciente.getPeso());
        dto.setAltura(paciente.getAltura());
        dto.setRitmoCardiaco(paciente.getRitmoCardiaco());
        if (paciente.getSeguroMedico() != null) {
            SeguroMedicoDTO seguroDTO = new SeguroMedicoDTO();
            seguroDTO.setNombreAseguradora(paciente.getSeguroMedico().getNombreAseguradora());
            seguroDTO.setNumeroPoliza(paciente.getSeguroMedico().getNumeroPoliza());
            dto.setSeguroMedico(seguroDTO);
        }
        return dto;
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMINISTRADOR')")
    public Paciente crearPaciente(@RequestBody Paciente pacienteDetails) {
        Paciente nuevoPaciente = new Paciente();
        nuevoPaciente.setDni(pacienteDetails.getDni());
        nuevoPaciente.setNombres(pacienteDetails.getNombres());
        nuevoPaciente.setApellidos(pacienteDetails.getApellidos());
        nuevoPaciente.setTelefono(pacienteDetails.getTelefono());
        nuevoPaciente.setDireccion(pacienteDetails.getDireccion());
        nuevoPaciente.setFechaNacimiento(pacienteDetails.getFechaNacimiento());
        nuevoPaciente.setPeso(pacienteDetails.getPeso());
        nuevoPaciente.setAltura(pacienteDetails.getAltura());
        nuevoPaciente.setRitmoCardiaco(pacienteDetails.getRitmoCardiaco());

        HistoriaClinica nuevaHistoria = new HistoriaClinica();
        nuevaHistoria.setFechaCreacion(LocalDate.now());
        nuevoPaciente.setHistoriaClinica(nuevaHistoria);
        nuevaHistoria.setPaciente(nuevoPaciente);
        
        return pacienteRepository.save(nuevoPaciente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA', 'MEDICO')")
    public ResponseEntity<Paciente> actualizarPaciente(@PathVariable Long id, @RequestBody Paciente detallesPaciente) {
        return pacienteRepository.findById(id)
                .map(pacienteExistente -> {
                    pacienteExistente.setDni(detallesPaciente.getDni());
                    pacienteExistente.setNombres(detallesPaciente.getNombres());
                    pacienteExistente.setApellidos(detallesPaciente.getApellidos());
                    pacienteExistente.setFechaNacimiento(detallesPaciente.getFechaNacimiento());
                    pacienteExistente.setTelefono(detallesPaciente.getTelefono());
                    pacienteExistente.setDireccion(detallesPaciente.getDireccion());
                    pacienteExistente.setPeso(detallesPaciente.getPeso());
                    pacienteExistente.setAltura(detallesPaciente.getAltura());
                    pacienteExistente.setRitmoCardiaco(detallesPaciente.getRitmoCardiaco());          
                    Paciente pacienteActualizado = pacienteRepository.save(pacienteExistente);
                    return ResponseEntity.ok(pacienteActualizado);
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        return pacienteRepository.findById(id)
                .map(paciente -> {
                    pacienteRepository.delete(paciente);
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }
}