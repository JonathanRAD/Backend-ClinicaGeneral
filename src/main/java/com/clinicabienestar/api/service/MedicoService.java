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
@Transactional // Todos los métodos públicos serán transaccionales
public class MedicoService {

    private final MedicoRepository medicoRepository;

    @Transactional(readOnly = true) // Optimizamos las operaciones de solo lectura
    public List<Medico> obtenerTodosLosMedicos() {
        return medicoRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Medico obtenerMedicoPorId(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con ID: " + id));
    }

    public Medico crearMedico(Medico medico) {
        // Aquí podrías añadir validaciones antes de guardar, por ejemplo, verificar si el CMP ya existe.
        return medicoRepository.save(medico);
    }

    public Medico actualizarMedico(Long id, Medico detallesMedico) {
        Medico medico = obtenerMedicoPorId(id); // Reutilizamos el método para encontrar o lanzar excepción

        medico.setNombres(detallesMedico.getNombres());
        medico.setApellidos(detallesMedico.getApellidos());
        medico.setEspecialidad(detallesMedico.getEspecialidad());
        medico.setFechaNacimiento(detallesMedico.getFechaNacimiento());
        
        return medicoRepository.save(medico);
    }

    public void eliminarMedico(Long id) {
        Medico medico = obtenerMedicoPorId(id); // Aseguramos que el médico exista antes de intentar borrar
        medicoRepository.delete(medico);
    }
}