// RUTA: src/main/java/com/clinicabienestar/api/controller/FacturaController.java

package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.FacturaDTO;
import com.clinicabienestar.api.model.Cita;
import com.clinicabienestar.api.model.Factura;
import com.clinicabienestar.api.repository.CitaRepository;
import com.clinicabienestar.api.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "http://localhost:4200")
public class FacturaController {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @GetMapping
    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Factura> crearFactura(@RequestBody FacturaDTO facturaDTO) {
        Optional<Cita> citaOpt = citaRepository.findById(facturaDTO.getCitaId());
        if (citaOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Factura factura = new Factura();
        factura.setCita(citaOpt.get());
        factura.setMonto(facturaDTO.getMonto());
        factura.setEstado(facturaDTO.getEstado());
        factura.setFechaEmision(LocalDate.now());

        Factura nuevaFactura = facturaRepository.save(factura);
        return ResponseEntity.ok(nuevaFactura);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Factura> actualizarFactura(@PathVariable Long id, @RequestBody FacturaDTO facturaDTO) {
        Optional<Factura> facturaOpt = facturaRepository.findById(id);
        Optional<Cita> citaOpt = citaRepository.findById(facturaDTO.getCitaId());

        if (facturaOpt.isEmpty() || citaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Factura factura = facturaOpt.get();
        factura.setCita(citaOpt.get());
        factura.setMonto(facturaDTO.getMonto());
        factura.setEstado(facturaDTO.getEstado());
        // La fecha de emisión no se suele actualizar, pero se podría añadir si fuera necesario

        Factura facturaActualizada = facturaRepository.save(factura);
        return ResponseEntity.ok(facturaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        return facturaRepository.findById(id)
            .map(factura -> {
                facturaRepository.delete(factura);
                return ResponseEntity.noContent().<Void>build();
            }).orElse(ResponseEntity.notFound().build());
    }
}