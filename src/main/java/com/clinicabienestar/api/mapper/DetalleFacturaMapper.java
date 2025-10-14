package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.DetalleFacturaDTO;
import com.clinicabienestar.api.model.DetalleFactura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DetalleFacturaMapper {

    DetalleFacturaMapper INSTANCE = Mappers.getMapper(DetalleFacturaMapper.class);

    // DTO a Entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "factura", ignore = true) 
    DetalleFactura toEntity(DetalleFacturaDTO dto);

    // Lista de DTOs a Lista de Entidades
    List<DetalleFactura> toEntityList(List<DetalleFacturaDTO> dtoList);
}