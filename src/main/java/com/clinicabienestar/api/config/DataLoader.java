// RUTA: src/main/java/com/clinicabienestar/api/config/DataLoader.java
package com.clinicabienestar.api.config;

import com.clinicabienestar.api.model.Permiso;
import com.clinicabienestar.api.model.Rol;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.PermisoRepository;
import com.clinicabienestar.api.repository.UsuarioRepository; // <-- 1. Importa el repositorio de usuarios
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository; // <-- 2. Inyéctalo en el constructor

    public DataLoader(PermisoRepository permisoRepository, UsuarioRepository usuarioRepository) {
        this.permisoRepository = permisoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // --- Creación de permisos (sin cambios) ---
        String[] permisosNombres = {
            "GESTIONAR_PACIENTES", "VER_PACIENTES",
            "GESTIONAR_CITAS", "VER_CITAS",
            "GESTIONAR_MEDICOS", "VER_MEDICOS",
            "GESTIONAR_USUARIOS", "VER_USUARIOS",
            "GESTIONAR_FACTURACION", "VER_FACTURACION",
            "GENERAR_REPORTES",
            "VER_HISTORIAL_CLINICO", "EDITAR_HISTORIAL_CLINICO"
        };

        Arrays.stream(permisosNombres).forEach(nombre -> {
            if (permisoRepository.findByNombre(nombre) == null) {
                permisoRepository.save(new Permiso(nombre));
            }
        });

        // =========== INICIO DEL CAMBIO ===========
        // --- Asignación de todos los permisos al rol de Administrador ---
        List<Usuario> administradores = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Rol.ADMINISTRADOR)
                .toList();

        List<Permiso> todosLosPermisos = permisoRepository.findAll();

        if (!administradores.isEmpty() && !todosLosPermisos.isEmpty()) {
            administradores.forEach(admin -> {
                admin.setPermisos(new HashSet<>(todosLosPermisos));
                usuarioRepository.save(admin);
            });
        }
        // =========== FIN DEL CAMBIO ===========
    }
}