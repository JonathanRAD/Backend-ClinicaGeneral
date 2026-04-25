// RUTA: src/main/java/com/clinicabienestar/api/service/EmailService.java
package com.clinicabienestar.api.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

@Service
public class EmailService {

    private static final String APPLICATION_NAME = "Clinica Bienestar";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // Inyectamos los valores desde application.properties
    @Value("${google.oauth.client-id}")
    private String CLIENT_ID;

    @Value("${google.oauth.client-secret}")
    private String CLIENT_SECRET;

    @Value("${google.oauth.refresh-token}")
    private String REFRESH_TOKEN;

    @Value("${google.oauth.email-from}")
    private String EMAIL_FROM;

    private Gmail gmailService; // Cache para el servicio

    /**
     * Crea y autentica el servicio de Gmail usando OAuth 2.0 y el Refresh Token.
     */
    private Gmail getGmailService() throws IOException, GeneralSecurityException {
        if (this.gmailService != null) {
            return this.gmailService;
        }

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Crea la credencial usando el Refresh Token
        Credential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .build();
        
        credential.setRefreshToken(REFRESH_TOKEN);
        credential.refreshToken(); // Forzamos a que se actualice el Access Token

        this.gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        return this.gmailService;
    }

    /**
     * Este es el método que tu AuthService ya está llamando.
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            Gmail service = getGmailService();
            
            MimeMessage mimeMessage = createHtmlEmail(to, EMAIL_FROM, subject, htmlContent);
            
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);

            Message message = new Message();
            message.setRaw(encodedEmail);

            service.users().messages().send("me", message).execute();
            
            System.out.println("DEBUG (Async con Google API OAuth): Correo HTML enviado a " + to);

        } catch (Exception e) {
            System.err.println("Error al enviar email con Google API OAuth: " + e.getMessage());
            e.printStackTrace();
            this.gmailService = null; // Resetea el servicio si falla
        }
    }

    /**
     * Helper para crear un MimeMessage de JavaMail con contenido HTML.
     */
    private MimeMessage createHtmlEmail(String to, String from, String subject, String htmlContent) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from, "Clínica Bienestar"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject, "UTF-8");
        email.setContent(htmlContent, "text/html; charset=utf-8");
        
        return email;
    }

    /**
     * Envía un correo HTML de confirmación de cita.
     * @param to           Destinatario del correo
     * @param nombrePaciente Nombre completo del paciente
     * @param nombreMedico   Nombre completo del médico (con "Dr(a).")
     * @param especialidad   Especialidad del médico
     * @param fechaHora      Fecha y hora de la cita
     * @param motivo         Motivo de la consulta
     * @param consultorio    Consultorio asignado
     * @param turno          Número de turno en el día
     * @param esAdmin        true → asunto/intro para el admin, false → para el paciente
     */
    @Async
    public void sendCitaConfirmationEmail(
            String to,
            String nombrePaciente,
            String nombreMedico,
            String especialidad,
            java.time.LocalDateTime fechaHora,
            String motivo,
            String consultorio,
            int turno,
            boolean esAdmin) {
        try {
            Gmail service = getGmailService();

            String subject = esAdmin
                    ? "⚕️ Nueva cita agendada - " + nombrePaciente
                    : "✅ Confirmación de tu cita - Clínica Bienestar";

            String htmlContent = crearPlantillaCita(
                    nombrePaciente, nombreMedico, especialidad,
                    fechaHora, motivo, consultorio, turno, esAdmin);

            MimeMessage mimeMessage = createHtmlEmail(to, EMAIL_FROM, subject, htmlContent);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            String encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray());

            Message message = new Message();
            message.setRaw(encodedEmail);
            service.users().messages().send("me", message).execute();

            System.out.println("DEBUG (Cita Email): Correo de cita enviado a " + to);

        } catch (Exception e) {
            System.err.println("Error al enviar email de cita: " + e.getMessage());
            e.printStackTrace();
            this.gmailService = null;
        }
    }

    /**
     * Genera la plantilla HTML del correo de confirmación de cita.
     */
    private String crearPlantillaCita(
            String nombrePaciente,
            String nombreMedico,
            String especialidad,
            java.time.LocalDateTime fechaHora,
            String motivo,
            String consultorio,
            int turno,
            boolean esAdmin) {

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
        String fechaFormateada = fechaHora != null ? fechaHora.format(formatter) : "Por confirmar";

        String intro = esAdmin
                ? "<p style='font-size:16px;color:#333;'>Se ha registrado una <strong>nueva cita</strong> en el sistema:</p>"
                : "<p style='font-size:16px;color:#333;'>Hola <strong>" + nombrePaciente + "</strong>, tu cita ha sido <strong>confirmada</strong> exitosamente. Aquí tienes los detalles:</p>";

        String notaCancelacion = esAdmin ? "" :
                "<p style='font-size:13px;color:#777;margin-top:20px;'>" +
                "Si necesitas cancelar o reprogramar, inicia sesión en la plataforma con anticipación.</p>";

        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>"
                + "<title>Confirmación de Cita</title></head>"
                + "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f6f9;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f6f9;padding:30px 0;'>"
                + "<tr><td align='center'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:10px;"
                + "overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1);max-width:600px;width:100%;'>"

                // Header
                + "<tr><td style='background:linear-gradient(135deg,#2c7be5,#1a4fa0);padding:35px 40px;text-align:center;'>"
                + "<h1 style='margin:0;color:#ffffff;font-size:26px;font-weight:700;letter-spacing:1px;'>🏥 Clínica Bienestar</h1>"
                + "<p style='margin:8px 0 0;color:#c8dcff;font-size:14px;'>Sistema de Gestión Médica</p>"
                + "</td></tr>"

                // Cuerpo intro
                + "<tr><td style='padding:35px 40px 10px;'>"
                + intro
                + "</td></tr>"

                // Tarjeta de detalles
                + "<tr><td style='padding:10px 40px 20px;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f0f5ff;"
                + "border-radius:8px;border-left:4px solid #2c7be5;padding:20px;'>"

                + detalleRow("👨‍⚕️", "Médico", nombreMedico)
                + detalleRow("🩺", "Especialidad", especialidad != null ? especialidad : "General")
                + detalleRow("👤", "Paciente", nombrePaciente)
                + detalleRow("📅", "Fecha y Hora", fechaFormateada)
                + detalleRow("🏢", "Consultorio", consultorio != null ? consultorio : "Por asignar")
                + detalleRow("🎫", "Número de Turno", String.valueOf(turno))
                + detalleRow("📝", "Motivo", motivo != null ? motivo : "Sin especificar")

                + "</table>"
                + "</td></tr>"

                // Nota cancelación (solo paciente)
                + "<tr><td style='padding:0 40px 10px;'>"
                + notaCancelacion
                + "</td></tr>"

                // Footer
                + "<tr><td style='background-color:#f2f4f8;padding:20px 40px;text-align:center;"
                + "border-top:1px solid #e0e6ef;'>"
                + "<p style='margin:4px 0;font-size:13px;color:#888;'>© " + java.time.Year.now().getValue()
                + " Clínica Bienestar. Todos los derechos reservados.</p>"
                + "<p style='margin:4px 0;font-size:12px;color:#aaa;'>Av. Principal 123, Lima, Perú</p>"
                + "</td></tr>"

                + "</table>"
                + "</td></tr></table>"
                + "</body></html>";
    }

    /** Genera una fila de detalle con ícono, etiqueta y valor para la tarjeta de cita. */
    private String detalleRow(String icono, String etiqueta, String valor) {
        return "<tr>"
                + "<td style='padding:8px 15px;vertical-align:top;width:30px;font-size:18px;'>" + icono + "</td>"
                + "<td style='padding:8px 5px;vertical-align:top;width:130px;font-size:14px;"
                + "color:#555;font-weight:bold;'>" + etiqueta + ":</td>"
                + "<td style='padding:8px 5px;vertical-align:top;font-size:14px;color:#333;'>" + valor + "</td>"
                + "</tr>";
    }
}