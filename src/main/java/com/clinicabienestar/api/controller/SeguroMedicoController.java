// RUTA: src/main/java/com/clinicabienestar/api/controller/SeguroMedicoController.java

package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.SeguroMedicoDTO;
import com.clinicabienestar.api.model.SeguroMedico;
import com.clinicabienestar.api.repository.PacienteRepository;
import com.clinicabienestar.api.repository.SeguroMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; 
import java.util.Map;

@RestController
@RequestMapping("/api/seguros")
@CrossOrigin(origins = "http://localhost:4200")
public class SeguroMedicoController {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SeguroMedicoRepository seguroMedicoRepository;

    @PostMapping("/paciente/{pacienteId}")
    public ResponseEntity<SeguroMedico> guardarSeguro(@PathVariable Long pacienteId, @RequestBody SeguroMedicoDTO seguroDTO) {
        return pacienteRepository.findById(pacienteId).map(paciente -> {
            
            SeguroMedico seguro = paciente.getSeguroMedico();
            if (seguro == null) {
                seguro = new SeguroMedico();
                seguro.setPaciente(paciente);
            }

            seguro.setNombreAseguradora(seguroDTO.getNombreAseguradora());
            seguro.setNumeroPoliza(seguroDTO.getNumeroPoliza());
            seguro.setCobertura(seguroDTO.getCobertura());
            
            SeguroMedico seguroGuardado = seguroMedicoRepository.save(seguro);
            
            return ResponseEntity.ok(seguroGuardado);
            
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/validar/paciente/{pacienteId}")
    public ResponseEntity<Map<String, Object>> validarSeguroPorPaciente(@PathVariable Long pacienteId) {
        return seguroMedicoRepository.findByPacienteId(pacienteId).map(seguro -> {
            boolean esValido = seguro.getNumeroPoliza() != null && seguro.getNumeroPoliza().toUpperCase().contains("ACTIVA");
            String mensaje = esValido ? "La póliza de seguro es válida y tiene cobertura." : "La póliza no se encuentra activa o no tiene cobertura.";
            
            Map<String, Object> response = new HashMap<>();
            response.put("valido", esValido);
            response.put("mensaje", mensaje);
            
            return ResponseEntity.ok(response);

        }).orElse(ResponseEntity.notFound().build());
    }
}