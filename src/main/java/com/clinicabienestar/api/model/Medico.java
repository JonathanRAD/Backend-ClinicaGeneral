package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <-- IMPORTA ESTA CLASE
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
// AÑADE ESTA ANOTACIÓN PARA IGNORAR LOS CAMPOS DEL PROXY DE HIBERNATE
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
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