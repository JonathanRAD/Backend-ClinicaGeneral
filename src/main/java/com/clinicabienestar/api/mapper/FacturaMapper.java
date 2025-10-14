package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.FacturaDTO;
import com.clinicabienestar.api.model.Factura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {DetalleFacturaMapper.class})
public interface FacturaMapper {

    FacturaMapper INSTANCE = Mappers.getMapper(FacturaMapper.class);

    // DTO a Entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaEmision", ignore = true) // Se asigna en el servicio
    @Mapping(target = "cita", ignore = true)         // Se asigna en el servicio
    @Mapping(source = "detalles", target = "detalles") // Usa el DetalleFacturaMapper para la lista
    Factura toEntity(FacturaDTO facturaDTO);
}