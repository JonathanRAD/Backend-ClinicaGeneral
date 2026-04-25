package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "RECETAS")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FECHA_EMISION")
    private LocalDateTime fechaEmision;

    @Column(columnDefinition = "TEXT")
    private String indicacionesGenerales;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONSULTA_ID")
    @JsonBackReference("consulta-recetas")
    private Consulta consulta;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("receta-detalles")
    private List<DetalleReceta> detalles;
}
