package com.clinicabienestar.api.model;

public enum Rol {
    ADMINISTRADOR, // Para la "Dirección Médica"
    RECEPCIONISTA,
    MEDICO,
    CAJERO,        // Para el "Área de Caja / Facturación"
    PACIENTE,      // Para usuarios registrados sin privilegios
    ENFERMERA      // Para la gestión de triaje y signos vitales
}