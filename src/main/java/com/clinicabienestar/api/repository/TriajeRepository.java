package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Triaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TriajeRepository extends JpaRepository<Triaje, Long> {
}
