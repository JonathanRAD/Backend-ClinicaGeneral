// RUTA MODIFICADA: src/main/java/com/clinicabienestar/api/controller/UsuarioController.java
package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.RegisterRequest;
import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.service.AuthService;
import com.clinicabienestar.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService; // Mantenemos este para la creación

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> crearUsuarioPorAdmin(@RequestBody RegisterRequest request) {
        // La creación ya usa un servicio, lo cual es correcto.
        authService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getMiPerfil() {
        return ResponseEntity.ok(usuarioService.getMiPerfil());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioDTO> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}