// RUTA: src/main/java/com/clinicabienestar/api/model/Consulta.java
package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "CONSULTAS")
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consultas_seq")
    @SequenceGenerator(name = "consultas_seq", sequenceName = "CONSULTAS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "FECHA_CONSULTA")
    private LocalDateTime fechaConsulta;

    @Lob @Column(columnDefinition = "CLOB") private String motivo;
    @Lob @Column(columnDefinition = "CLOB") private String diagnostico;
    @Lob @Column(columnDefinition = "CLOB") private String tratamiento;

    // ESTA ES LA CORRECCIÓN CLAVE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HISTORIA_CLINICA_ID")
    @JsonBackReference("historia-consultas")
    private HistoriaClinica historiaClinica;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEDICO_ID")
    private Medico medico;
}