package com.clinicabienestar.api.dto;

import com.clinicabienestar.api.model.Consulta;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class HistoriaClinicaDTO {
    // Datos de la Historia
    private Long id;
    private LocalDate fechaCreacion;
    private String antecedentes;
    private String alergias;
    private String enfermedadesCronicas;
    
    // Datos del Paciente anidados
    private PacienteDTO paciente; 
    
    // Lista de Consultas (la entidad completa, es seguro en este contexto)
    private List<Consulta> consultas; 
}