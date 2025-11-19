package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Llama a la función SQL sp_listar_medicos()
    @Query(value = "SELECT * FROM sp_listar_medicos()", nativeQuery = true)
    List<Medico> listarTodosSP();

    // Llama a la función SQL sp_buscar_medico_id(:id)
    @Query(value = "SELECT * FROM sp_buscar_medico_id(:id)", nativeQuery = true)
    Medico buscarPorIdSP(@Param("id") Long id);

    // Llama al procedimiento almacenado sp_eliminar_medico(:id)
    @Modifying
    @Query(value = "CALL sp_eliminar_medico(:id)", nativeQuery = true)
    void eliminarMedicoSP(@Param("id") Long id);
}