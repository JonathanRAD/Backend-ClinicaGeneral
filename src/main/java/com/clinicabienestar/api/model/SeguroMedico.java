package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SeguroMedico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreAseguradora;
    private String numeroPoliza;
    private String cobertura;

    @OneToOne
    @JoinColumn(name = "paciente_id", referencedColumnName = "id")
    @JsonBackReference("paciente-seguro")
    private Paciente paciente;
}