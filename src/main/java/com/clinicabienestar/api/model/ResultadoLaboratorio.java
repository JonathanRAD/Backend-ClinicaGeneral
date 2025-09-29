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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resultados_laboratorio_seq")
    @SequenceGenerator(name = "resultados_laboratorio_seq", sequenceName = "RESULTADOS_LABORATORIO_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "FECHA_RESULTADO")
    private LocalDate fechaResultado;

    // --- ESTAS SON LAS CORRECCIONES ---
    @Lob
    @Column(name = "DESCRIPCION", columnDefinition = "CLOB")
    private String descripcion;

    @Lob
    @Column(name = "VALORES", columnDefinition = "CLOB")
    private String valores;

    @Lob
    @Column(name = "CONCLUSIONES", columnDefinition = "CLOB")
    private String conclusiones;
    // --- FIN DE LAS CORRECCIONES ---

    @OneToOne
    @JoinColumn(name = "ORDEN_LABORATORIO_ID")
    private OrdenLaboratorio ordenLaboratorio;
}