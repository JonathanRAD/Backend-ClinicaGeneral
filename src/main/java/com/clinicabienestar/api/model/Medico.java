package com.clinicabienestar.api.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MEDICOS")
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "medicos_seq")
    @SequenceGenerator(name = "medicos_seq", sequenceName = "MEDICOS_SEQ", allocationSize = 1)
    private Long id;
    
    private String nombres;
    private String apellidos;
    private String especialidad;
    private String cmp;
    
    @Column(name = "FECHA_NACIMIENTO")
    private LocalDate fechaNacimiento;
}
