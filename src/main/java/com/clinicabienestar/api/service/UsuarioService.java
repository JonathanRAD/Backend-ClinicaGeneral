// RUTA: src/main/java/com/clinicabienestar/api/service/UsuarioService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.CambiarContrasenaRequest;
import com.clinicabienestar.api.dto.UsuarioDTO;
import com.clinicabienestar.api.exception.ForbiddenException;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.mapper.UsuarioMapper;
import com.clinicabienestar.api.model.Permiso;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.PermisoRepository;
import com.clinicabienestar.api.repository.UsuarioRepository;
import com.clinicabienestar.api.model.AccionAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors; 

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private static final int OTP_EXPIRACION_MINUTOS = 10;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;

    private Usuario getUsuarioActual() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario actual no encontrado en la base de datos."));
    }

    @Transactional(readOnly = true)
    public UsuarioDTO getMiPerfil() {
        Usuario usuarioActual = getUsuarioActual();
        return usuarioMapper.toUsuarioDTO(usuarioActual);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarioMapper.toUsuarioDTOList(usuarios);
    }

    public UsuarioDTO actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(usuario);
        } catch (Exception e) {}

        usuario.setNombres(usuarioDTO.getNombres());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setRol(usuarioDTO.getRol());
        
        if (usuarioDTO.getPermisos() != null) {
            Set<Permiso> permisos = usuarioDTO.getPermisos().stream()
                .map(permisoRepository::findByNombre)
                .filter(java.util.Objects::nonNull) 
                .collect(Collectors.toSet());
            usuario.setPermisos(permisos);
        }
        
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "USUARIO", updatedUsuario.getId(), "Administrador actualizó el usuario con ID: " + id, anteriorJson, auditService.toJson(updatedUsuario));
        } catch (Exception e) {}
        return usuarioMapper.toUsuarioDTO(updatedUsuario);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuarioActual = getUsuarioActual();

        if (usuarioActual.getId().equals(id)) {
            throw new ForbiddenException("Un administrador no puede eliminarse a sí mismo.");
        }

        Usuario usuarioAEliminar = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(usuarioAEliminar);
        } catch (Exception e) {}

        usuarioRepository.deleteById(id);
        try {
            auditService.registrarEvento(AccionAudit.ELIMINAR, "USUARIO", id, "Administrador eliminó el usuario con ID: " + id + " (Email: " + usuarioAEliminar.getEmail() + ")", anteriorJson, null);
        } catch (Exception e) {}
    }

    /**
     * Genera un OTP de 6 dígitos, lo guarda en el usuario y envía el email
     * para confirmar la eliminación de cuenta.
     * Endpoint: POST /api/usuarios/me/request-delete-otp
     */
    public void solicitarOtpEliminarCuenta() {
        Usuario usuarioActual = getUsuarioActual();

        String otpCode = String.format("%06d", new Random().nextInt(1_000_000));
        usuarioActual.setOtpCode(otpCode);
        usuarioActual.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRACION_MINUTOS));
        usuarioRepository.save(usuarioActual);

        String subject = "⚠️ Confirmación de eliminación de cuenta — Clínica Bienestar";
        String html = "<!DOCTYPE html>"
            + "<html lang='es'><head><meta charset='UTF-8'><title>Eliminar Cuenta</title></head>"
            + "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f6f9;'>"
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f6f9;padding:30px 0;'>"
            + "<tr><td align='center'>"
            + "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:10px;"
            + "overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1);max-width:600px;width:100%;'>"
            + "<tr><td style='background:linear-gradient(135deg,#c62828,#b71c1c);padding:35px 40px;text-align:center;'>"
            + "<h1 style='margin:0;color:#ffffff;font-size:26px;font-weight:700;'>🏥 Clínica Bienestar</h1>"
            + "<p style='margin:8px 0 0;color:#ffcdd2;font-size:14px;'>Sistema de Gestión Médica</p>"
            + "</td></tr>"
            + "<tr><td style='padding:35px 40px;text-align:center;'>"
            + "<h2 style='color:#c62828;font-size:20px;'>Solicitud de eliminación de cuenta</h2>"
            + "<p style='color:#555;font-size:15px;'>Hola <strong>" + usuarioActual.getNombres() + "</strong>, "
            + "hemos recibido una solicitud para eliminar tu cuenta. Si fuiste tú, usa este código:</p>"
            + "<div style='margin:30px auto;display:inline-block;background:#fff5f5;border:2px dashed #c62828;"
            + "border-radius:12px;padding:20px 40px;'>"
            + "<span style='font-size:42px;font-weight:900;letter-spacing:14px;color:#c62828;font-family:monospace;'>" + otpCode + "</span>"
            + "</div>"
            + "<p style='color:#888;font-size:13px;margin-top:20px;'>Este código expira en <strong>" + OTP_EXPIRACION_MINUTOS + " minutos</strong>.</p>"
            + "<p style='color:#aaa;font-size:12px;'>Si no solicitaste esto, ignora este correo. Tu cuenta <strong>no será eliminada</strong>.</p>"
            + "</td></tr>"
            + "<tr><td style='background:#f2f4f8;padding:20px 40px;text-align:center;border-top:1px solid #e0e6ef;'>"
            + "<p style='margin:4px 0;font-size:13px;color:#888;'>© " + java.time.Year.now().getValue() + " Clínica Bienestar.</p>"
            + "</td></tr>"
            + "</table></td></tr></table></body></html>";

        emailService.sendHtmlEmail(usuarioActual.getEmail(), subject, html);
        try {
            auditService.registrarEvento(AccionAudit.RESET_PASSWORD, "USUARIO", usuarioActual.getId(), "Usuario solicitó OTP para confirmar la eliminación de cuenta", null, null);
        } catch (Exception e) {}
    }

    /**
     * Valida el OTP recibido y, si es correcto, elimina la cuenta del usuario autenticado.
     * Endpoint: DELETE /api/usuarios/me  body: { "otp": "123456" }
     */
    public void eliminarMiCuenta(String otp) {
        Usuario usuarioActual = getUsuarioActual();

        if (usuarioActual.getOtpCode() == null || !usuarioActual.getOtpCode().equals(otp)) {
            throw new IllegalArgumentException("Código OTP incorrecto.");
        }
        if (usuarioActual.getOtpExpiry() == null || usuarioActual.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El código OTP ha expirado. Solicita uno nuevo.");
        }

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(usuarioActual);
        } catch (Exception e) {}

        usuarioRepository.deleteById(usuarioActual.getId());
        try {
            auditService.registrarEventoSinAuth(AccionAudit.ELIMINAR_CUENTA, "USUARIO", usuarioActual.getId(), usuarioActual.getEmail(), usuarioActual.getNombres() + " " + usuarioActual.getApellidos(), usuarioActual.getRol().name(), "Usuario eliminó su propia cuenta usando verificación OTP");
        } catch (Exception e) {}
    }

    /**
     * Actualiza el nombre y apellidos del usuario autenticado.
     * Endpoint: PUT /api/usuarios/me
     */
    public UsuarioDTO actualizarMiPerfil(UsuarioDTO dto) {
        Usuario usuarioActual = getUsuarioActual();
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(usuarioActual);
        } catch (Exception e) {}

        usuarioActual.setNombres(dto.getNombres());
        usuarioActual.setApellidos(dto.getApellidos());
        Usuario actualizado = usuarioRepository.save(usuarioActual);
        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "USUARIO", actualizado.getId(), "Usuario actualizó su perfil", anteriorJson, auditService.toJson(actualizado));
        } catch (Exception e) {}
        return usuarioMapper.toUsuarioDTO(actualizado);
    }

    /**
     * Cambia la contraseña del usuario autenticado, previa validación de la actual.
     * Endpoint: PUT /api/usuarios/me/password
     */
    public void cambiarMiContrasena(CambiarContrasenaRequest request) {
        Usuario usuarioActual = getUsuarioActual();

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuarioActual.getPassword())) {
            throw new ForbiddenException("La contraseña actual es incorrecta.");
        }

        usuarioActual.setPassword(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuarioActual);
        try {
            auditService.registrarEvento(AccionAudit.CAMBIAR_CONTRASENA, "USUARIO", usuarioActual.getId(), "Usuario cambió su contraseña", null, null);
        } catch (Exception e) {}
    }
}