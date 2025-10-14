// RUTA: src/main/java/com/clinicabienestar/api/service/FacturaService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.FacturaDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Cita;
import com.clinicabienestar.api.model.DetalleFactura;
import com.clinicabienestar.api.model.Factura;
import com.clinicabienestar.api.repository.CitaRepository;
import com.clinicabienestar.api.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final CitaRepository citaRepository;

    @Transactional(readOnly = true)
    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.findAll();
    }

    public Factura crearFactura(FacturaDTO facturaDTO) {
        Cita cita = citaRepository.findById(facturaDTO.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + facturaDTO.getCitaId()));

        Factura factura = new Factura();
        factura.setCita(cita); // Relación con Cita
        factura.setEstado(facturaDTO.getEstado());
        factura.setFechaEmision(LocalDate.now());
        factura.setMontoPagado(facturaDTO.getMontoPagado());

        // Manejo de la relación bidireccional con DetalleFactura
        if (facturaDTO.getDetalles() != null && !facturaDTO.getDetalles().isEmpty()) {
            List<DetalleFactura> detalles = facturaDTO.getDetalles().stream().map(dto -> {
                DetalleFactura detalle = new DetalleFactura();
                detalle.setDescripcionServicio(dto.getDescripcionServicio());
                detalle.setCantidad(dto.getCantidad());
                detalle.setPrecioUnitario(dto.getPrecioUnitario());
                detalle.setFactura(factura); // Establecer la relación en el lado "hijo"
                return detalle;
            }).collect(Collectors.toList());
            
            factura.getDetalles().addAll(detalles); // Añadir los detalles al "padre"

            BigDecimal montoTotal = detalles.stream()
                .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            factura.setMonto(montoTotal);
        } else {
            factura.setMonto(facturaDTO.getMonto());
        }

        return facturaRepository.save(factura);
    }

    public Factura actualizarFactura(Long id, FacturaDTO facturaDTO) {
        Factura factura = facturaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + id));
        Cita cita = citaRepository.findById(facturaDTO.getCitaId())
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + facturaDTO.getCitaId()));

        factura.setCita(cita);
        factura.setEstado(facturaDTO.getEstado());
        factura.setMontoPagado(facturaDTO.getMontoPagado());
        
        // Limpiar detalles antiguos para evitar duplicados y manejar la relación
        factura.getDetalles().clear();

        if (facturaDTO.getDetalles() != null && !facturaDTO.getDetalles().isEmpty()) {
            List<DetalleFactura> nuevosDetalles = facturaDTO.getDetalles().stream().map(dto -> {
                DetalleFactura detalle = new DetalleFactura();
                detalle.setDescripcionServicio(dto.getDescripcionServicio());
                detalle.setCantidad(dto.getCantidad());
                detalle.setPrecioUnitario(dto.getPrecioUnitario());
                detalle.setFactura(factura); // Establecer la relación
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

        return facturaRepository.save(factura);
    }

    public void eliminarFactura(Long id) {
        if (!facturaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Factura no encontrada con ID: " + id);
        }
        facturaRepository.deleteById(id);
    }
}