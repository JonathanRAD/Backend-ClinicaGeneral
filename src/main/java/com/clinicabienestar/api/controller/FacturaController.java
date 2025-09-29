package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.FacturaDTO;
import com.clinicabienestar.api.model.DetalleFactura;
import com.clinicabienestar.api.model.Factura;
import com.clinicabienestar.api.repository.CitaRepository;
import com.clinicabienestar.api.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
        return citaRepository.findById(facturaDTO.getCitaId())
            .map(cita -> {
                Factura factura = new Factura();
                factura.setCita(cita);
                factura.setEstado(facturaDTO.getEstado());
                factura.setFechaEmision(LocalDate.now());
                factura.setMontoPagado(facturaDTO.getMontoPagado());

                if (facturaDTO.getDetalles() != null && !facturaDTO.getDetalles().isEmpty()) {
                    List<DetalleFactura> detalles = facturaDTO.getDetalles().stream().map(dto -> {
                        DetalleFactura detalle = new DetalleFactura();
                        detalle.setDescripcionServicio(dto.getDescripcionServicio());
                        detalle.setCantidad(dto.getCantidad());
                        detalle.setPrecioUnitario(dto.getPrecioUnitario());
                        detalle.setFactura(factura);
                        return detalle;
                    }).collect(Collectors.toList());

                    factura.setDetalles(detalles); // ¡Ahora existe!
                    BigDecimal montoTotal = detalles.stream()
                        .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    factura.setMonto(montoTotal);
                } else {
                    factura.setMonto(facturaDTO.getMonto());
                }

                Factura nuevaFactura = facturaRepository.save(factura);
                return ResponseEntity.ok(nuevaFactura);
            }).orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Factura> actualizarFactura(@PathVariable Long id, @RequestBody FacturaDTO facturaDTO) {
        return facturaRepository.findById(id)
            .flatMap(factura -> citaRepository.findById(facturaDTO.getCitaId())
                .map(cita -> {
                    factura.setCita(cita);
                    factura.setEstado(facturaDTO.getEstado());
                    factura.setMontoPagado(facturaDTO.getMontoPagado());
                    
                    if (factura.getDetalles() != null) {
                        factura.getDetalles().clear();
                    } else {
                        factura.setDetalles(new java.util.ArrayList<>());
                    }

                    if (facturaDTO.getDetalles() != null && !facturaDTO.getDetalles().isEmpty()) {
                        List<DetalleFactura> nuevosDetalles = facturaDTO.getDetalles().stream().map(dto -> {
                            DetalleFactura detalle = new DetalleFactura();
                            detalle.setDescripcionServicio(dto.getDescripcionServicio());
                            detalle.setCantidad(dto.getCantidad());
                            detalle.setPrecioUnitario(dto.getPrecioUnitario());
                            detalle.setFactura(factura);
                            return detalle;
                        }).collect(Collectors.toList());
                        factura.getDetalles().addAll(nuevosDetalles);

                        BigDecimal montoTotal = nuevosDetalles.stream()
                            .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                        factura.setMonto(montoTotal);
                    } else {
                        factura.setMonto(facturaDTO.getMonto());
                    }

                    Factura facturaActualizada = facturaRepository.save(factura);
                    return ResponseEntity.ok(facturaActualizada);
                })
            ).orElse(ResponseEntity.notFound().build());
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