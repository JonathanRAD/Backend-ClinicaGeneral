// RUTA CORREGIDA: src/main/java/com/clinicabienestar/api/service/SeguroMedicoService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.SeguroMedicoDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.SeguroMedico;
import com.clinicabienestar.api.repository.PacienteRepository;
import com.clinicabienestar.api.repository.SeguroMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SeguroMedicoService {

    private final PacienteRepository pacienteRepository;
    private final SeguroMedicoRepository seguroMedicoRepository;

    public SeguroMedico guardarSeguro(Long pacienteId, SeguroMedicoDTO seguroDTO) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + pacienteId));

        SeguroMedico seguro = Optional.ofNullable(paciente.getSeguroMedico()).orElse(new SeguroMedico());

        seguro.setNombreAseguradora(seguroDTO.getNombreAseguradora());
        seguro.setNumeroPoliza(seguroDTO.getNumeroPoliza());
        seguro.setCobertura(seguroDTO.getCobertura());
        seguro.setPaciente(paciente);    
        paciente.setSeguroMedico(seguro);  

        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        return pacienteGuardado.getSeguroMedico();
    }

    public SeguroMedico actualizarSeguro(Long seguroId, SeguroMedicoDTO seguroDTO) {
        SeguroMedico seguro = seguroMedicoRepository.findById(seguroId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguro Médico no encontrado con ID: " + seguroId));
        
        seguro.setNombreAseguradora(seguroDTO.getNombreAseguradora());
        seguro.setNumeroPoliza(seguroDTO.getNumeroPoliza());
        seguro.setCobertura(seguroDTO.getCobertura());
        
        return seguroMedicoRepository.save(seguro);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> validarSeguroPorPaciente(Long pacienteId) {
        return seguroMedicoRepository.findByPacienteId(pacienteId)
            .map(seguro -> {
                boolean esValido = seguro.getNumeroPoliza() != null && seguro.getNumeroPoliza().toUpperCase().contains("ACTIVA");
                String mensaje = esValido ? "La póliza de seguro es válida y tiene cobertura." : "La póliza no se encuentra activa o no tiene cobertura.";
                
                Map<String, Object> response = new HashMap<>();
                response.put("valido", esValido);
                response.put("mensaje", mensaje);
                return response;
            })
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró información del seguro."));
    }
}