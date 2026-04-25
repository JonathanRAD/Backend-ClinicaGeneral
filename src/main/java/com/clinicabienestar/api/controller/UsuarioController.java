package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.CambiarContrasenaRequest;
import com.clinicabienestar.api.dto.EliminarCuentaRequest;
import com.clinicabienestar.api.dto.RegisterRequest;
import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.service.AuthService;
import com.clinicabienestar.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    // ── Admin-only endpoints ────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Void> crearUsuarioPorAdmin(@RequestBody RegisterRequest request) {
        authService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioDTO));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('VER_USUARIOS')")
    public List<UsuarioDTO> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // ── /me endpoints (usuario autenticado) ────────────────────────────────

    /** GET /api/usuarios/me — devuelve el perfil del usuario autenticado. */
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getMiPerfil() {
        return ResponseEntity.ok(usuarioService.getMiPerfil());
    }

    /** PUT /api/usuarios/me — actualiza nombres y apellidos del usuario autenticado. */
    @PutMapping("/me")
    public ResponseEntity<UsuarioDTO> actualizarMiPerfil(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarMiPerfil(dto));
    }

    /** PUT /api/usuarios/me/password — cambia la contraseña del usuario autenticado. */
    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> cambiarMiContrasena(@Valid @RequestBody CambiarContrasenaRequest request) {
        usuarioService.cambiarMiContrasena(request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
    }

    /** POST /api/usuarios/me/request-delete-otp — genera y envía OTP para confirmar eliminación. */
    @PostMapping("/me/request-delete-otp")
    public ResponseEntity<Map<String, String>> solicitarOtpEliminarCuenta() {
        usuarioService.solicitarOtpEliminarCuenta();
        return ResponseEntity.ok(Map.of("message", "Se ha enviado un código de verificación a tu correo electrónico."));
    }

    /** DELETE /api/usuarios/me — elimina la cuenta del usuario autenticado (requiere OTP). */
    @DeleteMapping("/me")
    public ResponseEntity<Void> eliminarMiCuenta(@Valid @RequestBody EliminarCuentaRequest request) {
        usuarioService.eliminarMiCuenta(request.getOtp());
        return ResponseEntity.noContent().build();
    }
}