// RUTA: src/main/java/com/clinicabienestar/api/model/OrdenLaboratorio.java
package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // <-- IMPORTANTE
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ORDENES_LABORATORIO")
public class OrdenLaboratorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "FECHA_ORDEN")
    private LocalDate fechaOrden;
    
    @Column(name = "TIPO_EXAMEN")
    private String tipoExamen;
    
    @Column(name = "OBSERVACIONES", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "CONSULTA_ID")
    @JsonBackReference("consulta-ordenes") // <-- AÑADE ESTA ANOTACIÓN
    private Consulta consulta;
}