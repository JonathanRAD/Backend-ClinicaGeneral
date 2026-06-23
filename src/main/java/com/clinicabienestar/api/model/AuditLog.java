package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_fecha", columnList = "fecha"),
    @Index(name = "idx_audit_usuario", columnList = "usuario_id"),
    @Index(name = "idx_audit_accion", columnList = "accion"),
    @Index(name = "idx_audit_entidad", columnList = "entidad")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_email")
    private String usuarioEmail;

    @Column(name = "usuario_nombre")
    private String usuarioNombre;

    @Column
    private String rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccionAudit accion;

    @Column(nullable = false)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "datos_anteriores", columnDefinition = "TEXT")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "TEXT")
    private String datosNuevos;

    @Column(name = "ip_address")
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
