// RUTA: src/main/java/com/clinicabienestar/api/model/Usuario.java
package com.clinicabienestar.api.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "USUARIOS_PERMISOS",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();

    @Column(name = "INTENTOS_FALLIDOS", nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer intentosFallidos = 0; 

    @Column(name = "BLOQUEO_EXPIRACION")
    private LocalDateTime bloqueoExpiracion;

    @Column(name = "RESET_PASSWORD_TOKEN")
    private String resetPasswordToken;

    @Column(name = "RESET_PASSWORD_TOKEN_EXPIRY")
    private LocalDateTime resetPasswordTokenExpiry;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (rol != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));
        }

        if (permisos != null) {
            permisos.forEach(permiso -> {
                authorities.add(new SimpleGrantedAuthority(permiso.getNombre()));
            });
        }

        return authorities;
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
        if (this.intentosFallidos == null) {
            this.intentosFallidos = 0; 
        }
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