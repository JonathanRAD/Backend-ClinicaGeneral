// RUTA: src/main/java/com/clinicabienestar/api/model/DetalleFactura.java

package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DetalleFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcionServicio;
    private int cantidad;
    private Double precioUnitario;

    @ManyToOne
    @JoinColumn(name = "factura_id")
    @JsonBackReference("factura-detalles")
    private Factura factura;
}