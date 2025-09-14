// RUTA: src/main/java/com/clinicabienestar/api/repository/PacienteRepository.java

package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    // ¡Listo! Spring Data JPA se encarga de implementar todos los métodos
    // como findAll(), findById(), save(), deleteById(), etc. por nosotros.
}