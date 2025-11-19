package com.clinicabienestar.api.service;

import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicoService {

    private final MedicoRepository medicoRepository;

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
        return medicoRepository.save(medico);
    }

    public Medico actualizarMedico(Long id, Medico detallesMedico) {
        Medico medico = obtenerMedicoPorId(id); 

        medico.setNombres(detallesMedico.getNombres());
        medico.setApellidos(detallesMedico.getApellidos());
        medico.setEspecialidad(detallesMedico.getEspecialidad());
        medico.setFechaNacimiento(detallesMedico.getFechaNacimiento());
        
        return medicoRepository.save(medico);
    }

    public void eliminarMedico(Long id) {
        if (medicoRepository.buscarPorIdSP(id) == null) {
            throw new ResourceNotFoundException("Médico no encontrado con ID: " + id);
        }
        medicoRepository.eliminarMedicoSP(id);
    }
}