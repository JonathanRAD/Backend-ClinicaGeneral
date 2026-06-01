package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Consultas JPA existentes (necesarias para lógica de negocio compleja)
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.medico.id = :medicoId AND c.fechaHora >= :startOfDay AND c.fechaHora < :endOfDay")
    long countByMedicoAndDateRange(@Param("medicoId") Long medicoId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT c FROM Cita c JOIN c.paciente p WHERE p.usuario.id = :usuarioId")
    List<Cita> findByPacienteUsuarioId(@Param("usuarioId") Long usuarioId);

    // --- IMPLEMENTACIÓN CON PROCEDIMIENTOS ALMACENADOS ---

    @Query(value = "SELECT * FROM sp_listar_citas()", nativeQuery = true)
    List<Cita> listarTodasSP();

    @Query(value = "SELECT * FROM sp_buscar_cita_id(:id)", nativeQuery = true)
    Cita buscarPorIdSP(@Param("id") Long id);
    
    // Nuevo método útil para búsquedas por fecha
    @Query(value = "SELECT * FROM sp_buscar_citas_fecha(:fecha)", nativeQuery = true)
    List<Cita> buscarPorFechaSP(@Param("fecha") LocalDate fecha);

    @Modifying
    @Query(value = "CALL sp_eliminar_cita(:id)", nativeQuery = true)
    void eliminarCitaSP(@Param("id") Long id);
}