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

@Entity // Le dice a JPA que esta clase es una tabla en la BD
@Data   // Lombok: Crea automáticamente getters, setters, toString(), etc.
@NoArgsConstructor // Lombok: Crea un constructor vacío
@AllArgsConstructor // Lombok: Crea un constructor con todos los campos
public class Paciente {

    @Id // Marca este campo como la clave primaria (ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Le dice a la BD que genere el ID automáticamente
    private Long id;

    private String dni;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
}