package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.ConsultaResponseDTO;
import com.clinicabienestar.api.model.Consulta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MedicoMapper.class})
public interface ConsultaMapper {

    ConsultaMapper INSTANCE = Mappers.getMapper(ConsultaMapper.class);

    @Mapping(source = "medico", target = "medico")
    ConsultaResponseDTO toDTO(Consulta consulta);

    List<ConsultaResponseDTO> toDTOList(List<Consulta> consultas);
}