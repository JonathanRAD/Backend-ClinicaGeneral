// RUTA: src/main/java/com/clinicabienestar/api/model/DetalleFactura.java
package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "DETALLES_FACTURA")
public class DetalleFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DESCRIPCION_SERVICIO")
    private String descripcionServicio;
    
    private Integer cantidad;

    @Column(name = "PRECIO_UNITARIO")
    private BigDecimal precioUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FACTURA_ID")
    @JsonBackReference("factura-detalles")
    private Factura factura;
}