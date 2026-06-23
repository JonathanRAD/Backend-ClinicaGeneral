package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.DespachoDTO;
import com.clinicabienestar.api.dto.MedicamentoDTO;
import com.clinicabienestar.api.dto.LoteMedicamentoDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Medicamento;
import com.clinicabienestar.api.model.LoteMedicamento;
import com.clinicabienestar.api.repository.LoteMedicamentoRepository;
import com.clinicabienestar.api.repository.MedicamentoRepository;
import com.clinicabienestar.api.model.AccionAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final LoteMedicamentoRepository loteMedicamentoRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Medicamento> obtenerTodos() {
        return medicamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Medicamento obtenerPorId(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado con ID: " + id));
    }

    public Medicamento crearMedicamento(MedicamentoDTO dto) {
        if (medicamentoRepository.existsByCodigo(dto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un medicamento con el código " + dto.getCodigo());
        }

        Medicamento med = new Medicamento();
        med.setCodigo(dto.getCodigo());
        med.setNombre(dto.getNombre());
        med.setDescripcion(dto.getDescripcion());
        med.setFormaFarmaceutica(dto.getFormaFarmaceutica());
        med.setConcentracion(dto.getConcentracion());
        med.setPrecioUnitario(dto.getPrecioUnitario() != null ? dto.getPrecioUnitario() : 0.0);
        med.setEstado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO");

        // Si se incluyen lotes en la creación del medicamento inicial
        if (dto.getLotes() != null && !dto.getLotes().isEmpty()) {
            for (LoteMedicamentoDTO loteDto : dto.getLotes()) {
                LoteMedicamento lote = new LoteMedicamento();
                lote.setNumeroLote(loteDto.getNumeroLote());
                lote.setStock(loteDto.getStock());
                lote.setFechaVencimiento(loteDto.getFechaVencimiento());
                lote.setFechaIngreso(LocalDateTime.now());
                lote.setMedicamento(med);
                med.getLotes().add(lote);
            }
        }

        Medicamento guardado = medicamentoRepository.save(med);
        try {
            auditService.registrarEvento(AccionAudit.CREAR, "MEDICAMENTO", guardado.getId(), "Creación de medicamento: " + guardado.getNombre() + " (Código: " + guardado.getCodigo() + ")", null, auditService.toJson(guardado));
        } catch (Exception e) {}
        return guardado;
    }

    public Medicamento actualizarMedicamento(Long id, MedicamentoDTO dto) {
        Medicamento med = obtenerPorId(id);
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(med);
        } catch (Exception e) {}

        med.setNombre(dto.getNombre());
        med.setDescripcion(dto.getDescripcion());
        med.setFormaFarmaceutica(dto.getFormaFarmaceutica());
        med.setConcentracion(dto.getConcentracion());
        med.setPrecioUnitario(dto.getPrecioUnitario() != null ? dto.getPrecioUnitario() : med.getPrecioUnitario());
        med.setEstado(dto.getEstado() != null ? dto.getEstado() : med.getEstado());

        Medicamento guardado = medicamentoRepository.save(med);
        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "MEDICAMENTO", guardado.getId(), "Actualización de medicamento", anteriorJson, auditService.toJson(guardado));
        } catch (Exception e) {}
        return guardado;
    }

    public Medicamento agregarLote(Long medicamentoId, LoteMedicamentoDTO loteDto) {
        Medicamento med = obtenerPorId(medicamentoId);

        LoteMedicamento lote = new LoteMedicamento();
        lote.setNumeroLote(loteDto.getNumeroLote());
        lote.setStock(loteDto.getStock());
        lote.setFechaVencimiento(loteDto.getFechaVencimiento());
        lote.setFechaIngreso(LocalDateTime.now());
        lote.setMedicamento(med);
        
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(med);
        } catch (Exception e) {}

        med.getLotes().add(lote);

        Medicamento guardado = medicamentoRepository.save(med); // Cascade guarda el anidado
        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "MEDICAMENTO", guardado.getId(), "Adición de lote a medicamento. Lote N°: " + lote.getNumeroLote() + ", Cantidad: " + lote.getStock(), anteriorJson, auditService.toJson(guardado));
        } catch (Exception e) {}
        return guardado;
    }

    /**
     * Despacha una lista de medicamentos descontando stock con lógica FEFO
     * (First Expired, First Out - primero el lote que vence antes).
     * Lanza excepción si no hay stock suficiente para cualquier ítem.
     */
    public List<Medicamento> despacharMedicamentos(DespachoDTO despachoDTO) {
        for (DespachoDTO.ItemDespacho item : despachoDTO.getItems()) {
            int cantidadRestante = item.getCantidad();
            List<LoteMedicamento> lotes = loteMedicamentoRepository
                .findLotesConStockOrdenadosPorVencimiento(item.getMedicamentoId());

            int stockDisponible = lotes.stream().mapToInt(LoteMedicamento::getStock).sum();
            if (stockDisponible < cantidadRestante) {
                Medicamento med = obtenerPorId(item.getMedicamentoId());
                throw new IllegalStateException(
                    "Stock insuficiente para: " + med.getNombre() +
                    ". Disponible: " + stockDisponible + ", Solicitado: " + cantidadRestante
                );
            }

            // Descontar FEFO: tomar del lote más próximo a vencer primero
            for (LoteMedicamento lote : lotes) {
                if (cantidadRestante <= 0) break;
                int tomar = Math.min(lote.getStock(), cantidadRestante);
                lote.setStock(lote.getStock() - tomar);
                cantidadRestante -= tomar;
                loteMedicamentoRepository.save(lote);
            }
        }
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(despachoDTO);
        } catch (Exception e) {}

        // Devolver el estado actualizado de los medicamentos despachados
        List<Medicamento> despachados = despachoDTO.getItems().stream()
            .map(i -> obtenerPorId(i.getMedicamentoId()))
            .toList();

        try {
            auditService.registrarEvento(AccionAudit.CAMBIAR_ESTADO, "MEDICAMENTO", null, "Despacho de medicamentos en farmacia", anteriorJson, auditService.toJson(despachados));
        } catch (Exception e) {}

        return despachados;
    }

    public void eliminarMedicamento(Long id) {
        Medicamento med = obtenerPorId(id);
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(med);
        } catch (Exception e) {}

        medicamentoRepository.delete(med);
        try {
            auditService.registrarEvento(AccionAudit.ELIMINAR, "MEDICAMENTO", id, "Eliminación de medicamento (ID: " + id + ", Nombre: " + med.getNombre() + ")", anteriorJson, null);
        } catch (Exception e) {}
    }
}
