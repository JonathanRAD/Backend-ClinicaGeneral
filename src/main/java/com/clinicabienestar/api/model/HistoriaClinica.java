// RUTA: src/main/java/com/clinicabienestar/api/model/HistoriaClinica.java
package com.clinicabienestar.api.model;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "historias_clinicas_seq")
    @SequenceGenerator(name = "historias_clinicas_seq", sequenceName = "HISTORIAS_CLINICAS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "FECHA_CREACION") private LocalDate fechaCreacion;
    @Lob @Column(columnDefinition = "CLOB") private String antecedentes;
    @Lob @Column(columnDefinition = "CLOB") private String alergias;
    @Lob @Column(name = "ENFERMEDADES_CRONICAS", columnDefinition = "CLOB") private String enfermedadesCronicas;

    // CORRECCIÓN: La Historia Clinica es "dueña" de esta relación
    @OneToOne
    @JoinColumn(name = "PACIENTE_ID", referencedColumnName = "id")
    private Paciente paciente;
    
    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("historia-consultas")
    private List<Consulta> consultas;
}