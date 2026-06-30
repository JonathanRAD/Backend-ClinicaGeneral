package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.AuthResponse;
import com.clinicabienestar.api.dto.LoginRequest;
import com.clinicabienestar.api.dto.RegisterRequest;
import com.clinicabienestar.api.dto.ResetPasswordDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.model.Permiso;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.PacienteRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
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
import com.clinicabienestar.api.model.AccionAudit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.LocalDate;

// ── DTOs adicionales para OTP ──────────────────────────────────────────────
import com.clinicabienestar.api.dto.AuthResponse;
import com.clinicabienestar.api.dto.VerifyOtpRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PermisoRepository permisoRepository;
    private final EmailService emailService;
    private final AuditService auditService;


    private static final int MAX_INTENTOS_FALLIDOS = 3;
    private static final int TIEMPO_BLOQUEO_MINUTOS = 15;
    private static final int EXPIRACION_TOKEN_MINUTOS = 60;
    private static final int OTP_EXPIRACION_MINUTOS = 10;

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos."));

        if (!usuario.isAccountNonLocked()) {
            if (usuario.getBloqueoExpiracion() != null && usuario.getBloqueoExpiracion().isBefore(LocalDateTime.now())) {
                usuario.setIntentosFallidos(0);
                usuario.setBloqueoExpiracion(null);
                usuarioRepository.save(usuario);
            } else {
                 try {
                     auditService.registrarEventoSinAuth(AccionAudit.LOGIN, "USUARIO", usuario.getId(), usuario.getEmail(), usuario.getNombres() + " " + usuario.getApellidos(), usuario.getRol().name(), "Intento de inicio de sesión en cuenta bloqueada");
                 } catch (Exception ex) {}
                 throw new LockedException("La cuenta está bloqueada temporalmente debido a múltiples intentos fallidos.");
            }
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            usuario.setIntentosFallidos(0);
            usuario.setBloqueoExpiracion(null);
            usuarioRepository.save(usuario);
            String token = jwtService.generateToken(usuario);
            try {
                auditService.registrarEventoSinAuth(AccionAudit.LOGIN, "USUARIO", usuario.getId(), usuario.getEmail(), usuario.getNombres() + " " + usuario.getApellidos(), usuario.getRol().name(), "Inicio de sesión exitoso");
            } catch (Exception ex) {}
            return AuthResponse.builder().token(token).build();
        } catch (AuthenticationException e) {
             usuario.setIntentosFallidos(usuario.getIntentosFallidos() == null ? 1 : usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
                usuario.setBloqueoExpiracion(LocalDateTime.now().plusMinutes(TIEMPO_BLOQUEO_MINUTOS));
            }
            usuarioRepository.save(usuario);
            try {
                auditService.registrarEventoSinAuth(AccionAudit.LOGIN, "USUARIO", usuario.getId(), usuario.getEmail(), usuario.getNombres() + " " + usuario.getApellidos(), usuario.getRol().name(), "Intento fallido de inicio de sesión: " + e.getMessage());
            } catch (Exception ex) {}
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
        } else if (usuarioGuardado.getRol() == Rol.MEDICO) {
            Medico nuevoMedico = new Medico();
            nuevoMedico.setNombres(usuarioGuardado.getNombres());
            nuevoMedico.setApellidos(usuarioGuardado.getApellidos());
            nuevoMedico.setEspecialidad("Medicina General");
            nuevoMedico.setCmp("PENDIENTE");
            nuevoMedico.setFechaNacimiento(LocalDate.of(1980, 1, 1));
            medicoRepository.save(nuevoMedico);
        }
        try {
            auditService.registrarEvento(AccionAudit.CREAR, "USUARIO", usuarioGuardado.getId(), "Administrador creó usuario con email: " + usuarioGuardado.getEmail(), null, auditService.toJson(usuarioGuardado));
        } catch (Exception ex) {}
    }

    public AuthResponse register(RegisterRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());
        if (usuarioOpt.isPresent()) {
            Usuario usuarioExistente = usuarioOpt.get();
            if (Boolean.TRUE.equals(usuarioExistente.getEmailVerificado())) {
                throw new IllegalArgumentException("El correo electrónico ya está registrado.");
            }
            if (!esContrasenaSegura(request.getPassword())) {
                throw new IllegalArgumentException("La contraseña no cumple con los requisitos de seguridad.");
            }

            usuarioExistente.setNombres(request.getNombres());
            usuarioExistente.setApellidos(request.getApellidos());
            usuarioExistente.setPassword(passwordEncoder.encode(request.getPassword()));
            usuarioExistente.setFechaRegistro(LocalDate.now());

            String otpCode = String.format("%06d", new Random().nextInt(1_000_000));
            usuarioExistente.setOtpCode(otpCode);
            usuarioExistente.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRACION_MINUTOS));

            Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);

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

            sendOtpEmail(usuarioGuardado.getNombres(), usuarioGuardado.getEmail(), otpCode);

            try {
                auditService.registrarEventoSinAuth(AccionAudit.REGISTRO, "USUARIO", usuarioGuardado.getId(), usuarioGuardado.getEmail(), usuarioGuardado.getNombres() + " " + usuarioGuardado.getApellidos(), usuarioGuardado.getRol().name(), "Reenvío de código OTP / Reintento de registro", null, auditService.toJson(usuarioGuardado));
            } catch (Exception ex) {}

            return AuthResponse.builder()
                    .requiresOtp(true)
                    .email(usuarioGuardado.getEmail())
                    .build();
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
        usuario.setFechaRegistro(LocalDate.now());

        String otpCode = String.format("%06d", new Random().nextInt(1_000_000));
        usuario.setOtpCode(otpCode);
        usuario.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRACION_MINUTOS));
        usuario.setEmailVerificado(false);

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

        sendOtpEmail(usuarioGuardado.getNombres(), usuarioGuardado.getEmail(), otpCode);

        try {
            auditService.registrarEventoSinAuth(AccionAudit.REGISTRO, "USUARIO", usuarioGuardado.getId(), usuarioGuardado.getEmail(), usuarioGuardado.getNombres() + " " + usuarioGuardado.getApellidos(), usuarioGuardado.getRol().name(), "Registro de nuevo usuario paciente (OTP enviado)", null, auditService.toJson(usuarioGuardado));
        } catch (Exception ex) {}

        return AuthResponse.builder()
                .requiresOtp(true)
                .email(usuarioGuardado.getEmail())
                .build();
    }

    /**
     * Valida el código OTP enviado por email tras el registro.
     * Si es correcto y no ha expirado, marca el email como verificado y devuelve el JWT.
     * Backend endpoint: POST /api/auth/verify-otp
     */
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (usuario.getOtpCode() == null || !usuario.getOtpCode().equals(request.getOtp())) {
            throw new IllegalArgumentException("Código OTP incorrecto.");
        }

        if (usuario.getOtpExpiry() == null || usuario.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El código OTP ha expirado. Solicita uno nuevo.");
        }

        // Limpiar OTP y marcar email como verificado
        usuario.setOtpCode(null);
        usuario.setOtpExpiry(null);
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        try {
            auditService.registrarEventoSinAuth(AccionAudit.VERIFICAR_OTP, "USUARIO", usuario.getId(), usuario.getEmail(), usuario.getNombres() + " " + usuario.getApellidos(), usuario.getRol().name(), "Código OTP verificado exitosamente");
        } catch (Exception ex) {}

        // Generar y devolver JWT
        String token = jwtService.generateToken(usuario);
        return AuthResponse.builder().token(token).build();
    }

    /** Envía el código OTP al email del nuevo usuario. */
    private void sendOtpEmail(String nombreUsuario, String email, String otpCode) {
        String subject = "🔐 Verifica tu cuenta — Clínica Bienestar";
        String htmlContent = "<!DOCTYPE html>"
            + "<html lang='es'><head><meta charset='UTF-8'><title>Verificación OTP</title></head>"
            + "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f6f9;'>"
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f6f9;padding:30px 0;'>"
            + "<tr><td align='center'>"
            + "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:10px;"
            + "overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1);max-width:600px;width:100%;'>"
            + "<tr><td style='background:linear-gradient(135deg,#2c7be5,#1a4fa0);padding:35px 40px;text-align:center;'>"
            + "<h1 style='margin:0;color:#ffffff;font-size:26px;font-weight:700;'>🏥 Clínica Bienestar</h1>"
            + "<p style='margin:8px 0 0;color:#c8dcff;font-size:14px;'>Sistema de Gestión Médica</p>"
            + "</td></tr>"
            + "<tr><td style='padding:35px 40px;text-align:center;'>"
            + "<h2 style='color:#1a202c;font-size:20px;'>Verifica tu cuenta</h2>"
            + "<p style='color:#555;font-size:15px;'>Hola <strong>" + nombreUsuario + "</strong>, usa el siguiente código para completar tu registro:</p>"
            + "<div style='margin:30px auto;display:inline-block;background:#f0f5ff;border:2px dashed #2c7be5;"
            + "border-radius:12px;padding:20px 40px;'>"
            + "<span style='font-size:42px;font-weight:900;letter-spacing:14px;color:#2c7be5;font-family:monospace;'>" + otpCode + "</span>"
            + "</div>"
            + "<p style='color:#888;font-size:13px;margin-top:20px;'>Este código expira en <strong>" + OTP_EXPIRACION_MINUTOS + " minutos</strong>.</p>"
            + "<p style='color:#aaa;font-size:12px;'>Si no solicitaste esto, ignora este correo.</p>"
            + "</td></tr>"
            + "<tr><td style='background:#f2f4f8;padding:20px 40px;text-align:center;border-top:1px solid #e0e6ef;'>"
            + "<p style='margin:4px 0;font-size:13px;color:#888;'>© " + java.time.Year.now().getValue() + " Clínica Bienestar.</p>"
            + "</td></tr>"
            + "</table></td></tr></table></body></html>";

        emailService.sendHtmlEmail(email, subject, htmlContent);
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

        try {
            auditService.registrarEventoSinAuth(AccionAudit.RESET_PASSWORD, "USUARIO", usuario.getId(), usuario.getEmail(), usuario.getNombres() + " " + usuario.getApellidos(), usuario.getRol().name(), "Solicitud de restablecimiento de contraseña iniciada");
        } catch (Exception ex) {}

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

        try {
            auditService.registrarEventoSinAuth(AccionAudit.RESET_PASSWORD, "USUARIO", usuario.getId(), usuario.getEmail(), usuario.getNombres() + " " + usuario.getApellidos(), usuario.getRol().name(), "Contraseña restablecida exitosamente usando token");
        } catch (Exception ex) {}
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