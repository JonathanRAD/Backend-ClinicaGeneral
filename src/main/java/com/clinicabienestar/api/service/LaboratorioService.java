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

    public OrdenLaboratorio crearOrden(Long consultaId, OrdenLaboratorioDTO ordenDTO) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada con ID: " + consultaId));

        OrdenLaboratorio nuevaOrden = new OrdenLaboratorio();
        nuevaOrden.setFechaOrden(LocalDate.now());
        nuevaOrden.setTipoExamen(ordenDTO.getTipoExamen());
        nuevaOrden.setObservaciones(ordenDTO.getObservaciones());
        
        // Manejando la relación: OrdenLaboratorio pertenece a una Consulta
        nuevaOrden.setConsulta(consulta);
        
        return ordenRepository.save(nuevaOrden);
    }

    public ResultadoLaboratorio cargarResultado(Long ordenId, ResultadoLaboratorioDTO resultadoDTO) {
        OrdenLaboratorio orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de laboratorio no encontrada con ID: " + ordenId));

        ResultadoLaboratorio nuevoResultado = new ResultadoLaboratorio();
        nuevoResultado.setFechaResultado(LocalDate.now());
        nuevoResultado.setDescripcion(resultadoDTO.getDescripcion());
        nuevoResultado.setValores(resultadoDTO.getValores());
        nuevoResultado.setConclusiones(resultadoDTO.getConclusiones());
        
        // Manejando la relación: ResultadoLaboratorio pertenece a una OrdenLaboratorio
        nuevoResultado.setOrdenLaboratorio(orden);

        return resultadoRepository.save(nuevoResultado);
    }
}