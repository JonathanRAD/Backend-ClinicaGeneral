package com.clinicabienestar.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TRIAJES")
public class Triaje {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double peso;
    private Double altura;
    private Double temperatura;
    
    @Column(name = "PRESION_ARTERIAL")
    private String presionArterial;
    
    @Column(name = "RITMO_CARDIACO")
    private Integer ritmoCardiaco;
    
    @Column(name = "SATURACION_OXIGENO")
    private Integer saturacionOxigeno;
    
    @Column(name = "NIVEL_AZUCAR")
    private Double nivelAzucar;
    
    @Column(name = "MOTIVO_CONSULTA")
    private String motivoConsulta;
    
    @Column(name = "NOTAS_OPCIONALES", length = 1000)
    private String notasOpcionales;
    
    @Column(name = "FECHA_REGISTRO")
    private LocalDateTime fechaRegistro;

    @OneToOne
    @JoinColumn(name = "CITA_ID")
    @JsonIgnore // Para evitar recursividad infinita
    private Cita cita;
}
