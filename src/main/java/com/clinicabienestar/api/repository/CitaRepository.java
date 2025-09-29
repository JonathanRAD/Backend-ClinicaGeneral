package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.medico.id = :medicoId AND c.fechaHora >= :startOfDay AND c.fechaHora < :endOfDay")
    long countByMedicoAndDateRange(
        @Param("medicoId") Long medicoId, 
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
}