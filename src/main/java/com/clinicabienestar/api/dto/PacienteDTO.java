// RUTA: src/main/java/com/clinicabienestar/api/dto/PacienteDTO.java
package com.clinicabienestar.api.dto;

import lombok.Data;
import java.math.BigDecimal; 
import java.time.LocalDate; 

@Data
public class PacienteDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private SeguroMedicoDTO seguroMedico; 
    private LocalDate fechaNacimiento;
    private BigDecimal peso;
    private BigDecimal altura;
    private Integer ritmoCardiaco;
}