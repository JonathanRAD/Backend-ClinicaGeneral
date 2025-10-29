package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.*;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://clinica-saludvida.vercel.app"})
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Void> createUser(@Valid @RequestBody RegisterRequest request) {
        authService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        try {
            authService.requestPasswordReset(requestDTO.getEmail());
            return ResponseEntity.ok(Map.of("message", "Si el correo existe, se ha enviado un enlace para restablecer la contraseña."));
        } catch (ResourceNotFoundException e) {
            System.out.println("DEBUG Controller: Intento de reseteo para correo no encontrado: " + requestDTO.getEmail());
            return ResponseEntity.ok(Map.of("message", "Si el correo existe, se ha enviado un enlace para restablecer la contraseña."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordDTO resetDTO) {
        authService.resetPassword(resetDTO);
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida con éxito."));
    }
}