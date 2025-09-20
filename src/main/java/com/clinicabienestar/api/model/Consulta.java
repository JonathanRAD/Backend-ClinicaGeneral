// RUTA: src/main/java/com/clinicabienestar/api/model/Consulta.java

package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference; 
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; 

@Entity
@Data
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaConsulta;
    
    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    // --- RELACIONES ---

    // Muchas consultas pertenecen a UNA historia clínica
    @ManyToOne
    @JoinColumn(name = "historia_clinica_id")
    @JsonBackReference
    private HistoriaClinica historiaClinica;

    // Muchas consultas son realizadas por UN médico
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("consulta-ordenes")
    private List<OrdenLaboratorio> ordenesLaboratorio;
}