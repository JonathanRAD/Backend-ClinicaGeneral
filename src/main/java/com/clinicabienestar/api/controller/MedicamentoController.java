package com.clinicabienestar.api.controller;

import com.clinicabienestar.api.dto.DespachoDTO;
import com.clinicabienestar.api.dto.MedicamentoDTO;
import com.clinicabienestar.api.dto.LoteMedicamentoDTO;
import com.clinicabienestar.api.model.Medicamento;
import com.clinicabienestar.api.service.MedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @GetMapping
    @PreAuthorize("hasAuthority('VER_INVENTARIO') or hasAuthority('GESTIONAR_INVENTARIO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Medicamento>> obtenerTodos() {
        return ResponseEntity.ok(medicamentoService.obtenerTodos());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTIONAR_INVENTARIO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Medicamento> crearMedicamento(@Valid @RequestBody MedicamentoDTO dto) {
        return new ResponseEntity<>(medicamentoService.crearMedicamento(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GESTIONAR_INVENTARIO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Medicamento> actualizarMedicamento(@PathVariable Long id, @Valid @RequestBody MedicamentoDTO dto) {
        return ResponseEntity.ok(medicamentoService.actualizarMedicamento(id, dto));
    }

    @PostMapping("/{id}/lotes")
    @PreAuthorize("hasAuthority('GESTIONAR_INVENTARIO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Medicamento> agregarLote(@PathVariable Long id, @Valid @RequestBody LoteMedicamentoDTO loteDto) {
        return new ResponseEntity<>(medicamentoService.agregarLote(id, loteDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GESTIONAR_INVENTARIO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarMedicamento(@PathVariable Long id) {
        medicamentoService.eliminarMedicamento(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/despachar")
    @PreAuthorize("hasAuthority('GESTIONAR_FACTURACION') or hasAuthority('GESTIONAR_INVENTARIO') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> despacharMedicamentos(@RequestBody DespachoDTO despachoDTO) {
        medicamentoService.despacharMedicamentos(despachoDTO);
        return ResponseEntity.noContent().build();
    }
}
