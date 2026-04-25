package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Conferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConferenciaRepository extends JpaRepository<Conferencia, Long> {
    List<Conferencia> findByPacienteId(Long pacienteId);
    List<Conferencia> findByMedicoId(Long medicoId);
}
