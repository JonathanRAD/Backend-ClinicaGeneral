package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.PacienteDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.mapper.PacienteMapper; 
import com.clinicabienestar.api.model.HistoriaClinica;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.PacienteRepository;
import com.clinicabienestar.api.model.AccionAudit;
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
    private final AuditService auditService;

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
        
        Paciente guardado = pacienteRepository.save(pacienteDetails);
        try {
            auditService.registrarEvento(AccionAudit.CREAR, "PACIENTE", guardado.getId(), "Creación de perfil de paciente: " + guardado.getNombres() + " " + guardado.getApellidos(), null, auditService.toJson(guardado));
        } catch (Exception e) {}
        return guardado;
    }

    public Paciente actualizarPaciente(Long id, Paciente detallesPaciente) {
        Paciente pacienteExistente = pacienteRepository.buscarPorIdSP(id);
        if (pacienteExistente == null) {
             throw new ResourceNotFoundException("Paciente no encontrado con ID: " + id);
        }

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(pacienteExistente);
        } catch (Exception e) {}

        pacienteExistente.setDni(detallesPaciente.getDni());
        pacienteExistente.setNombres(detallesPaciente.getNombres());
        pacienteExistente.setApellidos(detallesPaciente.getApellidos());
        pacienteExistente.setFechaNacimiento(detallesPaciente.getFechaNacimiento());
        pacienteExistente.setTelefono(detallesPaciente.getTelefono());
        pacienteExistente.setDireccion(detallesPaciente.getDireccion());
        pacienteExistente.setPeso(detallesPaciente.getPeso());
        pacienteExistente.setAltura(detallesPaciente.getAltura());
        pacienteExistente.setRitmoCardiaco(detallesPaciente.getRitmoCardiaco());
        
        Paciente guardado = pacienteRepository.save(pacienteExistente);
        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "PACIENTE", guardado.getId(), "Actualización de perfil de paciente", anteriorJson, auditService.toJson(guardado));
        } catch (Exception e) {}
        return guardado;
    }

    public void eliminarPaciente(Long id) {
        Paciente paciente = pacienteRepository.buscarPorIdSP(id);
        if (paciente == null) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + id);
        }
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(paciente);
        } catch (Exception e) {}

        pacienteRepository.eliminarPacienteSP(id);
        try {
            auditService.registrarEvento(AccionAudit.ELIMINAR, "PACIENTE", id, "Eliminación de perfil de paciente (ID: " + id + ", Nombre: " + paciente.getNombres() + " " + paciente.getApellidos() + ")", anteriorJson, null);
        } catch (Exception e) {}
    }
}