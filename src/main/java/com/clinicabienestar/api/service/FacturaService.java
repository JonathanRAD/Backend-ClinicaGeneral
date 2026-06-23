package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.FacturaDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.mapper.FacturaMapper;
import com.clinicabienestar.api.model.Cita;
import com.clinicabienestar.api.model.DetalleFactura;
import com.clinicabienestar.api.model.Factura;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.repository.CitaRepository;
import com.clinicabienestar.api.repository.FacturaRepository;
import com.clinicabienestar.api.service.EmailService;
import com.clinicabienestar.api.model.AccionAudit;
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
    private final FacturaMapper facturaMapper;
    private final EmailService emailService;
    private final AuditService auditService;

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

        Factura guardada = facturaRepository.save(factura);
        try {
            auditService.registrarEvento(AccionAudit.CREAR, "FACTURA", guardada.getId(), "Creación de factura para la cita ID: " + guardada.getCita().getId() + " por un monto de: " + guardada.getMonto(), null, auditService.toJson(guardada));
        } catch (Exception e) {}
        return guardada;
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

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(factura);
        } catch (Exception e) {}

        Factura guardada = facturaRepository.save(factura);
        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "FACTURA", guardada.getId(), "Actualización de factura", anteriorJson, auditService.toJson(guardada));
        } catch (Exception e) {}
        return guardada;
    }

    public void eliminarFactura(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + id));

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(factura);
        } catch (Exception e) {}

        facturaRepository.deleteById(id);
        try {
            auditService.registrarEvento(AccionAudit.ELIMINAR, "FACTURA", id, "Eliminación de factura (ID: " + id + ", Monto: " + factura.getMonto() + ")", anteriorJson, null);
        } catch (Exception e) {}
    }

    /**
     * Envía un correo HTML con el resumen de la factura al email del paciente.
     * Requiere que el paciente tenga un Usuario con email registrado.
     * Endpoint: POST /api/facturas/{id}/enviar-correo
     */
    public String enviarFacturaPorCorreo(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + id));

        Paciente paciente = factura.getCita().getPaciente();
        String emailDestinatario = (paciente.getUsuario() != null)
                ? paciente.getUsuario().getEmail()
                : null;

        if (emailDestinatario == null || emailDestinatario.isBlank()) {
            throw new IllegalStateException(
                "El paciente " + paciente.getNombres() + " no tiene un email registrado.");
        }

        String subject = "🧾 Resumen de Factura — Clínica Bienestar";
        String htmlContent = crearHtmlFactura(factura, paciente);

        emailService.sendHtmlEmail(emailDestinatario, subject, htmlContent);

        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "FACTURA", id, "Envío de factura por correo electrónico al destinatario: " + emailDestinatario, null, null);
        } catch (Exception e) {}

        return "Factura enviada correctamente al correo " + emailDestinatario;
    }

    /** Genera el HTML del correo de factura. */
    private String crearHtmlFactura(Factura factura, Paciente paciente) {
        StringBuilder detalles = new StringBuilder();
        if (factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
            factura.getDetalles().forEach(d -> detalles
                .append("<tr>")
                .append("<td style='padding:8px 12px;border-bottom:1px solid #e0e7ef;'>").append(d.getDescripcionServicio()).append("</td>")
                .append("<td style='padding:8px 12px;border-bottom:1px solid #e0e7ef;text-align:right;'>S/ ")
                .append(d.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(d.getCantidad()))).append("</td>")
                .append("</tr>"));
        } else {
            detalles.append("<tr><td colspan='2' style='padding:8px 12px;color:#888;'>Sin detalles de servicio.</td></tr>");
        }

        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'></head>"
            + "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f6f9;'>"
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f6f9;padding:30px 0;'>"
            + "<tr><td align='center'><table width='600' style='background:#fff;border-radius:10px;"
            + "overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.1);max-width:600px;width:100%;'>"
            + "<tr><td style='background:linear-gradient(135deg,#2c7be5,#1a4fa0);padding:30px 40px;text-align:center;'>"
            + "<h1 style='margin:0;color:#fff;font-size:24px;'>🏥 Clínica Bienestar</h1>"
            + "<p style='margin:6px 0 0;color:#c8dcff;font-size:13px;'>Resumen de Factura</p></td></tr>"
            + "<tr><td style='padding:30px 40px;'>"
            + "<p style='color:#333;font-size:15px;'>Estimado/a <strong>"
            + paciente.getNombres() + " " + paciente.getApellidos() + "</strong>,</p>"
            + "<p style='color:#555;'>Adjuntamos el resumen de tu factura (ID: <strong>" + factura.getId() + "</strong>):</p>"
            + "<table width='100%' style='border-collapse:collapse;margin-top:16px;'>"
            + "<thead><tr style='background:#f0f5ff;'>"
            + "<th style='padding:10px 12px;text-align:left;font-size:13px;color:#2c7be5;'>Descripción</th>"
            + "<th style='padding:10px 12px;text-align:right;font-size:13px;color:#2c7be5;'>Monto</th>"
            + "</tr></thead><tbody>" + detalles + "</tbody>"
            + "<tfoot><tr style='background:#f0f5ff;'><td style='padding:10px 12px;font-weight:bold;'>Total</td>"
            + "<td style='padding:10px 12px;font-weight:bold;text-align:right;'>S/ " + factura.getMonto() + "</td></tr></tfoot>"
            + "</table>"
            + "<p style='margin-top:20px;color:#555;'>Estado: <strong>" + factura.getEstado().toUpperCase() + "</strong></p>"
            + "<p style='color:#888;font-size:12px;'>Fecha de emisión: " + factura.getFechaEmision() + "</p>"
            + "</td></tr>"
            + "<tr><td style='background:#f2f4f8;padding:20px 40px;text-align:center;border-top:1px solid #e0e6ef;'>"
            + "<p style='margin:4px 0;font-size:13px;color:#888;'>© " + java.time.Year.now().getValue() + " Clínica Bienestar.</p>"
            + "</td></tr></table></td></tr></table></body></html>";
    }
}