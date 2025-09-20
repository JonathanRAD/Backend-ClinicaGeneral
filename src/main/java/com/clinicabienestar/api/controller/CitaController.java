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

import java.time.Instant;
import java.time.LocalDate; 
import java.time.ZoneId;
import java.util.List;
import java.time.temporal.ChronoUnit;

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
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));
        cita.setNumeroTurno(calcularNumeroTurno(medico.getId(), LocalDate.ofInstant(citaDTO.getFechaHora(), ZoneId.systemDefault())));

        Cita nuevaCita = citaRepository.save(cita);
        return ResponseEntity.ok(nuevaCita);
    }
    
    private String asignarConsultorio(String especialidad) {
        // Lógica expandida para asignar consultorios por área/piso
        return switch (especialidad.toLowerCase()) {
            // Piso 1: Atención Primaria y General
            case "medicina general" -> "Piso 1, Consultorio 102";
            case "dermatología" -> "Piso 1, Consultorio 105";

            // Piso 2: Atención Especializada Infantil y Femenina
            case "pediatría" -> "Piso 2, Consultorio 205";
            case "ginecología" -> "Piso 2, Consultorio 210";

            // Piso 3: Especialidades de Alta Complejidad
            case "cardiología" -> "Piso 3, Consultorio 301";
            case "neurología" -> "Piso 3, Consultorio 304";
            case "traumatología" -> "Piso 3, Consultorio 308";

            // Valor por defecto para cualquier otra especialidad no listada
            default -> "Piso 1, Admisión";
        };
    }

    private int calcularNumeroTurno(Long medicoId, LocalDate fechaCita) {
        // Define el inicio y el fin del día para la consulta a la base de datos
        Instant startOfDay = fechaCita.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = fechaCita.plus(1, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Usa el nuevo método del repositorio, que es mucho más eficiente y preciso
        long citasDelDia = citaRepository.countByMedicoAndDateRange(medicoId, startOfDay, endOfDay);
        
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