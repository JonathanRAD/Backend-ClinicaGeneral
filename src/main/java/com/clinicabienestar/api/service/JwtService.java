// RUTA: src/main/java/com/clinicabienestar/api/service/JwtService.java

package com.clinicabienestar.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}") // Lee la clave secreta desde application.properties
    private String SECRET_KEY;

    // Genera un token JWT para un usuario
    public String generateToken(UserDetails userDetails) {
    // AÑADIMOS EL ROL COMO UN "CLAIM" DENTRO DEL TOKEN
    Map<String, Object> claims = new HashMap<>();
    // Asumimos que userDetails es nuestra clase Usuario, lo cual es cierto con la configuración de Spring Security
    if (userDetails instanceof com.clinicabienestar.api.model.Usuario) {
        claims.put("rol", ((com.clinicabienestar.api.model.Usuario) userDetails).getRol().name());
    }
    
    return generateToken(claims, userDetails);
}

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24)) // Token válido por 24 horas
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Valida si un token es correcto y no ha expirado
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Extrae el nombre de usuario (email) del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // --- Métodos privados para el funcionamiento interno ---

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        // Cambiamos el método de decodificación.
        // En lugar de Base64, obtenemos los bytes directamente del String.
        // Esto es más robusto y evita el error.
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}