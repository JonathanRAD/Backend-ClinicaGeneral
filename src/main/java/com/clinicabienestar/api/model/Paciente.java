// RUTA: src/main/java/com/clinicabienestar/api/model/Paciente.java

package com.clinicabienestar.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    // --- NUEVOS CAMPOS AÑADIDOS ---
    private Double peso; // En kilogramos (ej: 70.5)
    private Double altura; // En metros (ej: 1.75)
    private Integer ritmoCardiaco; // En pulsaciones por minuto (ej: 80)
}