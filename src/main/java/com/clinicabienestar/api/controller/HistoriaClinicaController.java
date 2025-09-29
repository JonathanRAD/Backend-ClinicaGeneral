package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.ConsultaDTO;
import com.clinicabienestar.api.dto.HistoriaClinicaDTO;
import com.clinicabienestar.api.dto.PacienteDTO;
import com.clinicabienestar.api.model.Consulta;
import com.clinicabienestar.api.model.HistoriaClinica;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.repository.ConsultaRepository;
import com.clinicabienestar.api.repository.HistoriaClinicaRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinicabienestar.api.dto.SeguroMedicoDTO; 
import com.clinicabienestar.api.model.SeguroMedico;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/historias")
@CrossOrigin(origins = "http://localhost:4200")
public class HistoriaClinicaController {

    @Autowired private HistoriaClinicaRepository historiaClinicaRepository;
    @Autowired private ConsultaRepository consultaRepository;
    @Autowired private MedicoRepository medicoRepository;
    @Autowired private PacienteRepository pacienteRepository;

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<HistoriaClinicaDTO> obtenerHistoriaPorPacienteId(@PathVariable Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
            .map(paciente -> {
                HistoriaClinica historia = paciente.getHistoriaClinica();
                if (historia == null) {
                    return ResponseEntity.notFound().<HistoriaClinicaDTO>build();
                }

                PacienteDTO pacienteDTO = new PacienteDTO();
                pacienteDTO.setId(paciente.getId());
                pacienteDTO.setNombres(paciente.getNombres());
                pacienteDTO.setApellidos(paciente.getApellidos());
                pacienteDTO.setDni(paciente.getDni());
                pacienteDTO.setTelefono(paciente.getTelefono());

                SeguroMedico seguro = paciente.getSeguroMedico();
                if (seguro != null) {
                    SeguroMedicoDTO seguroDTO = new SeguroMedicoDTO();
                    seguroDTO.setNombreAseguradora(seguro.getNombreAseguradora());
                    seguroDTO.setNumeroPoliza(seguro.getNumeroPoliza());
                    seguroDTO.setCobertura(seguro.getCobertura());
                    pacienteDTO.setSeguroMedico(seguroDTO);
                }

                HistoriaClinicaDTO responseDTO = new HistoriaClinicaDTO();
                responseDTO.setId(historia.getId());
                responseDTO.setFechaCreacion(historia.getFechaCreacion());
                responseDTO.setAntecedentes(historia.getAntecedentes());
                responseDTO.setAlergias(historia.getAlergias());
                responseDTO.setEnfermedadesCronicas(historia.getEnfermedadesCronicas());
                responseDTO.setConsultas(historia.getConsultas());
                responseDTO.setPaciente(pacienteDTO);

                return ResponseEntity.ok(responseDTO);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HistoriaClinica> actualizarHistoriaClinica(@PathVariable Long id, @RequestBody HistoriaClinicaDTO historiaDTO) {
        return historiaClinicaRepository.findById(id)
                .map(historia -> {
                    historia.setAntecedentes(historiaDTO.getAntecedentes());
                    historia.setAlergias(historiaDTO.getAlergias());
                    historia.setEnfermedadesCronicas(historiaDTO.getEnfermedadesCronicas());
                    HistoriaClinica historiaActualizada = historiaClinicaRepository.save(historia);
                    return ResponseEntity.ok(historiaActualizada);
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{historiaId}/consultas")
    public ResponseEntity<Consulta> agregarConsulta(@PathVariable Long historiaId, @RequestBody ConsultaDTO consultaDTO) {
        return historiaClinicaRepository.findById(historiaId).map(historia -> {
            Medico medico = medicoRepository.findById(consultaDTO.getMedicoId())
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

            Consulta nuevaConsulta = new Consulta();
            nuevaConsulta.setFechaConsulta(LocalDateTime.now());
            nuevaConsulta.setMotivo(consultaDTO.getMotivo());
            nuevaConsulta.setDiagnostico(consultaDTO.getDiagnostico());
            nuevaConsulta.setTratamiento(consultaDTO.getTratamiento());
            nuevaConsulta.setMedico(medico);
            nuevaConsulta.setHistoriaClinica(historia);

            Consulta consultaGuardada = consultaRepository.save(nuevaConsulta);
            return ResponseEntity.ok(consultaGuardada);
        }).orElse(ResponseEntity.notFound().build());
    }
}