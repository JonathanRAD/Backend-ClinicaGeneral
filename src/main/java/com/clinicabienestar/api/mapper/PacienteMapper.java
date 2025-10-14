package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.PacienteDTO;
import com.clinicabienestar.api.model.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // <-- Importar
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SeguroMedicoMapper.class})
public interface PacienteMapper {

    PacienteMapper INSTANCE = Mappers.getMapper(PacienteMapper.class);

    PacienteDTO toDTO(Paciente paciente);
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "historiaClinica", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Paciente toEntity(PacienteDTO pacienteDTO);

    List<PacienteDTO> toDTOList(List<Paciente> pacientes);
}