package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class OrdenLaboratorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaOrden;
    private String tipoExamen; // Ej: "Hemograma completo", "Perfil lipídico"
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // --- RELACIÓN ---
    // Muchas órdenes pueden pertenecer a UNA consulta
    @ManyToOne
    @JoinColumn(name = "consulta_id")
    @JsonBackReference("consulta-ordenes")
    private Consulta consulta;
}