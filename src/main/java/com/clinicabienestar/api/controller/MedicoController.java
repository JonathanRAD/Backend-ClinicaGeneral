// RUTA: src/main/java/com/clinicabienestar/api/controller/MedicoController.java

package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@CrossOrigin(origins = "http://localhost:4200")
public class MedicoController {

    @Autowired
    private MedicoRepository medicoRepository;

    // GET: Obtener todos los médicos
    @GetMapping
    public List<Medico> obtenerTodosLosMedicos() {
        return medicoRepository.findAll();
    }

    // POST: Crear un nuevo médico
    @PostMapping
    public Medico crearMedico(@RequestBody Medico medico) {
        return medicoRepository.save(medico);
    }

    // PUT: Actualizar un médico existente
    @PutMapping("/{id}")
    public ResponseEntity<Medico> actualizarMedico(@PathVariable Long id, @RequestBody Medico detallesMedico) {
        return medicoRepository.findById(id)
                .map(medico -> {
                    medico.setNombres(detallesMedico.getNombres());
                    medico.setApellidos(detallesMedico.getApellidos());
                    medico.setEspecialidad(detallesMedico.getEspecialidad());
                    medico.setFechaNacimiento(detallesMedico.getFechaNacimiento());
                    Medico medicoActualizado = medicoRepository.save(medico);
                    return ResponseEntity.ok(medicoActualizado);
                }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Eliminar un médico
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMedico(@PathVariable Long id) {
        return medicoRepository.findById(id)
                .map(medico -> {
                    medicoRepository.delete(medico);
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }
}