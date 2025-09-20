// RUTA: src/main/java/com/clinicabienestar/api/controller/CitaController.java

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

import java.time.LocalDate; // <-- AÑADIR IMPORT
import java.time.ZoneId; // <-- AÑADIR IMPORT
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

    // --- MÉTODO POST MODIFICADO CON LÓGICA DE TURNOS ---
    @PostMapping
    public ResponseEntity<Cita> crearCita(@RequestBody CitaDTO citaDTO) {
        Paciente paciente = pacienteRepository.findById(citaDTO.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Medico medico = medicoRepository.findById(citaDTO.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHora(citaDTO.getFechaHora());
        cita.setMotivo(citaDTO.getMotivo());
        cita.setEstado("programada");

        // --- LÓGICA DE ASIGNACIÓN AUTOMÁTICA (RF-05) ---
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));
        cita.setNumeroTurno(calcularNumeroTurno(medico.getId(), LocalDate.ofInstant(citaDTO.getFechaHora(), ZoneId.systemDefault())));
        // --- FIN DE LA LÓGICA ---

        Cita nuevaCita = citaRepository.save(cita);
        return ResponseEntity.ok(nuevaCita);
    }
    
    // --- MÉTODOS PRIVADOS DE AYUDA ---

    private String asignarConsultorio(String especialidad) {
        // Lógica simple de ejemplo. Esto podría venir de otra tabla en el futuro.
        return switch (especialidad.toLowerCase()) {
            case "cardiología" -> "Piso 3, Consultorio 301";
            case "pediatría" -> "Piso 2, Consultorio 205";
            case "medicina general" -> "Piso 1, Consultorio 102";
            default -> "Piso 1, Admisión";
        };
    }

    private int calcularNumeroTurno(Long medicoId, LocalDate fechaCita) {
        // Contamos cuántas citas tiene el médico para la fecha dada
        long citasDelDia = citaRepository.findAll().stream()
                .filter(c -> c.getMedico().getId().equals(medicoId) &&
                             LocalDate.ofInstant(c.getFechaHora(), ZoneId.systemDefault()).equals(fechaCita))
                .count();
        // El nuevo turno es el número de citas existentes + 1
        return (int) citasDelDia + 1;
    }

    // ... (El resto de la clase, como PUT y DELETE, se mantienen igual por ahora)
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
        cita.setFechaHora(citaDTO.getFechaHora());
        cita.setMotivo(citaDTO.getMotivo());

        // Podríamos recalcular el turno y consultorio si la fecha o el médico cambian
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));
        // NOTA: La lógica de recalcular el turno si cambia la fecha puede ser más compleja,
        // por ahora lo mantenemos simple.

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