// RUTA: src/main/java/com/clinicabienestar/api/repository/FacturaRepository.java

package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
}