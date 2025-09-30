// RUTA: src/main/java/com/clinicabienestar/api/model/Paciente.java
package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "PACIENTES")
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pacientes_seq")
    @SequenceGenerator(name = "pacientes_seq", sequenceName = "PACIENTES_SEQ", allocationSize = 1)
    private Long id;
    private String dni;
    private String nombres;
    private String apellidos;
    @Column(name = "FECHA_NACIMIENTO") private LocalDate fechaNacimiento;
    private String telefono;
    private String direccion;
    private BigDecimal peso;
    private BigDecimal altura;
    @Column(name = "RITMO_CARDIACO") private Integer ritmoCardiaco;

    // CORRECCIÓN: Esta es la parte "inversa" de la relación
    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("paciente-historia")
    private HistoriaClinica historiaClinica;

    // CORRECCIÓN: El Paciente es "dueño" de esta relación
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "SEGURO_MEDICO_ID", referencedColumnName = "id")
    @JsonManagedReference("paciente-seguro")
    private SeguroMedico seguroMedico;

    @OneToOne
    @JoinColumn(name = "USUARIO_ID", referencedColumnName = "id")
    private Usuario usuario;
}