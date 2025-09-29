package com.clinicabienestar.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CITAS") // Mapeo a la tabla CITAS
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "citas_seq")
    @SequenceGenerator(name = "citas_seq", sequenceName = "CITAS_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "FECHA_HORA") // Mapeo de columna
    private LocalDateTime fechaHora;

    private String motivo;
    private String estado;
    private String consultorio;

    @Column(name = "NUMERO_TURNO") // Mapeo de columna
    private Integer numeroTurno;

    @ManyToOne
    @JoinColumn(name = "PACIENTE_ID") // Mapeo de clave foránea
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "MEDICO_ID") // Mapeo de clave foránea
    private Medico medico;
}
