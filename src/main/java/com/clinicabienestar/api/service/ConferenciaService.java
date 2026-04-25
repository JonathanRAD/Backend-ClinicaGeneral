package com.clinicabienestar.api.service;

import com.clinicabienestar.api.model.Conferencia;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.repository.ConferenciaRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConferenciaService {

    private final ConferenciaRepository conferenciaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

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
        
        return conferenciaRepository.save(nueva);
    }

    public Conferencia actualizarEstado(Long id, String estado) {
        Conferencia conferencia = conferenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conferencia no encontrada"));
        conferencia.setEstado(estado);
        return conferenciaRepository.save(conferencia);
    }

    public void eliminarConferencia(Long id) {
        conferenciaRepository.deleteById(id);
    }
}
