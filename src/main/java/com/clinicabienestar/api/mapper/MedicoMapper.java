package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.MedicoDTO;
import com.clinicabienestar.api.model.Medico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicoMapper {

    MedicoMapper INSTANCE = Mappers.getMapper(MedicoMapper.class);

    MedicoDTO toDTO(Medico medico);

    List<MedicoDTO> toDTOList(List<Medico> medicos);

    @Mapping(target = "cmp", ignore = true)
    @Mapping(target = "fechaNacimiento", ignore = true)
    Medico toEntity(MedicoDTO medicoDTO);
}