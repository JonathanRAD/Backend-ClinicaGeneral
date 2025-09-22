// RUTA: src/main/java/com/clinicabienestar/api/model/Usuario.java

package com.clinicabienestar.api.model;

import jakarta.persistence.*; // AÑADIR IMPORT
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // --- CAMBIO DE ROL ---
    @Enumerated(EnumType.STRING) // Guarda el nombre del rol (ej: "MEDICO") en la BD
    private Rol rol;
    // --- FIN DEL CAMBIO ---

    private int intentosFallidos;
    private LocalDateTime bloqueoExpiracion;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Ahora el rol se asigna dinámicamente
        return List.of(new SimpleGrantedAuthority(rol.name()));
    }

    // ... (El resto de la clase se mantiene igual)
    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (bloqueoExpiracion == null) {
            return true;
        }
        return bloqueoExpiracion.isBefore(LocalDateTime.now());
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