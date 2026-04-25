import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HistoriaClinicaService } from '../../../../services/historia-clinica';
import { HistoriaClinica } from '../../../../core/models/historia-clinica';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { FormularioConsultaComponent } from '../../componentes-panel/formulario-consulta/formulario-consulta';
import { FormularioRecetaComponent } from '../../componentes-panel/formulario-receta/formulario-receta';
import { Consulta } from '../../../../core/models/consulta';
import { SeguroMedicoService } from '../../../../services/seguro-medico';
import { FormularioSeguroComponent } from '../../componentes-panel/formulario-seguro/formulario-seguro';
import { FormularioOrdenLaboratorioComponent } from '../../componentes-panel/formulario-orden-laboratorio/formulario-orden-laboratorio';
import { LaboratorioService, OrdenLaboratorioPayload } from '../../../../services/laboratorio';
import { MatDividerModule } from '@angular/material/divider';
import { SeguroMedico } from '../../../../core/models/seguro-medico';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-historia-clinica',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule, MatCardModule, MatButtonModule, MatIconModule,
    MatTabsModule, MatFormFieldModule, MatInputModule, MatDialogModule, MatExpansionModule, MatDividerModule
  ],
  templateUrl: './historia-clinica.html',
  styleUrls: ['./historia-clinica.css']
})
export class HistoriaClinicaComponent implements OnInit {

  historia = signal<HistoriaClinica | null>(null);
  pacienteId = signal<string>('');
  modoEdicion = signal<boolean>(false);
  estadoValidacion = signal<{ valido?: boolean; mensaje?: string; verificado: boolean }>({ verificado: false });

  constructor(
    private route: ActivatedRoute,
    private historiaService: HistoriaClinicaService,
    private seguroService: SeguroMedicoService,
    private laboratorioService: LaboratorioService,
    private snackBar: MatSnackBar,
    public dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.pacienteId.set(id);
      this.cargarHistoria();
    }
  }

  cargarHistoria(): void {
    this.historiaService.getHistoriaPorPacienteId(this.pacienteId()).subscribe({
      next: (data) => this.historia.set(data),
      error: (err) => {
        console.error('Error al cargar la historia clínica:', err);
        this.snackBar.open('No se pudo cargar la historia clínica', 'Cerrar', { duration: 3000 });
      }
    });
  }

  activarEdicion(): void {
    this.modoEdicion.set(true);
  }

  guardarCambios(): void {
    const historiaActual = this.historia();
    if (!historiaActual) return;
    const datosParaActualizar = {
      antecedentes: historiaActual.antecedentes,
      alergias: historiaActual.alergias,
      enfermedadesCronicas: historiaActual.enfermedadesCronicas
    };
    this.historiaService.actualizarHistoria(historiaActual.id, datosParaActualizar).subscribe({
      next: (historiaActualizadaParcial) => {
        this.historia.update(currentHistoria => {
          if (currentHistoria) {
            currentHistoria.antecedentes = historiaActualizadaParcial.antecedentes;
            currentHistoria.alergias = historiaActualizadaParcial.alergias;
            currentHistoria.enfermedadesCronicas = historiaActualizadaParcial.enfermedadesCronicas;
          }
          return currentHistoria;
        });
        this.modoEdicion.set(false);
        this.snackBar.open('Historia clínica actualizada con éxito', 'Cerrar', { duration: 3000 });
      },
      error: (err) => this.snackBar.open('Error al guardar los cambios', 'Cerrar', { duration: 3000 })
    });
  }

  cancelarEdicion(): void {
    this.modoEdicion.set(false);
    this.cargarHistoria();
  }

  abrirFormularioSeguro(): void {
    const seguroActual = this.historia()?.paciente.seguroMedico || null;
    const dialogRef = this.dialog.open(FormularioSeguroComponent, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { seguro: seguroActual }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        const seguroId = seguroActual ? seguroActual.id : undefined;
        this.seguroService.guardarSeguro(this.pacienteId(), resultado, seguroId).subscribe({
          next: (seguroGuardado: SeguroMedico) => {
            this.historia.update(historiaActual => {
              if (historiaActual) {
                const pacienteActualizado = {
                  ...historiaActual.paciente,
                  seguroMedico: seguroGuardado
                };
                return { ...historiaActual, paciente: pacienteActualizado };
              }
              return historiaActual;
            });
            this.snackBar.open('Información del seguro guardada con éxito', 'Cerrar', { duration: 3000 });
            this.estadoValidacion.set({ verificado: false });
          },
          error: (err) => this.snackBar.open('Error al guardar la información del seguro', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }
  
  validarSeguro(): void {
    if (!this.pacienteId()) return;
    this.seguroService.validarSeguro(this.pacienteId()).subscribe({
      next: (respuesta) => {
        this.estadoValidacion.set({ ...respuesta, verificado: true });
        this.snackBar.open('Validación de seguro completada.', 'Cerrar', { duration: 2000 });
      },
      error: (err) => this.estadoValidacion.set({ valido: false, mensaje: 'No se encontró información del seguro.', verificado: true })
    });
  }
  
  abrirFormularioConsulta(): void {
    const dialogRef = this.dialog.open(FormularioConsultaComponent, {
      width: '600px',
      disableClose: true,
      panelClass: 'custom-dialog-container'
    });
    dialogRef.afterClosed().subscribe(resultado => {
      const historiaActual = this.historia();
      if (resultado && historiaActual) {
        this.historiaService.agregarConsulta(historiaActual.id, resultado).subscribe({
          next: (nuevaConsulta) => {
            this.historia.update(hist => {
              if (!hist) return null;
              const consultasActualizadas = [...(hist.consultas || []), nuevaConsulta];
              return { ...hist, consultas: consultasActualizadas };
            });
            this.snackBar.open('Consulta registrada con éxito', 'Cerrar', { duration: 3000 });
          },
          error: (err) => this.snackBar.open('Error al registrar la consulta', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }

  abrirFormularioOrden(consultaId: number): void {
    const dialogRef = this.dialog.open(FormularioOrdenLaboratorioComponent, {
      width: '550px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { consultaId }
    });
    dialogRef.afterClosed().subscribe((resultado: OrdenLaboratorioPayload) => {
      if (resultado) {
        this.laboratorioService.crearOrden(consultaId, resultado).subscribe({
          next: (nuevaOrden) => {
            this.historia.update(currentHistoria => {
              if (!currentHistoria) return null;
              const nuevasConsultas = currentHistoria.consultas.map(consulta => {
                if (consulta.id === consultaId) {
                  return {
                    ...consulta,
                    ordenesLaboratorio: [...(consulta.ordenesLaboratorio || []), nuevaOrden]
                  };
                }
                return consulta;
              });
              return { ...currentHistoria, consultas: nuevasConsultas };
            });
            this.snackBar.open('Orden de laboratorio creada con éxito', 'Cerrar', { duration: 3000 });
          },
          error: (err) => this.snackBar.open('No se pudo crear la orden', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }
  
  abrirFormularioReceta(consultaId: number): void {
    const dialogRef = this.dialog.open(FormularioRecetaComponent, {
      width: '800px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { consultaId }
    });
    
    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.historiaService.agregarReceta(consultaId, resultado).subscribe({
          next: (nuevaReceta) => {
            this.historia.update(currentHistoria => {
              if (!currentHistoria) return null;
              const nuevasConsultas = currentHistoria.consultas.map(consulta => {
                if (consulta.id === consultaId) {
                  return {
                    ...consulta,
                    recetas: [...(consulta.recetas || []), nuevaReceta]
                  };
                }
                return consulta;
              });
              return { ...currentHistoria, consultas: nuevasConsultas };
            });
            this.snackBar.open('Receta médica emitida con éxito', 'Cerrar', { duration: 3000 });
          },
          error: (err) => this.snackBar.open('No se pudo emitir la receta', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }

  imprimirReceta(receta: any, consulta: Consulta): void {
    const doc = new jsPDF();
    const hc = this.historia();
    if (!hc) return;

    // Encabezado
    doc.setFontSize(22);
    doc.setTextColor(13, 110, 253);
    doc.text('Clínica Bienestar', 105, 20, { align: 'center' });
    doc.setFontSize(14);
    doc.setTextColor(100);
    doc.text('Receta Médica', 105, 28, { align: 'center' });

    doc.setDrawColor(200);
    doc.line(15, 35, 195, 35);

    // Datos del Paciente
    doc.setFontSize(12);
    doc.setTextColor(0);
    doc.text(`Paciente: ${hc.paciente.nombres} ${hc.paciente.apellidos}`, 15, 45);
    doc.text(`DNI: ${hc.paciente.dni} | Teléfono: ${hc.paciente.telefono || 'N/A'}`, 15, 52);
    
    doc.text(`Fecha: ${new Date(receta.fechaEmision).toLocaleDateString()}`, 195, 45, { align: 'right' });
    
    const drName = `Dr(a): ${consulta.medico.nombres} ${consulta.medico.apellidos}`;
    if (drName.length > 35) {
      const drSplit = doc.splitTextToSize(drName, 80);
      doc.text(drSplit, 195, 52, { align: 'right' });
    } else {
      doc.text(drName, 195, 52, { align: 'right' });
    }

    // Medicamentos Table
    if (receta.detalles && receta.detalles.length > 0) {
      const tableData = receta.detalles.map((d: any) => [
        (d.medicamento && d.medicamento.nombre) ? d.medicamento.nombre : ('ID:' + d.medicamentoId),
        d.cantidadSolicitada,
        d.dosis,
        d.frecuencia,
        d.duracion
      ]);

      autoTable(doc, {
        startY: 65,
        head: [['Medicamento', 'Cant.', 'Dosis', 'Frecuencia', 'Duración']],
        body: tableData,
        theme: 'striped',
        headStyles: { fillColor: [13, 110, 253] }
      });
    }

    let finalY = (doc as any).lastAutoTable ? (doc as any).lastAutoTable.finalY : 65;
    
    if (receta.indicacionesGenerales) {
      doc.setFontSize(12);
      doc.setTextColor(0);
      doc.text('Indicaciones Generales:', 15, finalY + 15);
      doc.setFontSize(10);
      doc.setTextColor(80);
      const warnings = doc.splitTextToSize(receta.indicacionesGenerales, 180);
      doc.text(warnings, 15, finalY + 22);
    }

    doc.setFontSize(10);
    doc.setTextColor(150);
    doc.text('Firma y Sello del Médico', 105, 270, { align: 'center' });
    doc.line(70, 265, 140, 265);

    doc.save(`Receta_${hc.paciente.dni}_${new Date().getTime()}.pdf`);
  }

  /** Genera e imprime en PDF toda la historia clínica del paciente */
  imprimirHistoriaCompleta(): void {
    const hc = this.historia();
    if (!hc) return;

    const doc = new jsPDF();
    const pageW = doc.internal.pageSize.getWidth();
    const primaryColor: [number, number, number] = [13, 110, 253];
    const darkGray: [number, number, number] = [30, 30, 30];
    const medGray: [number, number, number] = [100, 100, 100];
    const lightBg: [number, number, number] = [245, 248, 255];

    // ── Encabezado ────────────────────────────────────────────────────────────
    doc.setFillColor(...primaryColor);
    doc.rect(0, 0, pageW, 32, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text('Clínica Bienestar', pageW / 2, 14, { align: 'center' });
    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    doc.text('Historia Clínica del Paciente', pageW / 2, 24, { align: 'center' });

    // ── Datos del paciente ────────────────────────────────────────────────────
    let y = 42;
    doc.setFillColor(...lightBg);
    doc.roundedRect(10, y - 5, pageW - 20, 38, 3, 3, 'F');

    doc.setTextColor(...darkGray);
    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.text(`${hc.paciente.nombres} ${hc.paciente.apellidos}`, 15, y + 4);

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(...medGray);
    doc.text(`DNI: ${hc.paciente.dni}`, 15, y + 12);
    doc.text(`Teléfono: ${hc.paciente.telefono || 'N/A'}`, 75, y + 12);
    doc.text(`Fecha de impresión: ${new Date().toLocaleDateString('es-PE')}`, pageW - 15, y + 4, { align: 'right' });

    y += 42;

    // ── Sección: Información General ─────────────────────────────────────────
    const drawSectionTitle = (title: string, yPos: number) => {
      doc.setFillColor(...primaryColor);
      doc.rect(10, yPos, 4, 8, 'F');
      doc.setTextColor(...darkGray);
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text(title, 17, yPos + 6);
      return yPos + 14;
    };

    y = drawSectionTitle('Información Médica General', y);

    autoTable(doc, {
      startY: y,
      head: [['Campo', 'Detalle']],
      body: [
        ['Antecedentes', hc.antecedentes || 'No registrados'],
        ['Alergias', hc.alergias || 'No registradas'],
        ['Enfermedades Crónicas', hc.enfermedadesCronicas || 'No registradas'],
      ],
      theme: 'grid',
      headStyles: { fillColor: primaryColor, fontSize: 9, fontStyle: 'bold' },
      bodyStyles: { fontSize: 9 },
      columnStyles: { 0: { fontStyle: 'bold', cellWidth: 55 } },
      margin: { left: 10, right: 10 },
    });

    y = (doc as any).lastAutoTable.finalY + 10;

    // ── Sección: Seguro Médico ────────────────────────────────────────────────
    if (hc.paciente.seguroMedico) {
      const s = hc.paciente.seguroMedico;
      y = drawSectionTitle('Seguro Médico', y);
      autoTable(doc, {
        startY: y,
        head: [['Aseguradora', 'N° Póliza', 'Cobertura']],
        body: [[s.nombreAseguradora || '-', s.numeroPoliza || '-', s.cobertura || '-']],
        theme: 'striped',
        headStyles: { fillColor: primaryColor, fontSize: 9, fontStyle: 'bold' },
        bodyStyles: { fontSize: 9 },
        margin: { left: 10, right: 10 },
      });
      y = (doc as any).lastAutoTable.finalY + 10;
    }

    // ── Sección: Historial de Consultas ───────────────────────────────────────
    if (hc.consultas && hc.consultas.length > 0) {
      y = drawSectionTitle('Historial de Consultas', y);

      hc.consultas.forEach((consulta: any, idx: number) => {
        // Verificar si necesitamos nueva página
        if (y > 230) {
          doc.addPage();
          y = 20;
        }

        // Sub-encabezado de consulta
        doc.setFillColor(230, 240, 255);
        doc.rect(10, y, pageW - 20, 10, 'F');
        doc.setTextColor(...primaryColor);
        doc.setFontSize(10);
        doc.setFont('helvetica', 'bold');
        const fechaConsulta = new Date(consulta.fechaConsulta).toLocaleDateString('es-PE');
        doc.text(`Consulta ${idx + 1} — ${fechaConsulta} | Dr(a). ${consulta.medico?.nombres || ''} ${consulta.medico?.apellidos || ''}`, 14, y + 7);
        y += 13;

        autoTable(doc, {
          startY: y,
          head: [['Campo', 'Detalle']],
          body: [
            ['Motivo', consulta.motivo || '-'],
            ['Diagnóstico', consulta.diagnostico || '-'],
            ['Tratamiento', consulta.tratamiento || '-'],
          ],
          theme: 'plain',
          headStyles: { fillColor: [220, 230, 250], fontSize: 8, fontStyle: 'bold', textColor: darkGray },
          bodyStyles: { fontSize: 8 },
          columnStyles: { 0: { fontStyle: 'bold', cellWidth: 40 } },
          margin: { left: 10, right: 10 },
          tableLineColor: [210, 218, 230],
          tableLineWidth: 0.1,
        });
        y = (doc as any).lastAutoTable.finalY + 4;

        // Signos Vitales (Triaje)
        if (consulta.cita?.triaje) {
          const t = consulta.cita.triaje;
          autoTable(doc, {
            startY: y,
            head: [['Temperatura', 'Presión', 'Frec. Card.', 'SpO₂', 'Peso', 'Glucemia']],
            body: [[
              `${t.temperatura || '-'}°C`,
              t.presionArterial || '-',
              `${t.ritmoCardiaco || '-'} lpm`,
              `${t.saturacionOxigeno || '-'}%`,
              `${t.peso || '-'} kg`,
              t.nivelAzucar ? `${t.nivelAzucar} mg/dL` : 'N/A'
            ]],
            theme: 'grid',
            headStyles: { fillColor: [34, 139, 84], fontSize: 7, fontStyle: 'bold', textColor: [255, 255, 255] },
            bodyStyles: { fontSize: 8, halign: 'center' },
            margin: { left: 10, right: 10 },
          });
          y = (doc as any).lastAutoTable.finalY + 4;
        }

        // Recetas de esta consulta
        if (consulta.recetas && consulta.recetas.length > 0) {
          consulta.recetas.forEach((receta: any) => {
            if (receta.detalles && receta.detalles.length > 0) {
              autoTable(doc, {
                startY: y,
                head: [['Medicamento (receta)', 'Cant.', 'Dosis', 'Frecuencia', 'Duración']],
                body: receta.detalles.map((d: any) => [
                  d.medicamento?.nombre || `ID:${d.medicamentoId}`,
                  d.cantidadSolicitada,
                  d.dosis,
                  d.frecuencia,
                  d.duracion
                ]),
                theme: 'striped',
                headStyles: { fillColor: [111, 66, 193], fontSize: 7, fontStyle: 'bold', textColor: [255, 255, 255] },
                bodyStyles: { fontSize: 8 },
                margin: { left: 10, right: 10 },
              });
              y = (doc as any).lastAutoTable.finalY + 4;
            }
          });
        }

        y += 4;
      });
    }

    // ── Pie de página ─────────────────────────────────────────────────────────
    const pageCount = (doc as any).internal.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.setTextColor(160);
      doc.text(
        `Página ${i} de ${pageCount} — Documento generado por Clínica Bienestar`,
        pageW / 2,
        doc.internal.pageSize.getHeight() - 8,
        { align: 'center' }
      );
    }

    const nombreArchivo = `HistoriaClinica_${hc.paciente.dni}_${new Date().toISOString().slice(0, 10)}.pdf`;
    const blob = doc.output('blob');
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nombreArchivo;
    link.type = 'application/pdf';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    this.snackBar.open('Historia clínica descargada en PDF', 'Cerrar', { duration: 3000 });
  }

  trackByConsultaId(index: number, consulta: Consulta): number {
    return consulta.id;
  }
}