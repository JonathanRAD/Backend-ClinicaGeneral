// RUTA: src/main/java/com/clinicabienestar/api/repository/MedicoRepository.java

package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
}