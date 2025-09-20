// RUTA: src/main/java/com/clinicabienestar/api/model/HistoriaClinica.java

package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
public class HistoriaClinica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaCreacion;
    
    @Column(columnDefinition = "TEXT")
    private String antecedentes;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(columnDefinition = "TEXT")
    private String enfermedadesCronicas;

    @OneToOne
    @JoinColumn(name = "paciente_id", referencedColumnName = "id")
    @JsonBackReference("paciente-historia")
    private Paciente paciente;

    // Una historia clínica puede tener MUCHAS consultas
    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consulta> consultas;
}