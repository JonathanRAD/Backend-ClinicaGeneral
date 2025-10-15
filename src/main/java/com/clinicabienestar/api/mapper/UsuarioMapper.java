// RUTA: src/main/java/com/clinicabienestar/api/mapper/UsuarioMapper.java
package com.clinicabienestar.api.mapper;

import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.model.Permiso;
import com.clinicabienestar.api.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List; // <-- Importar List
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // --- Mapeo de Entidad a DTO ---

    @Mapping(source = "permisos", target = "permisos", qualifiedByName = "permisosToString")
    UsuarioDTO toUsuarioDTO(Usuario usuario); // <-- El nombre correcto es toUsuarioDTO

    // Método para convertir listas de entidades a listas de DTOs
    List<UsuarioDTO> toUsuarioDTOList(List<Usuario> usuarios); // <-- AÑADIR ESTE MÉTODO

    // --- Mapeo de DTO a Entidad ---
    
    // Al convertir de DTO a Entidad, ignoramos los permisos para manejarlos manualmente
    @Mapping(target = "permisos", ignore = true) 
    Usuario toUsuario(UsuarioDTO usuarioDTO);

    // --- Métodos de ayuda para permisos ---

    @Named("permisosToString")
    default Set<String> permisosToString(Set<Permiso> permisos) {
        if (permisos == null) {
            return null;
        }
        return permisos.stream()
                       .map(Permiso::getNombre)
                       .collect(Collectors.toSet());
    }
}