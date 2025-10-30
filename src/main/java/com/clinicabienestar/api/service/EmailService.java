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
}