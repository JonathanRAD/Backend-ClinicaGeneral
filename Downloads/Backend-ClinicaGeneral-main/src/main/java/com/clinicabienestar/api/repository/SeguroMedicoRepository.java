package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.SeguroMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeguroMedicoRepository extends JpaRepository<SeguroMedico, Long> {
    
    Optional<SeguroMedico> findByPacienteId(Long pacienteId);

    // SPs
    @Query(value = "SELECT * FROM sp_listar_seguros()", nativeQuery = true)
    List<SeguroMedico> listarTodosSP();

    @Query(value = "SELECT * FROM sp_buscar_seguro_id(:id)", nativeQuery = true)
    SeguroMedico buscarPorIdSP(@Param("id") Long id);

    @Modifying
    @Query(value = "CALL sp_eliminar_seguro(:id)", nativeQuery = true)
    void eliminarSeguroSP(@Param("id") Long id);
}