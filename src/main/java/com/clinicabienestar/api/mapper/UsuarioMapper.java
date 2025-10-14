package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    @Mapping(target = "fechaRegistro", ignore = true)
    UsuarioDTO toDTO(Usuario usuario);

    // --- CORRECCIÓN ---
    // Eliminamos la línea de "authorities"
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "intentosFallidos", ignore = true)
    @Mapping(target = "bloqueoExpiracion", ignore = true)
    Usuario toEntity(UsuarioDTO usuarioDTO);

    List<UsuarioDTO> toDTOList(List<Usuario> usuarios);

    List<Usuario> toEntityList(List<UsuarioDTO> usuarioDTOs);
}