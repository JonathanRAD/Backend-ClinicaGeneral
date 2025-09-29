package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.CitaDTO;
import com.clinicabienestar.api.model.Cita;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.repository.CitaRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:4200")
public class CitaController {

    @Autowired private CitaRepository citaRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private MedicoRepository medicoRepository;

    @GetMapping
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Cita> crearCita(@RequestBody CitaDTO citaDTO) {
        Paciente paciente = pacienteRepository.findById(citaDTO.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Medico medico = medicoRepository.findById(citaDTO.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        
        LocalDateTime fechaHoraLocal = LocalDateTime.ofInstant(citaDTO.getFechaHora(), ZoneId.systemDefault());
        cita.setFechaHora(fechaHoraLocal);
        
        cita.setMotivo(citaDTO.getMotivo());
        cita.setEstado("programada");
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));
        cita.setNumeroTurno(calcularNumeroTurno(medico.getId(), fechaHoraLocal.toLocalDate()));

        Cita nuevaCita = citaRepository.save(cita);
        return ResponseEntity.ok(nuevaCita);
    }
    
    private String asignarConsultorio(String especialidad) {
        if (especialidad == null) return "Piso 1, Admisión";
        return switch (especialidad.toLowerCase()) {
            case "medicina general" -> "Piso 1, Consultorio 102";
            case "dermatología" -> "Piso 1, Consultorio 105";
            case "pediatría" -> "Piso 2, Consultorio 205";
            case "ginecología" -> "Piso 2, Consultorio 210";
            case "cardiología" -> "Piso 3, Consultorio 301";
            case "neurología" -> "Piso 3, Consultorio 304";
            case "traumatología" -> "Piso 3, Consultorio 308";
            default -> "Piso 1, Admisión";
        };
    }

    private int calcularNumeroTurno(Long medicoId, LocalDate fechaCita) {
        LocalDateTime startOfDay = fechaCita.atStartOfDay();
        LocalDateTime endOfDay = fechaCita.plusDays(1).atStartOfDay();
        long citasDelDia = citaRepository.countByMedicoAndDateRange(medicoId, startOfDay, endOfDay);
        return (int) citasDelDia + 1;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizarCita(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        Paciente paciente = pacienteRepository.findById(citaDTO.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Medico medico = medicoRepository.findById(citaDTO.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.ofInstant(citaDTO.getFechaHora(), ZoneId.systemDefault()));
        cita.setMotivo(citaDTO.getMotivo());
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));

        Cita citaActualizada = citaRepository.save(cita);
        return ResponseEntity.ok(citaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        return citaRepository.findById(id)
                .map(cita -> {
                    citaRepository.delete(cita);
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }
}