package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.SeguroMedicoDTO;
import com.clinicabienestar.api.model.SeguroMedico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // <-- Importar
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SeguroMedicoMapper {

    SeguroMedicoMapper INSTANCE = Mappers.getMapper(SeguroMedicoMapper.class);

    SeguroMedicoDTO toDTO(SeguroMedico seguroMedico);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paciente", ignore = true)
    SeguroMedico toEntity(SeguroMedicoDTO seguroMedicoDTO);
}