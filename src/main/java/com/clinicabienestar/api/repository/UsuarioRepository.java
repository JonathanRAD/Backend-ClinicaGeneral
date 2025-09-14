// RUTA: src/main/java/com/clinicabienestar/api/repository/UsuarioRepository.java

package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Data JPA creará automáticamente la consulta para buscar un usuario por su email
    Optional<Usuario> findByEmail(String email);
}