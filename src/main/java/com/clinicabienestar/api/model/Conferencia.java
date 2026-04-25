package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CONFERENCIAS")
public class Conferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    private LocalDateTime fechaProgramada;
    private Integer duracionMinutos;

    @Column(unique = true)
    private String nombreSala;

    // "PROGRAMADA", "FINALIZADA", "CANCELADA"
    private String estado;

    @PrePersist
    public void prePersist() {
        if (this.nombreSala == null || this.nombreSala.isEmpty()) {
            this.nombreSala = UUID.randomUUID().toString();
        }
        if (this.estado == null || this.estado.isEmpty()) {
            this.estado = "PROGRAMADA";
        }
    }
}
