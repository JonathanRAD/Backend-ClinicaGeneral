package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.ResultadoLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultadoLaboratorioRepository extends JpaRepository<ResultadoLaboratorio, Long> {
}