// RUTA: src/main/java/com/clinicabienestar/api/controller/AuthController.java
package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.AuthResponse;
import com.clinicabienestar.api.dto.LoginRequest;
import com.clinicabienestar.api.dto.RegisterRequest;
import com.clinicabienestar.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// Ya no es necesario @CrossOrigin aquí, porque lo manejamos globalmente en SecurityConfig
public class AuthController {
    
    private final AuthService authService;

    // Se especifica que este endpoint solo responde a POST
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Se especifica que este endpoint solo responde a POST
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}