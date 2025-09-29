// RUTA: src/main/java/com/clinicabienestar/api/model/SeguroMedico.java
package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "SEGUROS_MEDICOS")
public class SeguroMedico {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seguros_medicos_seq")
    @SequenceGenerator(name = "seguros_medicos_seq", sequenceName = "SEGUROS_MEDICOS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "NOMBRE_ASEGURADORA") private String nombreAseguradora;
    @Column(name = "NUMERO_POLIZA") private String numeroPoliza;
    private String cobertura;

    // CORRECCIÓN: Esta es la parte "inversa" de la relación, no tiene @JoinColumn
    @OneToOne(mappedBy = "seguroMedico")
    private Paciente paciente;
}