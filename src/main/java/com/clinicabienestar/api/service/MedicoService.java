package com.clinicabienestar.api.service;

import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.model.AccionAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Medico> obtenerTodosLosMedicos() {
        return medicoRepository.listarTodosSP();
    }
    
    @Transactional(readOnly = true)
    public Medico obtenerMedicoPorId(Long id) {
        Medico medico = medicoRepository.buscarPorIdSP(id);
        if (medico == null) {
            throw new ResourceNotFoundException("Médico no encontrado con ID: " + id);
        }
        return medico;
    }

    public Medico crearMedico(Medico medico) {
        Medico guardado = medicoRepository.save(medico);
        try {
            auditService.registrarEvento(AccionAudit.CREAR, "MEDICO", guardado.getId(), "Creación de perfil de médico: " + guardado.getNombres() + " " + guardado.getApellidos(), null, auditService.toJson(guardado));
        } catch (Exception e) {}
        return guardado;
    }

    public Medico actualizarMedico(Long id, Medico detallesMedico) {
        Medico medico = obtenerMedicoPorId(id); 
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(medico);
        } catch (Exception e) {}

        medico.setNombres(detallesMedico.getNombres());
        medico.setApellidos(detallesMedico.getApellidos());
        medico.setEspecialidad(detallesMedico.getEspecialidad());
        medico.setFechaNacimiento(detallesMedico.getFechaNacimiento());
        
        Medico guardado = medicoRepository.save(medico);
        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "MEDICO", guardado.getId(), "Actualización de perfil de médico", anteriorJson, auditService.toJson(guardado));
        } catch (Exception e) {}
        return guardado;
    }

    public void eliminarMedico(Long id) {
        Medico medico = medicoRepository.buscarPorIdSP(id);
        if (medico == null) {
            throw new ResourceNotFoundException("Médico no encontrado con ID: " + id);
        }
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(medico);
        } catch (Exception e) {}

        medicoRepository.eliminarMedicoSP(id);
        try {
            auditService.registrarEvento(AccionAudit.ELIMINAR, "MEDICO", id, "Eliminación de perfil de médico (ID: " + id + ", Nombre: " + medico.getNombres() + " " + medico.getApellidos() + ")", anteriorJson, null);
        } catch (Exception e) {}
    }
}