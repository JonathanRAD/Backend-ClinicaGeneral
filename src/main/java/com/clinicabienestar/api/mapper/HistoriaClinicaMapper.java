package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.HistoriaClinicaDTO;
import com.clinicabienestar.api.model.HistoriaClinica;
import com.clinicabienestar.api.model.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {PacienteMapper.class})
public interface HistoriaClinicaMapper {

    HistoriaClinicaMapper INSTANCE = Mappers.getMapper(HistoriaClinicaMapper.class);
    @Mapping(source = "historia.id", target = "id")
    @Mapping(source = "paciente", target = "paciente") 
    HistoriaClinicaDTO toDTO(HistoriaClinica historia, Paciente paciente);
}