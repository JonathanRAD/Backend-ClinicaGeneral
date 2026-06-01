// RUTA: src/main/java/com/clinicabienestar/api/model/ResultadoLaboratorio.java
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
@Table(name = "RESULTADOS_LABORATORIO")
public class ResultadoLaboratorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FECHA_RESULTADO")
    private LocalDate fechaResultado;

    @Column(name = "DESCRIPCION", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "VALORES", columnDefinition = "TEXT")
    private String valores;

    @Column(name = "CONCLUSIONES", columnDefinition = "TEXT")
    private String conclusiones;

    @OneToOne
    @JoinColumn(name = "ORDEN_LABORATORIO_ID")
    private OrdenLaboratorio ordenLaboratorio;
}