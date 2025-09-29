// RUTA: src/main/java/com/clinicabienestar/api/model/OrdenLaboratorio.java
package com.clinicabienestar.api.model;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ordenes_laboratorio_seq")
    @SequenceGenerator(name = "ordenes_laboratorio_seq", sequenceName = "ORDENES_LABORATORIO_SEQ", allocationSize = 1)
    private Long id;
    
    @Column(name = "FECHA_ORDEN")
    private LocalDate fechaOrden;
    
    @Column(name = "TIPO_EXAMEN")
    private String tipoExamen;
    
    // ESTA ES LA CORRECCIÓN FINAL
    @Lob 
    @Column(name = "OBSERVACIONES", columnDefinition = "CLOB")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "CONSULTA_ID")
    private Consulta consulta;
}