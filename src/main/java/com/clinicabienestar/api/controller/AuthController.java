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
@CrossOrigin(origins = "http://localhost:4200") // Permite la conexión desde Angular
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}