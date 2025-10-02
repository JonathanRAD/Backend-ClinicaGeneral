package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.RegisterRequest;
import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.UsuarioRepository;
import com.clinicabienestar.api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthService authService; // Reutilizaremos la lógica de creación

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> crearUsuarioPorAdmin(@RequestBody RegisterRequest request) {
        authService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // PUT: Endpoint para que un admin actualice un usuario
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombres(usuarioDTO.getNombres());
                    usuario.setApellidos(usuarioDTO.getApellidos());
                    usuario.setRol(usuarioDTO.getRol());
                    // Nota: No permitimos cambiar email o contraseña desde aquí para simplificar
                    Usuario actualizado = usuarioRepository.save(usuario);
                    return ResponseEntity.ok(convertirADTO(actualizado));
                }).orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para que un usuario obtenga su propio perfil
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getMiPerfil() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        return usuarioRepository.findByEmail(userEmail)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para que un admin obtenga todos los usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioDTO> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }


    // DELETE: Endpoint para que un admin elimine un usuario
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        // Asegúrate de no permitir que un admin se elimine a sí mismo
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Usuario usuarioActual = usuarioRepository.findByEmail(userEmail).orElse(null);

        if (usuarioActual != null && usuarioActual.getId().equals(id)) {
            return ResponseEntity.badRequest().build(); // No se puede auto-eliminar
        }

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }
    // Mapea la entidad Usuario al DTO
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