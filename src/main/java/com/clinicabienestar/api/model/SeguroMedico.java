// RUTA: src/main/java/com/clinicabienestar/api/model/SeguroMedico.java
package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // <-- IMPORTA ESTA CLASE
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "SEGUROS_MEDICOS")
public class SeguroMedico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE_ASEGURADORA") private String nombreAseguradora;
    @Column(name = "NUMERO_POLIZA") private String numeroPoliza;
    private String cobertura;

    // CORRECCIÓN: Esta es la parte "inversa" de la relación, no tiene @JoinColumn
    @OneToOne(mappedBy = "seguroMedico")
    @JsonBackReference("paciente-seguro") // <-- AÑADE ESTA ANOTACIÓN (con un nombre único)
    private Paciente paciente;
}