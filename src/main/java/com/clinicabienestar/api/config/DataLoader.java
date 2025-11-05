// RUTA: src/main/java/com/clinicabienestar/api/config/DataLoader.java
package com.clinicabienestar.api.config;

import com.clinicabienestar.api.model.Permiso;
import com.clinicabienestar.api.model.Rol;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.PermisoRepository;
import com.clinicabienestar.api.repository.UsuarioRepository; // <-- 1. Importa el repositorio de usuarios
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository; // <-- 2. Inyéctalo en el constructor
    private final PasswordEncoder passwordEncoder;

    public DataLoader(PermisoRepository permisoRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.permisoRepository = permisoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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

        if (usuarioRepository.count() == 0) {
            
            System.out.println(">>> Base de datos de usuarios vacía. Creando Super Admin por defecto...");

            // Obtenemos todos los permisos que acabamos de crear
            List<Permiso> todosLosPermisos = permisoRepository.findAll();

            Usuario superAdmin = new Usuario();
            superAdmin.setNombres("Super");
            superAdmin.setApellidos("Admin");
            superAdmin.setEmail("admin@gmail.com"); // <-- Puedes cambiar este email

            superAdmin.setPassword(passwordEncoder.encode("Elmaspro_123")); // <-- ¡CAMBIA ESTA CONTRASEÑA!
            
            superAdmin.setRol(Rol.ADMINISTRADOR); //
            superAdmin.setPermisos(new HashSet<>(todosLosPermisos)); // Asigna todos los permisos
            superAdmin.setIntentosFallidos(0);

            usuarioRepository.save(superAdmin);
            
            System.out.println(">>> Usuario Super Admin 'admin@clinica.com' creado exitosamente. <<<");
        }
    
    }
}