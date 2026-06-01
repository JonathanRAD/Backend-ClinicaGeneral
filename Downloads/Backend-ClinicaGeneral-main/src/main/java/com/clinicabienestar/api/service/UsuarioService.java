// RUTA: src/main/java/com/clinicabienestar/api/service/UsuarioService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.exception.ForbiddenException;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.mapper.UsuarioMapper;
import com.clinicabienestar.api.model.Permiso; 
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.PermisoRepository;
import com.clinicabienestar.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set; 
import java.util.stream.Collectors; 

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PermisoRepository permisoRepository; 

    private Usuario getUsuarioActual() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario actual no encontrado en la base de datos."));
    }

    @Transactional(readOnly = true)
    public UsuarioDTO getMiPerfil() {
        Usuario usuarioActual = getUsuarioActual();
        return usuarioMapper.toUsuarioDTO(usuarioActual);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarioMapper.toUsuarioDTOList(usuarios);
    }

    public UsuarioDTO actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setNombres(usuarioDTO.getNombres());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setRol(usuarioDTO.getRol());
        
        if (usuarioDTO.getPermisos() != null) {
            Set<Permiso> permisos = usuarioDTO.getPermisos().stream()
                .map(permisoRepository::findByNombre)
                .filter(java.util.Objects::nonNull) 
                .collect(Collectors.toSet());
            usuario.setPermisos(permisos);
        }
        
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return usuarioMapper.toUsuarioDTO(updatedUsuario);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuarioActual = getUsuarioActual();

        if (usuarioActual.getId().equals(id)) {
            throw new ForbiddenException("Un administrador no puede eliminarse a sí mismo.");
        }

        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }

        usuarioRepository.deleteById(id);
    }
}