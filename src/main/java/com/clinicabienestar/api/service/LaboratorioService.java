// RUTA: src/main/java/com/clinicabienestar/api/service/LaboratorioService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.OrdenLaboratorioDTO;
import com.clinicabienestar.api.dto.ResultadoLaboratorioDTO;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Consulta;
import com.clinicabienestar.api.model.OrdenLaboratorio;
import com.clinicabienestar.api.model.ResultadoLaboratorio;
import com.clinicabienestar.api.repository.ConsultaRepository;
import com.clinicabienestar.api.repository.OrdenLaboratorioRepository;
import com.clinicabienestar.api.repository.ResultadoLaboratorioRepository;
import com.clinicabienestar.api.model.AccionAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class LaboratorioService {

    private final OrdenLaboratorioRepository ordenRepository;
    private final ResultadoLaboratorioRepository resultadoRepository;
    private final ConsultaRepository consultaRepository;
    private final AuditService auditService;

    public OrdenLaboratorio crearOrden(Long consultaId, OrdenLaboratorioDTO ordenDTO) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada con ID: " + consultaId));

        OrdenLaboratorio nuevaOrden = new OrdenLaboratorio();
        nuevaOrden.setFechaOrden(LocalDate.now());
        nuevaOrden.setTipoExamen(ordenDTO.getTipoExamen());
        nuevaOrden.setObservaciones(ordenDTO.getObservaciones());
        
        // Manejando la relación: OrdenLaboratorio pertenece a una Consulta
        nuevaOrden.setConsulta(consulta);
        
        OrdenLaboratorio guardada = ordenRepository.save(nuevaOrden);
        try {
            auditService.registrarEvento(AccionAudit.CREAR, "ORDEN_LABORATORIO", guardada.getId(), "Creación de orden de laboratorio de tipo: " + guardada.getTipoExamen() + " para la consulta ID: " + consultaId, null, auditService.toJson(guardada));
        } catch (Exception e) {}
        return guardada;
    }

    public ResultadoLaboratorio cargarResultado(Long ordenId, ResultadoLaboratorioDTO resultadoDTO) {
        OrdenLaboratorio orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de laboratorio no encontrada con ID: " + ordenId));

        ResultadoLaboratorio nuevoResultado = new ResultadoLaboratorio();
        nuevoResultado.setFechaResultado(LocalDate.now());
        nuevoResultado.setDescripcion(resultadoDTO.getDescripcion());
        nuevoResultado.setValores(resultadoDTO.getValores());
        nuevoResultado.setConclusiones(resultadoDTO.getConclusiones());
        
        nuevoResultado.setOrdenLaboratorio(orden);

        ResultadoLaboratorio guardado = resultadoRepository.save(nuevoResultado);
        try {
            auditService.registrarEvento(AccionAudit.CREAR, "RESULTADO_LABORATORIO", guardado.getId(), "Carga de resultado de laboratorio para la orden ID: " + ordenId, null, auditService.toJson(guardado));
        } catch (Exception e) {}
        return guardado;
    }
}