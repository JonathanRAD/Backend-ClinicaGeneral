// RUTA: src/main/java/com/clinicabienestar/api/controller/CitaController.java

package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.Cita;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.repository.CitaRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:4200")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private MedicoRepository medicoRepository;

    @GetMapping
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAll();
    }

    // Usamos un Map para recibir los IDs y otros datos del frontend
    @PostMapping
    public ResponseEntity<Cita> crearCita(@RequestBody Map<String, Object> payload) {
        Long pacienteId = Long.parseLong(payload.get("pacienteId").toString());
        Long medicoId = Long.parseLong(payload.get("medicoId").toString());

        Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);
        Medico medico = medicoRepository.findById(medicoId).orElse(null);

        if (paciente == null || medico == null) {
            return ResponseEntity.badRequest().build();
        }

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.parse(payload.get("fechaHora").toString()));
        cita.setMotivo(payload.get("motivo").toString());
        cita.setEstado("programada");

        Cita nuevaCita = citaRepository.save(cita);
        return ResponseEntity.ok(nuevaCita);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizarCita(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Long pacienteId = Long.parseLong(payload.get("pacienteId").toString());
        Long medicoId = Long.parseLong(payload.get("medicoId").toString());

        Cita cita = citaRepository.findById(id).orElse(null);
        Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);
        Medico medico = medicoRepository.findById(medicoId).orElse(null);

        if (cita == null || paciente == null || medico == null) {
            return ResponseEntity.notFound().build();
        }

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.parse(payload.get("fechaHora").toString()));
        cita.setMotivo(payload.get("motivo").toString());
        // El estado podría venir en el payload si se quisiera cambiar
        // cita.setEstado(payload.get("estado").toString());

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