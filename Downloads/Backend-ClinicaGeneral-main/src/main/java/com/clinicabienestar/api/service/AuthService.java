package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.AuthResponse;
import com.clinicabienestar.api.dto.LoginRequest;
import com.clinicabienestar.api.dto.RegisterRequest;
import com.clinicabienestar.api.dto.ResetPasswordDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.Permiso;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.PacienteRepository;
import com.clinicabienestar.api.repository.PermisoRepository;
import com.clinicabienestar.api.repository.UsuarioRepository;
import com.clinicabienestar.api.model.HistoriaClinica;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.clinicabienestar.api.model.Rol;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PermisoRepository permisoRepository;
    private final EmailService emailService;


    private static final int MAX_INTENTOS_FALLIDOS = 3;
    private static final int TIEMPO_BLOQUEO_MINUTOS = 15;
    private static final int EXPIRACION_TOKEN_MINUTOS = 60;

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos."));

        if (!usuario.isAccountNonLocked()) {
            if (usuario.getBloqueoExpiracion() != null && usuario.getBloqueoExpiracion().isBefore(LocalDateTime.now())) {
                usuario.setIntentosFallidos(0);
                usuario.setBloqueoExpiracion(null);
                usuarioRepository.save(usuario);
            } else {
                 throw new LockedException("La cuenta está bloqueada temporalmente debido a múltiples intentos fallidos.");
            }
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            usuario.setIntentosFallidos(0);
            usuario.setBloqueoExpiracion(null);
            usuarioRepository.save(usuario);
            String token = jwtService.generateToken(usuario);
            return AuthResponse.builder().token(token).build();
        } catch (AuthenticationException e) {
             usuario.setIntentosFallidos(usuario.getIntentosFallidos() == null ? 1 : usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
                usuario.setBloqueoExpiracion(LocalDateTime.now().plusMinutes(TIEMPO_BLOQUEO_MINUTOS));
            }
            usuarioRepository.save(usuario);
            throw new BadCredentialsException("Usuario o contraseña incorrectos.");
        }
    }

    public void createUserByAdmin(RegisterRequest request) {
         if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }
        if (request.getPassword() != null && !esContrasenaSegura(request.getPassword())) {
            throw new IllegalArgumentException("La contraseña no cumple con los requisitos de seguridad.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setEmail(request.getEmail());

        if (request.getPassword() != null) {
             usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
             String tempPassword = generarPasswordTemporal();
             usuario.setPassword(passwordEncoder.encode(tempPassword));
             String subject = "Bienvenido a Clínica SaludVida - Contraseña Temporal";
             String htmlContent = String.format(
                "<p>Hola %s,</p>" +
                "<p>Se ha creado una cuenta para ti en la Clínica SaludVida.</p>" +
                "<p>Tu contraseña temporal es: <strong>%s</strong></p>" +
                "<p>Por favor, cámbiala después de iniciar sesión.</p>" +
                "<p>Saludos.</p>",
                usuario.getNombres(),
                tempPassword
             );
             emailService.sendHtmlEmail(usuario.getEmail(), subject, htmlContent);
        }

        usuario.setIntentosFallidos(0);
        usuario.setRol(request.getRol() != null ? request.getRol() : Rol.PACIENTE);

        if (request.getPermisos() != null && !request.getPermisos().isEmpty()) {
            Set<Permiso> permisos = request.getPermisos().stream()
                .map(permisoRepository::findByNombre)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
            usuario.setPermisos(permisos);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        if (usuarioGuardado.getRol() == Rol.PACIENTE) {
            if (pacienteRepository.findByUsuarioId(usuarioGuardado.getId()).isEmpty()) {
                Paciente nuevoPaciente = new Paciente();
                nuevoPaciente.setNombres(usuarioGuardado.getNombres());
                nuevoPaciente.setApellidos(usuarioGuardado.getApellidos());
                nuevoPaciente.setUsuario(usuarioGuardado);

                HistoriaClinica nuevaHistoria = new HistoriaClinica();
                nuevaHistoria.setFechaCreacion(LocalDate.now());
                nuevaHistoria.setPaciente(nuevoPaciente);
                nuevoPaciente.setHistoriaClinica(nuevaHistoria);

                pacienteRepository.save(nuevoPaciente);
            }
        }
    }

    public AuthResponse register(RegisterRequest request) {
         if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }
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
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

         if (pacienteRepository.findByUsuarioId(usuarioGuardado.getId()).isEmpty()) {
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setNombres(usuarioGuardado.getNombres());
            nuevoPaciente.setApellidos(usuarioGuardado.getApellidos());
            nuevoPaciente.setUsuario(usuarioGuardado);

            HistoriaClinica nuevaHistoria = new HistoriaClinica();
            nuevaHistoria.setFechaCreacion(LocalDate.now());
            nuevaHistoria.setPaciente(nuevoPaciente);
            nuevoPaciente.setHistoriaClinica(nuevaHistoria);

            pacienteRepository.save(nuevoPaciente);
        }

        String token = jwtService.generateToken(usuarioGuardado);
        return AuthResponse.builder().token(token).build();
    }

    public void requestPasswordReset(String email) throws ResourceNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No existe usuario con el email: " + email));

        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(EXPIRACION_TOKEN_MINUTOS));
        usuarioRepository.save(usuario);

        String resetLink = "https://clinica-saludvida.vercel.app" + "/reset-password/" + token;
        String subject = "Restablecimiento de Contraseña - Clínica Bienestar";
        String htmlContent = crearPlantillaHtmlReseteo(
            usuario.getNombres(),
            resetLink,
            EXPIRACION_TOKEN_MINUTOS
        );
        emailService.sendHtmlEmail(usuario.getEmail(), subject, htmlContent);

        System.out.println("DEBUG: Enlace de reseteo HTML enviado a " + email);
    }

    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Usuario usuario = usuarioRepository.findByResetPasswordToken(resetPasswordDTO.getToken())
            .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado."));

        if (usuario.getResetPasswordTokenExpiry() == null || usuario.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token inválido o expirado.");
        }

        if (!esContrasenaSegura(resetPasswordDTO.getNewPassword())) {
            throw new IllegalArgumentException("La nueva contraseña no cumple con los requisitos de seguridad.");
        }

        usuario.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiry(null);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueoExpiracion(null);
        usuarioRepository.save(usuario);
    }

    private boolean esContrasenaSegura(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#.,;:<>(){}\\[\\]\\-+=/\\|~`^])[A-Za-z\\d@$!%*?&_#.,;:<>(){}\\[\\]\\-+=/\\|~`^]{8,}$");
        return pattern.matcher(password).matches();
    }

    private String generarPasswordTemporal() {
        String passChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        return java.util.stream.IntStream.range(0, 12)
                .map(i -> (int)(passChars.length() * Math.random()))
                .mapToObj(passChars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private String crearPlantillaHtmlReseteo(String nombreUsuario, String resetLink, int expiracionMinutos) {
        String estiloBody = "font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0;";
        String estiloContainer = "width: 90%; max-width: 600px; margin: 20px auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.1);";
        String estiloHeader = "background-color: #3498db; color: #ffffff; padding: 30px 20px; text-align: center;";
        String estiloHeaderH1 = "margin: 0; font-size: 26px; font-weight: bold;";
        String estiloContent = "padding: 30px 40px;";
        String estiloContentP = "margin-bottom: 20px; font-size: 16px;";
        String estiloBoton = "display: inline-block; padding: 12px 25px; background-color: #2ecc71; color: #ffffff; text-decoration: none; border-radius: 5px; font-size: 17px; font-weight: bold; border: none; cursor: pointer; transition: background-color 0.3s ease;";
        String estiloBotonHover = ":hover { background-color: #27ae60; }";
        String estiloFooter = "background-color: #f2f2f2; color: #777; padding: 20px; text-align: center; font-size: 13px;";
        String estiloFooterP = "margin: 5px 0;";

        return "<!DOCTYPE html>"
            + "<html lang='es'>"
            + "<head><meta charset='UTF-8'><title>Restablecer Contraseña</title>"
            + "<style>"
            + " a {" + estiloBoton + "} a" + estiloBotonHover + ""
            + "</style>"
            + "</head>"
            + "<body style='" + estiloBody + "'>"
            + "  <div style='" + estiloContainer + "'>"
            + "    <div style='" + estiloHeader + "'>"
            + "      <h1 style='" + estiloHeaderH1 + "'>Clínica Bienestar</h1>"
            + "    </div>"
            + "    <div style='" + estiloContent + "'>"
            + "      <p style='" + estiloContentP + "'>Hola " + nombreUsuario + ",</p>"
            + "      <p style='" + estiloContentP + "'>Has solicitado restablecer tu contraseña. Haz clic en el botón de abajo para continuar:</p>"
            + "      <p style='text-align: center; margin: 35px 0;'>"
            + "        <a href='" + resetLink + "' style='" + estiloBoton + "'>Restablecer Contraseña</a>"
            + "      </p>"
            + "      <p style='" + estiloContentP + "'>Si el botón no funciona, copia y pega el siguiente enlace en tu navegador:</p>"
            + "      <p style='word-break: break-all; font-size: 14px; color: #555; background-color: #f0f0f0; padding: 10px; border-radius: 4px;'>" + resetLink + "</p>"
            + "      <p style='" + estiloContentP + "'>Este enlace expirará en <strong>" + expiracionMinutos + " minutos</strong>.</p>"
            + "      <p style='" + estiloContentP + "'>Si no solicitaste esto, puedes ignorar este correo de forma segura.</p>"
            + "    </div>"
            + "    <div style='" + estiloFooter + "'>"
            + "      <p style='" + estiloFooterP + "'>© " + java.time.Year.now().getValue() + " Clínica SaludVida. Todos los derechos reservados.</p>"
            + "      <p style='" + estiloFooterP + "'>Av. Principal 123, Lima, Perú</p>"
            + "    </div>"
            + "  </div>"
            + "</body>"
            + "</html>";
    }
}