// RUTA: src/main/java/com/clinicabienestar/api/repository/PacienteRepository.java

package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    Optional<Paciente> findByUsuarioId(Long usuarioId);
}