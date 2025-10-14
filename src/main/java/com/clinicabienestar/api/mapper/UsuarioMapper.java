package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    UsuarioDTO toDTO(Usuario usuario);

    Usuario toEntity(UsuarioDTO usuarioDTO);

    List<UsuarioDTO> toDTOList(List<Usuario> usuarios);

    List<Usuario> toEntityList(List<UsuarioDTO> usuarioDTOs);
}