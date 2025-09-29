package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.SeguroMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeguroMedicoRepository extends JpaRepository<SeguroMedico, Long> {
    // Consulta para encontrar el seguro por el ID del paciente
    Optional<SeguroMedico> findByPacienteId(Long pacienteId);
}