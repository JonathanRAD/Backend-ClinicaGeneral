// RUTA: src/main/java/com/clinicabienestar/api/model/Factura.java
package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data // <-- Esta anotación crea los getters y setters como getDetalles()
@Table(name = "FACTURAS")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal monto;

    @Column(name = "MONTO_PAGADO")
    private BigDecimal montoPagado;

    @Column(name = "FECHA_EMISION")
    private LocalDate fechaEmision;
    
    private String estado;

    @OneToOne
    @JoinColumn(name = "CITA_ID")
    private Cita cita;

    // ESTA LÍNEA CREA LOS MÉTODOS getDetalles() y setDetalles()
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("factura-detalles")
    private List<DetalleFactura> detalles = new ArrayList<>();
}