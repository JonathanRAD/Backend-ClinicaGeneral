package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.PacienteDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.mapper.PacienteMapper; 
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

@Service
@RequiredArgsConstructor
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;

    private Usuario getUsuarioActual() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public Paciente obtenerPerfilPacienteActual() {
        Long usuarioId = getUsuarioActual().getId();
        return pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado."));
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
        List<Paciente> pacientes = pacienteRepository.listarTodosSP();
        return pacienteMapper.toDTOList(pacientes);
    }

    public Paciente crearPaciente(Paciente pacienteDetails) {
        HistoriaClinica nuevaHistoria = new HistoriaClinica();
        nuevaHistoria.setFechaCreacion(LocalDate.now());
        
        pacienteDetails.setHistoriaClinica(nuevaHistoria);
        nuevaHistoria.setPaciente(pacienteDetails);
        
        return pacienteRepository.save(pacienteDetails);
    }

    public Paciente actualizarPaciente(Long id, Paciente detallesPaciente) {
        Paciente pacienteExistente = pacienteRepository.buscarPorIdSP(id);
        if (pacienteExistente == null) {
             throw new ResourceNotFoundException("Paciente no encontrado con ID: " + id);
        }

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
        if (pacienteRepository.buscarPorIdSP(id) == null) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + id);
        }
        pacienteRepository.eliminarPacienteSP(id);
    }
}