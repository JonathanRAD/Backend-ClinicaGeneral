import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { ReporteService } from '../../../../services/reporte';
import { CitaService } from '../../../../services/cita';
import { MedicamentoService } from '../../../../services/medicamento.service';
import { DialogoPrevisualizacionComponent } from '../dialogo-previsualizacion/dialogo-previsualizacion';

@Component({
  selector: 'app-gestion-reportes',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './gestion-reportes.html',
  styleUrls: ['./gestion-reportes.css']
})
export class GestionReportesComponent implements OnInit {

  totalCitas = 0;
  totalTriajes = 0;
  totalMedicamentos = 0;
  stockCritico = 0;

  constructor(
    private reporteService: ReporteService,
    private citaService: CitaService,
    private medicamentoService: MedicamentoService,
    public dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.citaService.cargarCitasPanel();
    this.medicamentoService.cargarMedicamentos();
    // Actualizar stats cuando los signals se resuelvan
    setTimeout(() => {
      this.totalCitas = this.citaService.citas().length;
      this.totalTriajes = this.citaService.citas().filter(c => c.triaje).length;
      this.totalMedicamentos = this.medicamentoService.medicamentos().length;
      this.stockCritico = this.medicamentoService.medicamentos().filter(m => (m.stockTotal ?? 0) < 10).length;
    }, 800);
  }

  // ── Citas ─────────────────────────────────────────────────────────────────
  descargarReporteCitasXLSX(): void { this.reporteService.generarReporteDeCitasXLSX(); }
  descargarReporteCitasCSV(): void { this.reporteService.generarReporteDeCitasCSV(); }

  previsualizarReporteCitas(): void {
    const citas = this.citaService.citas().slice(0, 5);
    if (citas.length === 0) { alert('No hay datos para previsualizar.'); return; }
    this.dialog.open(DialogoPrevisualizacionComponent, {
      width: '800px', disableClose: true,
      data: {
        titulo: 'Previsualización: Reporte General de Citas',
        headers: ['ID Cita', 'Fecha', 'Paciente', 'Médico', 'Estado'],
        rows: citas.map(c => [c.id, new Date(c.fechaHora).toLocaleString('es-ES'),
          `${c.paciente.nombres} ${c.paciente.apellidos}`,
          `Dr(a). ${c.medico.nombres} ${c.medico.apellidos}`, c.estado])
      }
    });
  }

  // ── Triajes ───────────────────────────────────────────────────────────────
  descargarReporteTriajesXLSX(): void { this.reporteService.generarReporteTriajesXLSX(); }

  previsualizarReporteTriajes(): void {
    const citas = this.citaService.citas().filter(c => c.triaje).slice(0, 5);
    if (citas.length === 0) { alert('No hay triajes registrados para previsualizar.'); return; }
    this.dialog.open(DialogoPrevisualizacionComponent, {
      width: '900px', disableClose: true,
      data: {
        titulo: 'Previsualización: Reporte de Triajes',
        headers: ['Paciente', 'DNI', 'Temp.', 'Presión', 'Ritmo Card.', 'SpO₂', 'Peso'],
        rows: citas.map(c => [
          `${c.paciente.nombres} ${c.paciente.apellidos}`, c.paciente.dni,
          `${c.triaje!.temperatura}°C`, c.triaje!.presionArterial,
          `${c.triaje!.ritmoCardiaco} lpm`, `${c.triaje!.saturacionOxigeno}%`,
          `${c.triaje!.peso} kg`
        ])
      }
    });
  }

  // ── Inventario ───────────────────────────────────────────────────────────
  descargarReporteInventarioXLSX(): void { this.reporteService.generarReporteInventarioXLSX(); }

  previsualizarReporteInventario(): void {
    const meds = this.medicamentoService.medicamentos().slice(0, 5);
    if (meds.length === 0) { alert('No hay inventario para previsualizar.'); return; }
    this.dialog.open(DialogoPrevisualizacionComponent, {
      width: '800px', disableClose: true,
      data: {
        titulo: 'Previsualización: Inventario Farmacia',
        headers: ['Medicamento', 'Tipo', 'Stock', 'Precio', 'Estado'],
        rows: meds.map(m => [
          m.nombre, m.formaFarmaceutica ?? '—', m.stockTotal ?? 0,
          `S/ ${(m.precioUnitario ?? 0).toFixed(2)}`,
          (m.stockTotal ?? 0) === 0 ? 'AGOTADO' : (m.stockTotal ?? 0) < 10 ? 'CRÍTICO' : 'DISPONIBLE'
        ])
      }
    });
  }
}