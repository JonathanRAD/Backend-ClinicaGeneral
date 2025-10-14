package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.FacturaDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.mapper.FacturaMapper; // <-- 1. IMPORTAR
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
// import java.util.stream.Collectors; // Ya no es necesario para el mapeo

@Service
@RequiredArgsConstructor
@Transactional
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final CitaRepository citaRepository;
    private final FacturaMapper facturaMapper; // <-- 2. INYECTAR

    @Transactional(readOnly = true)
    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.findAll();
    }

    public Factura crearFactura(FacturaDTO facturaDTO) {
        Cita cita = citaRepository.findById(facturaDTO.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + facturaDTO.getCitaId()));

        // <-- 3. USAR EL MAPPER PARA LA CONVERSIÓN BASE
        Factura factura = facturaMapper.toEntity(facturaDTO);
        
        // Asignar los campos ignorados por el mapper
        factura.setCita(cita);
        factura.setFechaEmision(LocalDate.now());

        // Manejo de la relación bidireccional
        if (factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
            factura.getDetalles().forEach(detalle -> detalle.setFactura(factura)); // Establecer la referencia inversa

            BigDecimal montoTotal = factura.getDetalles().stream()
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
        
        // Limpiar detalles antiguos y mapear los nuevos
        factura.getDetalles().clear();
        List<DetalleFactura> nuevosDetalles = facturaMapper.toEntity(facturaDTO).getDetalles();
        
        if (nuevosDetalles != null && !nuevosDetalles.isEmpty()) {
            nuevosDetalles.forEach(detalle -> detalle.setFactura(factura));
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