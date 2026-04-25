
import { Injectable } from '@angular/core';
import { CitaService } from './cita';
import { MedicamentoService } from './medicamento.service';
import * as ExcelJS from 'exceljs'; 
import { saveAs } from 'file-saver'; 

@Injectable({
  providedIn: 'root'
})
export class ReporteService {

  constructor(
    private citaService: CitaService,
    private medicamentoService: MedicamentoService
  ) { }

  // ── Utilidad para estilo de encabezado ────────────────────────────────────
  private aplicarEstiloEncabezado(worksheet: ExcelJS.Worksheet, color: string = 'FF0D6EFD'): void {
    const headerRow = worksheet.getRow(1);
    headerRow.eachCell((cell) => {
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: color } };
      cell.font = { color: { argb: 'FFFFFFFF' }, bold: true, size: 12 };
      cell.alignment = { vertical: 'middle', horizontal: 'center' };
    });
  }

  private aplicarBordes(worksheet: ExcelJS.Worksheet): void {
    worksheet.eachRow({ includeEmpty: false }, (row) => {
      row.eachCell({ includeEmpty: true }, (cell) => {
        cell.border = {
          top: { style: 'thin' }, left: { style: 'thin' },
          bottom: { style: 'thin' }, right: { style: 'thin' }
        };
      });
    });
  }

  // ── Reporte de Citas ─────────────────────────────────────────────────────
  async generarReporteDeCitasXLSX(): Promise<void> {
    const citas = this.citaService.citas();
    if (citas.length === 0) { alert('No hay citas para generar un reporte.'); return; }

    const workbook = new ExcelJS.Workbook();
    workbook.creator = 'ClínicaBienestar';
    workbook.created = new Date();
    const worksheet = workbook.addWorksheet('Reporte de Citas');
    worksheet.columns = [
      { header: 'ID Cita', key: 'id', width: 10 },
      { header: 'Fecha y Hora', key: 'fechaHora', width: 22, style: { numFmt: 'dd/mm/yyyy hh:mm:ss' } },
      { header: 'Paciente', key: 'paciente', width: 35 },
      { header: 'DNI Paciente', key: 'dni', width: 15 },
      { header: 'Médico', key: 'medico', width: 35 },
      { header: 'Especialidad', key: 'especialidad', width: 20 },
      { header: 'Estado', key: 'estado', width: 15 },
      { header: 'Motivo', key: 'motivo', width: 50 }
    ];
    this.aplicarEstiloEncabezado(worksheet);
    worksheet.addRows(citas.map(c => ({
      id: c.id, fechaHora: new Date(c.fechaHora),
      paciente: `${c.paciente.nombres} ${c.paciente.apellidos}`,
      dni: c.paciente.dni,
      medico: `Dr(a). ${c.medico.nombres} ${c.medico.apellidos}`,
      especialidad: c.medico.especialidad, estado: c.estado, motivo: c.motivo
    })));
    this.aplicarBordes(worksheet);
    const buffer = await workbook.xlsx.writeBuffer();
    saveAs(new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }), 'Reporte_De_Citas.xlsx');
  }

  generarReporteDeCitasCSV(): void {
    const citas = this.citaService.citas();
    if (citas.length === 0) { alert('No hay citas para generar un reporte.'); return; }
    const cabeceras = ['ID Cita', 'Fecha y Hora', 'Estado', 'Paciente', 'DNI Paciente', 'Medico', 'Especialidad Medico', 'Motivo'];
    const filas = citas.map(c => [
      c.id, new Date(c.fechaHora).toLocaleString('es-ES'), c.estado,
      `${c.paciente.nombres} ${c.paciente.apellidos}`, c.paciente.dni,
      `${c.medico.nombres} ${c.medico.apellidos}`, c.medico.especialidad,
      `"${c.motivo.replace(/"/g, '""')}"`
    ].join(','));
    saveAs(new Blob([[cabeceras.join(','), ...filas].join('\n')], { type: 'text/csv;charset=utf-8;' }), 'reporte_de_citas.csv');
  }

  // ── Reporte de Triajes ───────────────────────────────────────────────────
  async generarReporteTriajesXLSX(): Promise<void> {
    const citas = this.citaService.citas().filter(c => c.triaje);
    if (citas.length === 0) { alert('No hay triajes registrados.'); return; }

    const workbook = new ExcelJS.Workbook();
    workbook.creator = 'ClínicaBienestar';
    const ws = workbook.addWorksheet('Reporte de Triajes');
    ws.columns = [
      { header: 'ID', key: 'id', width: 8 },
      { header: 'Fecha Registro', key: 'fecha', width: 20 },
      { header: 'Paciente', key: 'paciente', width: 30 },
      { header: 'DNI', key: 'dni', width: 14 },
      { header: 'Médico', key: 'medico', width: 30 },
      { header: 'Temperatura (°C)', key: 'temp', width: 16 },
      { header: 'Presión', key: 'presion', width: 12 },
      { header: 'Ritmo Card. (lpm)', key: 'ritmo', width: 16 },
      { header: 'SpO₂ (%)', key: 'spo2', width: 10 },
      { header: 'Peso (kg)', key: 'peso', width: 10 },
      { header: 'Glucemia (mg/dL)', key: 'glucemia', width: 16 },
      { header: 'Notas', key: 'notas', width: 40 }
    ];
    this.aplicarEstiloEncabezado(ws, 'FF198754');
    ws.addRows(citas.map(c => ({
      id: c.triaje!.id,
      fecha: c.triaje!.fechaRegistro ? new Date(c.triaje!.fechaRegistro) : '',
      paciente: `${c.paciente.nombres} ${c.paciente.apellidos}`,
      dni: c.paciente.dni,
      medico: `Dr(a). ${c.medico.nombres} ${c.medico.apellidos}`,
      temp: c.triaje!.temperatura,
      presion: c.triaje!.presionArterial,
      ritmo: c.triaje!.ritmoCardiaco,
      spo2: c.triaje!.saturacionOxigeno,
      peso: c.triaje!.peso,
      glucemia: c.triaje!.nivelAzucar ?? '—',
      notas: c.triaje!.notasOpcionales ?? ''
    })));
    this.aplicarBordes(ws);
    const buffer = await workbook.xlsx.writeBuffer();
    saveAs(new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }), 'Reporte_Triajes.xlsx');
  }

  // ── Reporte de Inventario ────────────────────────────────────────────────
  async generarReporteInventarioXLSX(): Promise<void> {
    const medicamentos = this.medicamentoService.medicamentos();
    if (medicamentos.length === 0) { alert('No hay medicamentos en inventario.'); return; }

    const workbook = new ExcelJS.Workbook();
    workbook.creator = 'ClínicaBienestar';
    const ws = workbook.addWorksheet('Inventario Farmacia');
    ws.columns = [
      { header: 'ID', key: 'id', width: 8 },
      { header: 'Medicamento', key: 'nombre', width: 35 },
      { header: 'Tipo', key: 'tipo', width: 20 },
      { header: 'Stock Total', key: 'stock', width: 14 },
      { header: 'Precio Unit.', key: 'precio', width: 14, style: { numFmt: '"S/ "#,##0.00' } },
      { header: 'Estado', key: 'estado', width: 16 }
    ];
    this.aplicarEstiloEncabezado(ws, 'FF6F42C1');
    ws.addRows(medicamentos.map(m => ({
      id: m.id, nombre: m.nombre, tipo: m.formaFarmaceutica ?? '—',
      stock: m.stockTotal ?? 0,
      precio: m.precioUnitario ?? 0,
      estado: (m.stockTotal ?? 0) === 0 ? 'AGOTADO' : (m.stockTotal ?? 0) < 10 ? 'CRÍTICO' : 'DISPONIBLE'
    })));
    // Colorear filas críticas
    ws.eachRow({ includeEmpty: false }, (row, rowNum) => {
      if (rowNum === 1) return;
      const stock = row.getCell('stock').value as number;
      if (stock === 0) {
        row.eachCell(cell => { cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFFFC7CE' } }; });
      } else if (stock < 10) {
        row.eachCell(cell => { cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFFFFFCC' } }; });
      }
    });
    this.aplicarBordes(ws);
    const buffer = await workbook.xlsx.writeBuffer();
    saveAs(new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }), 'Reporte_Inventario_Farmacia.xlsx');
  }
}