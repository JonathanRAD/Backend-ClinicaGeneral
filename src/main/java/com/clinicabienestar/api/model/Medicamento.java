package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MEDICAMENTOS")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "FORMA_FARMACEUTICA")
    private String formaFarmaceutica; // Ej: Pastilla, Jarabe, Inyección

    private String concentracion; // Ej: 500mg, 1L

    @Column(name = "PRECIO_UNITARIO")
    private Double precioUnitario;

    private String estado; // Ej: ACTIVO, INACTIVO

    @OneToMany(mappedBy = "medicamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoteMedicamento> lotes = new ArrayList<>();

    // Función auxiliar para obtener el stock total al vuelo
    @Transient
    public Integer getStockTotal() {
        if (lotes == null || lotes.isEmpty()) return 0;
        return lotes.stream().mapToInt(LoteMedicamento::getStock).sum();
    }
}
