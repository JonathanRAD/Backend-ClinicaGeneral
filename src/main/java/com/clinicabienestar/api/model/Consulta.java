// RUTA: src/main/java/com/clinicabienestar/api/model/Consulta.java
package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference; // <-- IMPORTANTE
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // <-- IMPORTANTE

@Entity
@Data
@Table(name = "CONSULTAS")
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FECHA_CONSULTA")
    private LocalDateTime fechaConsulta;
    
    @Column(columnDefinition = "TEXT") private String motivo;
    @Column(columnDefinition = "TEXT") private String diagnostico;
    @Column(columnDefinition = "TEXT") private String tratamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HISTORIA_CLINICA_ID")
    @JsonBackReference("historia-consultas")
    private HistoriaClinica historiaClinica;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICO_ID")
    private Medico medico;

    // --- AÑADE ESTA SECCIÓN ---
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("consulta-ordenes")
    private List<OrdenLaboratorio> ordenesLaboratorio;
}