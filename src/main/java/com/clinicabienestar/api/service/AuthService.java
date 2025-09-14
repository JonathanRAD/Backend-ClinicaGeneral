// RUTA: src/main/java/com/clinicabienestar/api/service/AuthService.java

package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.AuthResponse;
import com.clinicabienestar.api.dto.LoginRequest;
import com.clinicabienestar.api.dto.RegisterRequest;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        // Spring Security autentica al usuario
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        
        // Ahora, esta línea funciona porque Usuario implementa UserDetails
        Usuario user = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
        
        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).build();
    }

    public AuthResponse register(RegisterRequest request) {
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        
        usuarioRepository.save(usuario);

        // Generamos el token directamente desde nuestro objeto Usuario
        String token = jwtService.generateToken(usuario);
        return AuthResponse.builder().token(token).build();
    }
}