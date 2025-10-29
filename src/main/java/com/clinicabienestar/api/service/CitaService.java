// RUTA: src/main/java/com/clinicabienestar/api/service/CitaService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.CitaDTO;
import com.clinicabienestar.api.dto.AgendarCitaPacienteDTO;
import com.clinicabienestar.api.exception.ForbiddenException;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Cita;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.CitaRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    private Usuario getUsuarioActual() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Cita> obtenerMisCitas() {
        Long usuarioId = getUsuarioActual().getId();
        return citaRepository.findByPacienteUsuarioId(usuarioId);
    }
    
    public Cita agendarCitaPaciente(CitaDTO citaDTO) {
        Long usuarioId = getUsuarioActual().getId();
        Paciente paciente = pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado para el usuario actual"));
        
        return crearNuevaCita(citaDTO, paciente);
    }
    public Cita agendarCitaPaciente(AgendarCitaPacienteDTO citaDTO) {
        Long usuarioId = getUsuarioActual().getId();
        Paciente paciente = pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado para el usuario actual"));
        
        CitaDTO citaCompleta = new CitaDTO();
        citaCompleta.setPacienteId(paciente.getId());
        citaCompleta.setMedicoId(citaDTO.getMedicoId());
        citaCompleta.setFechaHora(citaDTO.getFechaHora());
        citaCompleta.setMotivo(citaDTO.getMotivo());

        return crearNuevaCita(citaCompleta, paciente);
    }

    public Cita crearCita(CitaDTO citaDTO) {
        Paciente paciente = pacienteRepository.findById(citaDTO.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + citaDTO.getPacienteId()));
        
        return crearNuevaCita(citaDTO, paciente);
    }

    public Cita actualizarCita(Long id, CitaDTO citaDTO) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));
        
        // Aquí presto atención a las relaciones que mencionaste.
        Paciente paciente = pacienteRepository.findById(citaDTO.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + citaDTO.getPacienteId()));
        Medico medico = medicoRepository.findById(citaDTO.getMedicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con ID: " + citaDTO.getMedicoId()));

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.ofInstant(citaDTO.getFechaHora(), ZoneId.systemDefault()));
        cita.setMotivo(citaDTO.getMotivo());
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));

        return citaRepository.save(cita);
    }

    public void cancelarMiCita(Long id) {
        Long usuarioId = getUsuarioActual().getId();
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

        // Verificación de seguridad clave
        if (!cita.getPaciente().getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenException("No tiene permiso para cancelar una cita que no es suya.");
        }

        citaRepository.delete(cita);
    }

    public void eliminarCita(Long id) {
        if (!citaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cita no encontrada con ID: " + id);
        }
        citaRepository.deleteById(id);
    }

    // --- Métodos privados de lógica de negocio ---

    private Cita crearNuevaCita(CitaDTO citaDTO, Paciente paciente) {
        Medico medico = medicoRepository.findById(citaDTO.getMedicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con ID: " + citaDTO.getMedicoId()));

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        
        LocalDateTime fechaHoraLocal = LocalDateTime.ofInstant(citaDTO.getFechaHora(), ZoneId.systemDefault());
        cita.setFechaHora(fechaHoraLocal);
        
        cita.setMotivo(citaDTO.getMotivo());
        cita.setEstado("programada");
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));
        cita.setNumeroTurno(calcularNumeroTurno(medico.getId(), fechaHoraLocal.toLocalDate()));

        return citaRepository.save(cita);
    }

    private String asignarConsultorio(String especialidad) {
        if (especialidad == null) return "Piso 1, Admisión";
        return switch (especialidad.toLowerCase()) {
            case "medicina general" -> "Piso 1, Consultorio 102";
            case "dermatología" -> "Piso 1, Consultorio 105";
            case "pediatría" -> "Piso 2, Consultorio 205";
            case "ginecología" -> "Piso 2, Consultorio 210";
            case "cardiología" -> "Piso 3, Consultorio 301";
            case "neurología" -> "Piso 3, Consultorio 304";
            case "traumatología" -> "Piso 3, Consultorio 308";
            default -> "Piso 1, Admisión";
        };
    }

    private int calcularNumeroTurno(Long medicoId, LocalDate fechaCita) {
        LocalDateTime startOfDay = fechaCita.atStartOfDay();
        LocalDateTime endOfDay = fechaCita.plusDays(1).atStartOfDay();
        long citasDelDia = citaRepository.countByMedicoAndDateRange(medicoId, startOfDay, endOfDay);
        return (int) citasDelDia + 1;
    }

}