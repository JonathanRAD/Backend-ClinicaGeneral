// RUTA: src/main/java/com/clinicabienestar/api/model/Factura.java

package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double monto;
    private LocalDate fechaEmision;
    private String estado; // "pagada", "pendiente", "anulada"

    // Relación: Muchas facturas pueden estar asociadas a UNA cita.
    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;
}