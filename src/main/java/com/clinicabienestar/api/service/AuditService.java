package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.AuditEstadisticasDTO;
import com.clinicabienestar.api.dto.AuditLogDTO;
import com.clinicabienestar.api.dto.AuditPageResponse;
import com.clinicabienestar.api.model.AccionAudit;
import com.clinicabienestar.api.model.AuditLog;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private HttpServletRequest request;

    // Main method for authenticated operations
    public void registrarEvento(AccionAudit accion, String entidad, Long entidadId,
                                 String descripcion, String datosAnteriores, String datosNuevos) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Usuario usuario = null;
            if (principal instanceof Usuario) {
                usuario = (Usuario) principal;
            }
            
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .accion(accion)
                    .entidad(entidad)
                    .entidadId(entidadId)
                    .descripcion(descripcion)
                    .datosAnteriores(datosAnteriores)
                    .datosNuevos(datosNuevos)
                    .ipAddress(obtenerIp());

            if (usuario != null) {
                builder.usuarioId(usuario.getId())
                       .usuarioEmail(usuario.getEmail())
                       .usuarioNombre(usuario.getNombres() + " " + usuario.getApellidos())
                       .rol(usuario.getRol().name());
            } else {
                builder.usuarioNombre("Sistema / Anónimo")
                       .usuarioEmail("sistema@clinica.com")
                       .rol("SISTEMA");
            }

            auditLogRepository.save(builder.build());
        } catch (Exception e) {
            log.error("Error al registrar evento de auditoría: {}", e.getMessage());
        }
    }

    // For login/register where user is not in SecurityContext yet or during auth failures
    public void registrarEventoSinAuth(AccionAudit accion, String entidad, Long usuarioId,
                                        String email, String nombre, String rol, String descripcion) {
        registrarEventoSinAuth(accion, entidad, usuarioId, email, nombre, rol, descripcion, null, null);
    }

    public void registrarEventoSinAuth(AccionAudit accion, String entidad, Long usuarioId,
                                        String email, String nombre, String rol, String descripcion,
                                        String datosAnteriores, String datosNuevos) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .usuarioId(usuarioId)
                    .usuarioEmail(email)
                    .usuarioNombre(nombre)
                    .rol(rol)
                    .accion(accion)
                    .entidad(entidad)
                    .descripcion(descripcion)
                    .datosAnteriores(datosAnteriores)
                    .datosNuevos(datosNuevos)
                    .ipAddress(obtenerIp())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Error al registrar evento de auditoría sin auth: {}", e.getMessage());
        }
    }

    private String obtenerIp() {
        if (request == null) return "N/A";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    // Query logs with dynamic filtering using JPA Specifications
    public AuditPageResponse obtenerLogs(Long usuarioId, String rol, String accion, String entidad,
                                         LocalDateTime fechaInicio, LocalDateTime fechaFin, String busqueda, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuarioId"), usuarioId));
            }
            if (rol != null && !rol.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("rol"), rol));
            }
            if (accion != null && !accion.trim().isEmpty()) {
                try {
                    AccionAudit accionEnum = AccionAudit.valueOf(accion.trim());
                    predicates.add(cb.equal(root.get("accion"), accionEnum));
                } catch (IllegalArgumentException e) {
                    log.warn("Acción inválida recibida en filtro: {}", accion);
                }
            }
            if (entidad != null && !entidad.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("entidad"), entidad));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaFin));
            }
            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String likePattern = "%" + busqueda.trim().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("usuarioNombre")), likePattern),
                        cb.like(cb.lower(root.get("usuarioEmail")), likePattern),
                        cb.like(cb.lower(root.get("descripcion")), likePattern)
                );
                predicates.add(searchPredicate);
            }

            // Order by date descending by default if not specified
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Create pageable with default sorting if sorting is not provided
        Pageable sortedPageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("fecha").descending());
        }

        Page<AuditLog> page = auditLogRepository.findAll(spec, sortedPageable);
        
        return AuditPageResponse.builder()
                .content(toDTOList(page.getContent()))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    public AuditEstadisticasDTO obtenerEstadisticas() {
        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = LocalDate.now().atTime(LocalTime.MAX);
        
        LocalDateTime semanaInicio = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime semanaFin = LocalDate.now().atTime(LocalTime.MAX);

        long totalEventos = auditLogRepository.count();
        long eventosHoy = auditLogRepository.countByFechaBetween(hoyInicio, hoyFin);
        long eventosSemana = auditLogRepository.countByFechaBetween(semanaInicio, semanaFin);

        // Eventos por Acción
        Map<String, Long> eventosPorAccion = new HashMap<>();
        List<Object[]> porAccionList = auditLogRepository.countByAccionGrouped();
        for (Object[] row : porAccionList) {
            if (row[0] != null) {
                eventosPorAccion.put(row[0].toString(), (Long) row[1]);
            }
        }

        // Eventos por Entidad
        Map<String, Long> eventosPorEntidad = new HashMap<>();
        List<Object[]> porEntidadList = auditLogRepository.countByEntidadGrouped();
        for (Object[] row : porEntidadList) {
            if (row[0] != null) {
                eventosPorEntidad.put(row[0].toString(), (Long) row[1]);
            }
        }

        // Usuarios más activos (Top 5)
        List<AuditEstadisticasDTO.UsuarioActivoDTO> usuariosMasActivos = new ArrayList<>();
        List<Object[]> masActivosList = auditLogRepository.findUsuariosMasActivos(PageRequest.of(0, 5));
        for (Object[] row : masActivosList) {
            if (row[0] != null && row[1] != null) {
                usuariosMasActivos.add(AuditEstadisticasDTO.UsuarioActivoDTO.builder()
                        .nombre(row[0].toString())
                        .email(row[1].toString())
                        .total((Long) row[2])
                        .build());
            }
        }

        return AuditEstadisticasDTO.builder()
                .totalEventos(totalEventos)
                .eventosHoy(eventosHoy)
                .eventosSemana(eventosSemana)
                .eventosPorAccion(eventosPorAccion)
                .eventosPorEntidad(eventosPorEntidad)
                .usuariosMasActivos(usuariosMasActivos)
                .build();
    }

    public List<AuditLogDTO> obtenerActividadReciente() {
        List<AuditLog> logs = auditLogRepository.findTop50ByOrderByFechaDesc();
        return toDTOList(logs);
    }

    private AuditLogDTO toDTO(AuditLog log) {
        if (log == null) return null;
        return AuditLogDTO.builder()
                .id(log.getId())
                .fecha(log.getFecha())
                .usuarioId(log.getUsuarioId())
                .usuarioEmail(log.getUsuarioEmail())
                .usuarioNombre(log.getUsuarioNombre())
                .rol(log.getRol())
                .accion(log.getAccion().name())
                .entidad(log.getEntidad())
                .entidadId(log.getEntidadId())
                .descripcion(log.getDescripcion())
                .datosAnteriores(log.getDatosAnteriores())
                .datosNuevos(log.getDatosNuevos())
                .ipAddress(log.getIpAddress())
                .build();
    }

    private List<AuditLogDTO> toDTOList(List<AuditLog> logs) {
        if (logs == null) return new ArrayList<>();
        return logs.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
