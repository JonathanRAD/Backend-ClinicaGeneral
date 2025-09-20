// RUTA: src/main/java/com/clinicabienestar/api/model/Paciente.java

package com.clinicabienestar.api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dni;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String direccion;

    private Double peso;
    private Double altura;
    private Integer ritmoCardiaco;

    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("paciente-historia") // Para evitar recursividad infinita
    private HistoriaClinica historiaClinica;

    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("paciente-seguro") // Para evitar recursividad infinita
    private SeguroMedico seguroMedico;
}