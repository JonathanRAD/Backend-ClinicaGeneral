// RUTA: src/main/java/com/clinicabienestar/api/service/HistoriaClinicaService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.*;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.*;
import com.clinicabienestar.api.repository.ConsultaRepository;
import com.clinicabienestar.api.repository.HistoriaClinicaRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;

    private Usuario getUsuarioActual() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public HistoriaClinicaDTO obtenerMiHistoriaClinica() {
        Long usuarioId = getUsuarioActual().getId();
        Paciente paciente = pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado para el usuario actual"));
        
        return obtenerHistoriaPorPacienteId(paciente.getId());
    }

    @Transactional(readOnly = true)
    public HistoriaClinicaDTO obtenerHistoriaPorPacienteId(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + pacienteId));
        
        HistoriaClinica historia = paciente.getHistoriaClinica();
        if (historia == null) {
            throw new ResourceNotFoundException("Historia clínica no encontrada para el paciente con ID: " + pacienteId);
        }

        return convertirAHistoriaClinicaDTO(historia, paciente);
    }
    
    public HistoriaClinica actualizarHistoriaClinica(Long id, HistoriaClinicaDTO historiaDTO) {
        HistoriaClinica historia = historiaClinicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historia Clínica no encontrada con ID: " + id));

        historia.setAntecedentes(historiaDTO.getAntecedentes());
        historia.setAlergias(historiaDTO.getAlergias());
        historia.setEnfermedadesCronicas(historiaDTO.getEnfermedadesCronicas());
        
        return historiaClinicaRepository.save(historia);
    }

    public Consulta agregarConsulta(Long historiaId, ConsultaDTO consultaDTO) {
        HistoriaClinica historia = historiaClinicaRepository.findById(historiaId)
            .orElseThrow(() -> new ResourceNotFoundException("Historia Clínica no encontrada con ID: " + historiaId));
        
        Medico medico = medicoRepository.findById(consultaDTO.getMedicoId())
            .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con ID: " + consultaDTO.getMedicoId()));

        Consulta nuevaConsulta = new Consulta();
        nuevaConsulta.setFechaConsulta(LocalDateTime.now());
        nuevaConsulta.setMotivo(consultaDTO.getMotivo());
        nuevaConsulta.setDiagnostico(consultaDTO.getDiagnostico());
        nuevaConsulta.setTratamiento(consultaDTO.getTratamiento());
        
        // Manejo de la relación bidireccional
        nuevaConsulta.setMedico(medico);
        nuevaConsulta.setHistoriaClinica(historia); // Lado "dueño" de la relación
        historia.getConsultas().add(nuevaConsulta); // Actualizamos el lado inverso

        return consultaRepository.save(nuevaConsulta);
    }
    
    // Método privado para el mapeo/ensamblaje del DTO
    private HistoriaClinicaDTO convertirAHistoriaClinicaDTO(HistoriaClinica historia, Paciente paciente) {
        // Mapeo del Paciente a PacienteDTO
        PacienteDTO pacienteDTO = new PacienteDTO();
        pacienteDTO.setId(paciente.getId());
        pacienteDTO.setNombres(paciente.getNombres());
        pacienteDTO.setApellidos(paciente.getApellidos());
        pacienteDTO.setDni(paciente.getDni());
        pacienteDTO.setTelefono(paciente.getTelefono());

        // Mapeo del Seguro (si existe)
        SeguroMedico seguro = paciente.getSeguroMedico();
        if (seguro != null) {
            SeguroMedicoDTO seguroDTO = new SeguroMedicoDTO();
            seguroDTO.setNombreAseguradora(seguro.getNombreAseguradora());
            seguroDTO.setNumeroPoliza(seguro.getNumeroPoliza());
            seguroDTO.setCobertura(seguro.getCobertura());
            pacienteDTO.setSeguroMedico(seguroDTO);
        }

        // Ensamblaje final de HistoriaClinicaDTO
        HistoriaClinicaDTO responseDTO = new HistoriaClinicaDTO();
        responseDTO.setId(historia.getId());
        responseDTO.setFechaCreacion(historia.getFechaCreacion());
        responseDTO.setAntecedentes(historia.getAntecedentes());
        responseDTO.setAlergias(historia.getAlergias());
        responseDTO.setEnfermedadesCronicas(historia.getEnfermedadesCronicas());
        responseDTO.setConsultas(historia.getConsultas()); // Se puede mantener la entidad completa si es necesario
        responseDTO.setPaciente(pacienteDTO);

        return responseDTO;
    }
}