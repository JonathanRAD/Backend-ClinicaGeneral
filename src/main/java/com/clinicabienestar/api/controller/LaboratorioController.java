package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.OrdenLaboratorioDTO;
import com.clinicabienestar.api.dto.ResultadoLaboratorioDTO;
import com.clinicabienestar.api.model.OrdenLaboratorio;
import com.clinicabienestar.api.model.ResultadoLaboratorio;
import com.clinicabienestar.api.service.LaboratorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/laboratorio")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class LaboratorioController {

    private final LaboratorioService laboratorioService;

    @PostMapping("/ordenes/consulta/{consultaId}")
    @PreAuthorize("hasAuthority('EDITAR_HISTORIAL_CLINICO')")
    public ResponseEntity<OrdenLaboratorio> crearOrden(@PathVariable Long consultaId, @Valid @RequestBody OrdenLaboratorioDTO ordenDTO) {
        OrdenLaboratorio ordenGuardada = laboratorioService.crearOrden(consultaId, ordenDTO);
        return ResponseEntity.ok(ordenGuardada);
    }

    @PostMapping("/resultados/orden/{ordenId}")
    @PreAuthorize("hasAuthority('EDITAR_HISTORIAL_CLINICO')")
    public ResponseEntity<ResultadoLaboratorio> cargarResultado(@PathVariable Long ordenId, @Valid @RequestBody ResultadoLaboratorioDTO resultadoDTO) {
        ResultadoLaboratorio resultadoGuardado = laboratorioService.cargarResultado(ordenId, resultadoDTO);
        return ResponseEntity.ok(resultadoGuardado);
    }
}