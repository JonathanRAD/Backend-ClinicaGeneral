package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "DETALLES_RECETA")
public class DetalleReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECETA_ID")
    @JsonBackReference("receta-detalles")
    private Receta receta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MEDICAMENTO_ID")
    private Medicamento medicamento;

    private Integer cantidadSolicitada;
    
    private String dosis;
    
    private String frecuencia;
    
    private String duracion;
}
