// RUTA: src/main/java/com/clinicabienestar/api/service/AuthService.java

package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.AuthResponse;
import com.clinicabienestar.api.dto.LoginRequest;
import com.clinicabienestar.api.dto.RegisterRequest;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.clinicabienestar.api.model.Rol;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    private static final int MAX_INTENTOS_FALLIDOS = 3;
    private static final int TIEMPO_BLOQUEO_MINUTOS = 15;

    // ... (El método login se mantiene igual que en la respuesta anterior)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos."));

        if (!usuario.isAccountNonLocked()) {
            throw new LockedException("La cuenta está bloqueada temporalmente debido a múltiples intentos fallidos.");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            
            usuario.setIntentosFallidos(0);
            usuario.setBloqueoExpiracion(null);
            usuarioRepository.save(usuario);

            String token = jwtService.generateToken(usuario);
            return AuthResponse.builder().token(token).build();
        } catch (AuthenticationException e) {
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
                usuario.setBloqueoExpiracion(LocalDateTime.now().plusMinutes(TIEMPO_BLOQUEO_MINUTOS));
            }
            usuarioRepository.save(usuario);
            throw new BadCredentialsException("Usuario o contraseña incorrectos.");
        }
    }

    public void createUserByAdmin(RegisterRequest request) {
        if (!esContrasenaSegura(request.getPassword())) {
            throw new IllegalArgumentException("La contraseña no cumple con los requisitos de seguridad.");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setIntentosFallidos(0);
        // El rol se toma directamente del request
        usuario.setRol(request.getRol()); 
        
        usuarioRepository.save(usuario);
    }
    public AuthResponse register(RegisterRequest request) {
        if (!esContrasenaSegura(request.getPassword())) {
            throw new IllegalArgumentException("La contraseña no cumple con los requisitos de seguridad.");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setIntentosFallidos(0);
        usuario.setRol(Rol.PACIENTE); 
        
        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);
        return AuthResponse.builder().token(token).build();
    }
    
    private boolean esContrasenaSegura(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#.,;:<>(){}\\[\\]\\-+=/\\|~`^])[A-Za-z\\d@$!%*?&_#.,;:<>(){}\\[\\]\\-+=/\\|~`^]{8,}$");
        return pattern.matcher(password).matches();
    }
}