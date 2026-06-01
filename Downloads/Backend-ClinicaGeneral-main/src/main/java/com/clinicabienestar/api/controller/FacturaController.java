package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.FacturaDTO;
import com.clinicabienestar.api.model.Factura;
import com.clinicabienestar.api.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FacturaController {

    private final FacturaService facturaService;

    @GetMapping
    @PreAuthorize("hasAuthority('VER_FACTURACION')")
    public List<Factura> obtenerTodasLasFacturas() {
        return facturaService.obtenerTodasLasFacturas();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTIONAR_FACTURACION')")
    public ResponseEntity<Factura> crearFactura(@Valid @RequestBody FacturaDTO facturaDTO) {
        Factura nuevaFactura = facturaService.crearFactura(facturaDTO);
        return new ResponseEntity<>(nuevaFactura, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GESTIONAR_FACTURACION')")
    public ResponseEntity<Factura> actualizarFactura(@PathVariable Long id, @Valid @RequestBody FacturaDTO facturaDTO) {
        Factura facturaActualizada = facturaService.actualizarFactura(id, facturaDTO);
        return ResponseEntity.ok(facturaActualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GESTIONAR_FACTURACION')")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        facturaService.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }
}