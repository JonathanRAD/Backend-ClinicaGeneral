package com.clinicabienestar.api.dto;

import lombok.Data;

@Data
public class MedicoDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String especialidad;
}