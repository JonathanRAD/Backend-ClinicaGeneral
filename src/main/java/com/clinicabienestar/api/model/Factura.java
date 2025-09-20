// RUTA: src/main/java/com/clinicabienestar/api/model/Factura.java

package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; 
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List; 

@Entity
@Data
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double monto; 
    private Double montoPagado; 
    private LocalDate fechaEmision;
    private String estado; 

    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("factura-detalles")
    private List<DetalleFactura> detalles;
}