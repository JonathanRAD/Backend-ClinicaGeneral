package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.PacienteDTO;
import com.clinicabienestar.api.dto.SeguroMedicoDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.HistoriaClinica;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    private Usuario getUsuarioActual() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public Paciente obtenerPerfilPacienteActual() {
        Long usuarioId = getUsuarioActual().getId();
        return pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado para el usuario actual."));
    }

    public Paciente actualizarPerfilPacienteActual(Paciente detallesPaciente) {
        Paciente pacienteExistente = obtenerPerfilPacienteActual();

        pacienteExistente.setDni(detallesPaciente.getDni());
        pacienteExistente.setTelefono(detallesPaciente.getTelefono());
        pacienteExistente.setDireccion(detallesPaciente.getDireccion());
        pacienteExistente.setFechaNacimiento(detallesPaciente.getFechaNacimiento());
        pacienteExistente.setPeso(detallesPaciente.getPeso());
        pacienteExistente.setAltura(detallesPaciente.getAltura());
        
        return pacienteRepository.save(pacienteExistente);
    }

    @Transactional(readOnly = true)
    public List<PacienteDTO> obtenerTodosLosPacientes() {
        return pacienteRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Paciente crearPaciente(Paciente pacienteDetails) {
        HistoriaClinica nuevaHistoria = new HistoriaClinica();
        nuevaHistoria.setFechaCreacion(LocalDate.now());
        
        pacienteDetails.setHistoriaClinica(nuevaHistoria);
        nuevaHistoria.setPaciente(pacienteDetails);
        
        return pacienteRepository.save(pacienteDetails);
    }

    public Paciente actualizarPaciente(Long id, Paciente detallesPaciente) {
        Paciente pacienteExistente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));

        pacienteExistente.setDni(detallesPaciente.getDni());
        pacienteExistente.setNombres(detallesPaciente.getNombres());
        pacienteExistente.setApellidos(detallesPaciente.getApellidos());
        pacienteExistente.setFechaNacimiento(detallesPaciente.getFechaNacimiento());
        pacienteExistente.setTelefono(detallesPaciente.getTelefono());
        pacienteExistente.setDireccion(detallesPaciente.getDireccion());
        pacienteExistente.setPeso(detallesPaciente.getPeso());
        pacienteExistente.setAltura(detallesPaciente.getAltura());
        pacienteExistente.setRitmoCardiaco(detallesPaciente.getRitmoCardiaco());
        
        return pacienteRepository.save(pacienteExistente);
    }

    public void eliminarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + id);
        }
        pacienteRepository.deleteById(id);
    }

    private PacienteDTO convertirADTO(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setId(paciente.getId());
        dto.setNombres(paciente.getNombres());
        dto.setApellidos(paciente.getApellidos());
        dto.setDni(paciente.getDni());
        dto.setTelefono(paciente.getTelefono());
        dto.setFechaNacimiento(paciente.getFechaNacimiento());
        dto.setPeso(paciente.getPeso());
        dto.setAltura(paciente.getAltura());
        dto.setRitmoCardiaco(paciente.getRitmoCardiaco());
        if (paciente.getSeguroMedico() != null) {
            SeguroMedicoDTO seguroDTO = new SeguroMedicoDTO();
            seguroDTO.setNombreAseguradora(paciente.getSeguroMedico().getNombreAseguradora());
            seguroDTO.setNumeroPoliza(paciente.getSeguroMedico().getNumeroPoliza());
            dto.setSeguroMedico(seguroDTO);
        }
        return dto;
    }
}