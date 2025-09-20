package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class ResultadoLaboratorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaResultado;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String valores; // Podríamos usar un tipo más complejo en el futuro, pero TEXT es flexible

    @Column(columnDefinition = "TEXT")
    private String conclusiones;

    // --- RELACIÓN ---
    @OneToOne
    @JoinColumn(name = "orden_laboratorio_id", referencedColumnName = "id")
    @JsonBackReference("orden-resultado")
    private OrdenLaboratorio ordenLaboratorio;
}