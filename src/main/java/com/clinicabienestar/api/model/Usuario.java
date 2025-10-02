// RUTA: src/main/java/com/clinicabienestar/api/model/Usuario.java
package com.clinicabienestar.api.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USUARIOS")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombres;
    private String apellidos;
    private String email;
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Rol rol;
    
    @Column(name = "INTENTOS_FALLIDOS")
    private Integer intentosFallidos;
    
    @Column(name = "BLOQUEO_EXPIRACION")
    private LocalDateTime bloqueoExpiracion;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // =========== INICIO DEL CAMBIO ===========
        // Agregamos el prefijo "ROLE_" que es el estándar de Spring Security.
        // Esto asegura que hasRole() y hasAnyRole() funcionen correctamente.
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
        // =========== FIN DEL CAMBIO ===========
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Esta lógica estaba incompleta. La corregimos para que el bloqueo funcione.
        return this.bloqueoExpiracion == null || this.bloqueoExpiracion.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}