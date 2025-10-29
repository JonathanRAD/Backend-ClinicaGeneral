package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.OrdenLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenLaboratorioRepository extends JpaRepository<OrdenLaboratorio, Long> {
}