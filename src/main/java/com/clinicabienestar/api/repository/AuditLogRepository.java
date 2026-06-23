package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.AccionAudit;
import com.clinicabienestar.api.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findAllByOrderByFechaDesc(Pageable pageable);

    long countByFechaBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a.accion, COUNT(a) FROM AuditLog a GROUP BY a.accion")
    List<Object[]> countByAccionGrouped();

    @Query("SELECT a.entidad, COUNT(a) FROM AuditLog a GROUP BY a.entidad")
    List<Object[]> countByEntidadGrouped();

    @Query("SELECT a.usuarioNombre, a.usuarioEmail, COUNT(a) FROM AuditLog a GROUP BY a.usuarioNombre, a.usuarioEmail ORDER BY COUNT(a) DESC")
    List<Object[]> findUsuariosMasActivos(Pageable pageable);

    List<AuditLog> findTop50ByOrderByFechaDesc();
}
