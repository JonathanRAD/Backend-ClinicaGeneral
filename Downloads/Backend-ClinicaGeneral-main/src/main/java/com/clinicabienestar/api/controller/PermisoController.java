package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.Permiso;
import com.clinicabienestar.api.repository.PermisoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@CrossOrigin(origins = "http://localhost:4200")
public class PermisoController {

    private final PermisoRepository permisoRepository;

    public PermisoController(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('GESTIONAR_USUARIOS')")
    public List<Permiso> getAllPermisos() {
        return permisoRepository.findAll();
    }
}