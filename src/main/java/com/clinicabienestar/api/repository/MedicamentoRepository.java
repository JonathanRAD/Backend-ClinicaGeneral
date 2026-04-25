package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    boolean existsByCodigo(String codigo);
}
