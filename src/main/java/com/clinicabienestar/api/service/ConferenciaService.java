package com.clinicabienestar.api.service;

import com.clinicabienestar.api.model.Conferencia;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.repository.ConferenciaRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.repository.PacienteRepository;
import com.clinicabienestar.api.model.AccionAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConferenciaService {

    private final ConferenciaRepository conferenciaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final AuditService auditService;

    public List<Conferencia> obtenerTodas() {
        return conferenciaRepository.findAll();
    }

    public List<Conferencia> obtenerPorPaciente(Long pacienteId) {
        return conferenciaRepository.findByPacienteId(pacienteId);
    }

    public List<Conferencia> obtenerPorMedico(Long medicoId) {
        return conferenciaRepository.findByMedicoId(medicoId);
    }

    public Conferencia programarConferencia(Conferencia conferenciaDTO) {
        Paciente paciente = pacienteRepository.findById(conferenciaDTO.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Medico medico = medicoRepository.findById(conferenciaDTO.getMedico().getId())
                .orElseThrow(() -> new RuntimeException("Medico no encontrado"));

        Conferencia nueva = new Conferencia();
        nueva.setPaciente(paciente);
        nueva.setMedico(medico);
        nueva.setFechaProgramada(conferenciaDTO.getFechaProgramada());
        nueva.setDuracionMinutos(conferenciaDTO.getDuracionMinutos());
        
        Conferencia guardada = conferenciaRepository.save(nueva);
        try {
            auditService.registrarEvento(AccionAudit.CREAR, "CONFERENCIA", guardada.getId(), "Programación de teleconferencia entre Paciente ID: " + paciente.getId() + " y Médico ID: " + medico.getId(), null, auditService.toJson(guardada));
        } catch (Exception e) {}
        return guardada;
    }

    public Conferencia actualizarEstado(Long id, String estado) {
        Conferencia conferencia = conferenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conferencia no encontrada"));

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(conferencia);
        } catch (Exception e) {}

        conferencia.setEstado(estado);
        Conferencia guardada = conferenciaRepository.save(conferencia);
        try {
            auditService.registrarEvento(AccionAudit.CAMBIAR_ESTADO, "CONFERENCIA", guardada.getId(), "Cambio de estado de teleconferencia a: " + estado, anteriorJson, auditService.toJson(guardada));
        } catch (Exception e) {}
        return guardada;
    }

    public void eliminarConferencia(Long id) {
        Conferencia conferencia = conferenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conferencia no encontrada"));

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(conferencia);
        } catch (Exception e) {}

        conferenciaRepository.deleteById(id);
        try {
            auditService.registrarEvento(AccionAudit.ELIMINAR, "CONFERENCIA", id, "Eliminación de teleconferencia (ID: " + id + ")", anteriorJson, null);
        } catch (Exception e) {}
    }
}
