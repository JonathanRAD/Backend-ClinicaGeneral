// RUTA: src/main/java/com/clinicabienestar/api/repository/CitaRepository.java

package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
}