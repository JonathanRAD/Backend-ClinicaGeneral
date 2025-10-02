// RUTA: src/main/java/com/clinicabienestar/api/model/HistoriaClinica.java
package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // <-- IMPORTA ESTA CLASE
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "HISTORIAS_CLINICAS")
public class HistoriaClinica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FECHA_CREACION")
    private LocalDate fechaCreacion;
    
    // QUITA @Lob de estos tres campos
    @Column(columnDefinition = "TEXT") private String antecedentes;
    @Column(columnDefinition = "TEXT") private String alergias;
    @Column(name = "ENFERMEDADES_CRONICAS", columnDefinition = "TEXT") private String enfermedadesCronicas;

    // CORRECCIÓN: La Historia Clinica es "dueña" de esta relación
    @OneToOne
    @JoinColumn(name = "PACIENTE_ID", referencedColumnName = "id")
    @JsonBackReference("paciente-historia") // <-- AÑADE ESTA ANOTACIÓN
    private Paciente paciente;
    
    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("historia-consultas")
    private List<Consulta> consultas;
}