package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    Permiso findByNombre(String nombre);
}