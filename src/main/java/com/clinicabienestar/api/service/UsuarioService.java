// RUTA PROPUESTA: src/main/java/com/clinicabienestar/api/service/UsuarioService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.exception.ForbiddenException;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioActual() {
        // Obtenemos el email del contexto de seguridad
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario actual no encontrado en la base de datos."));
    }

    @Transactional(readOnly = true)
    public UsuarioDTO getMiPerfil() {
        Usuario usuarioActual = getUsuarioActual();
        return convertirADTO(usuarioActual);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setNombres(usuarioDTO.getNombres());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setRol(usuarioDTO.getRol());
        // Nota: No permitimos cambiar email o contraseña desde aquí para simplificar
        
        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirADTO(actualizado);
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

    // Este método de conversión vive ahora en el servicio
    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        return dto;
    }
}