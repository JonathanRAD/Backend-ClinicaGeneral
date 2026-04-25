package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    Optional<Paciente> findByUsuarioId(Long usuarioId);

    // USANDO SP: Listar todos los pacientes
    @Query(value = "SELECT * FROM sp_listar_pacientes()", nativeQuery = true)
    List<Paciente> listarTodosSP();

    // USANDO SP: Buscar paciente por ID
    @Query(value = "SELECT * FROM sp_buscar_paciente_id(:id)", nativeQuery = true)
    Paciente buscarPorIdSP(@Param("id") Long id);

    // USANDO SP: Eliminar paciente
    @Modifying
    @Query(value = "CALL sp_eliminar_paciente(:id)", nativeQuery = true)
    void eliminarPacienteSP(@Param("id") Long id);
}