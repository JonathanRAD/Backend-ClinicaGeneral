package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.OrdenLaboratorioDTO;
import com.clinicabienestar.api.dto.ResultadoLaboratorioDTO;
import com.clinicabienestar.api.model.OrdenLaboratorio;
import com.clinicabienestar.api.model.ResultadoLaboratorio;
import com.clinicabienestar.api.repository.ConsultaRepository;
import com.clinicabienestar.api.repository.OrdenLaboratorioRepository;
import com.clinicabienestar.api.repository.ResultadoLaboratorioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/laboratorio")
@CrossOrigin(origins = "http://localhost:4200")
public class LaboratorioController {

    @Autowired private OrdenLaboratorioRepository ordenRepository;
    @Autowired private ResultadoLaboratorioRepository resultadoRepository;
    @Autowired private ConsultaRepository consultaRepository;

    // Endpoint para CREAR una nueva orden de laboratorio para una consulta
    @PostMapping("/ordenes/consulta/{consultaId}")
    public ResponseEntity<OrdenLaboratorio> crearOrden(@PathVariable Long consultaId, @RequestBody OrdenLaboratorioDTO ordenDTO) {
        return consultaRepository.findById(consultaId).map(consulta -> {
            OrdenLaboratorio nuevaOrden = new OrdenLaboratorio();
            nuevaOrden.setFechaOrden(LocalDate.now());
            nuevaOrden.setTipoExamen(ordenDTO.getTipoExamen());
            nuevaOrden.setObservaciones(ordenDTO.getObservaciones());
            nuevaOrden.setConsulta(consulta);
            
            OrdenLaboratorio ordenGuardada = ordenRepository.save(nuevaOrden);
            return ResponseEntity.ok(ordenGuardada);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para CARGAR el resultado de una orden
    @PostMapping("/resultados/orden/{ordenId}")
    public ResponseEntity<ResultadoLaboratorio> cargarResultado(@PathVariable Long ordenId, @RequestBody ResultadoLaboratorioDTO resultadoDTO) {
        return ordenRepository.findById(ordenId).map(orden -> {
            ResultadoLaboratorio nuevoResultado = new ResultadoLaboratorio();
            nuevoResultado.setFechaResultado(LocalDate.now());
            nuevoResultado.setDescripcion(resultadoDTO.getDescripcion());
            nuevoResultado.setValores(resultadoDTO.getValores());
            nuevoResultado.setConclusiones(resultadoDTO.getConclusiones());
            nuevoResultado.setOrdenLaboratorio(orden);

            ResultadoLaboratorio resultadoGuardado = resultadoRepository.save(nuevoResultado);
            return ResponseEntity.ok(resultadoGuardado);
        }).orElse(ResponseEntity.notFound().build());
    }
}