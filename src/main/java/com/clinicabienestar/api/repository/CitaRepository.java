// RUTA: src/main/java/com/clinicabienestar/api/repository/CitaRepository.java

package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant; // <-- AÑADE ESTA IMPORTACIÓN

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * @param medicoId 
     * @param startOfDay 
     * @param endOfDay 
     * @return 
     */
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.medico.id = :medicoId AND c.fechaHora >= :startOfDay AND c.fechaHora < :endOfDay")
    long countByMedicoAndDateRange(@Param("medicoId") Long medicoId, @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);
    // --- FIN DEL NUEVO CÓDIGO ---
}