package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.Conferencia;
import com.clinicabienestar.api.service.ConferenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conferencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConferenciaController {

    private final ConferenciaService conferenciaService;

    @GetMapping
    public ResponseEntity<List<Conferencia>> obtenerTodas() {
        return ResponseEntity.ok(conferenciaService.obtenerTodas());
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<Conferencia>> obtenerPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(conferenciaService.obtenerPorPaciente(pacienteId));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<Conferencia>> obtenerPorMedico(@PathVariable Long medicoId) {
        return ResponseEntity.ok(conferenciaService.obtenerPorMedico(medicoId));
    }

    @PostMapping
    public ResponseEntity<Conferencia> programarConferencia(@RequestBody Conferencia conferencia) {
        return ResponseEntity.ok(conferenciaService.programarConferencia(conferencia));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Conferencia> actualizarEstado(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(conferenciaService.actualizarEstado(id, body.get("estado")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarConferencia(@PathVariable Long id) {
        conferenciaService.eliminarConferencia(id);
        return ResponseEntity.noContent().build();
    }
}
