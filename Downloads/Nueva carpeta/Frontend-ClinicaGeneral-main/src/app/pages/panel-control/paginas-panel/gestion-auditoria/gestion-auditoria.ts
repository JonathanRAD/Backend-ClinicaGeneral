import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuditoriaService } from '../../../../services/auditoria.service';
import { AuditFiltros, AuditEstadisticas } from '../../../../core/models/auditoria';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

// Angular Material Modules
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-gestion-auditoria',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatPaginatorModule, MatIconModule, DatePipe,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatDatepickerModule,
    MatNativeDateModule, MatButtonModule
  ],
  templateUrl: './gestion-auditoria.html',
  styleUrls: ['./gestion-auditoria.css']
})
export class GestionAuditoria implements OnInit {
  // ── Inyecciones Modernas vía inject() ────────────────────────────────────
  private auditoriaService = inject(AuditoriaService);
  private snackBar = inject(MatSnackBar);

  // ── Vinculación Directa a Señales del Servicio (100% Reactivo) ────────────
  logs = this.auditoriaService.logs;
  totalElements = this.auditoriaService.totalElements;
  totalPages = this.auditoriaService.totalPages;
  isLoading = this.auditoriaService.isLoading;
  estadisticas = this.auditoriaService.estadisticas;

  // ── Estado Local del Componente ───────────────────────────────────────────
  filtros = signal<AuditFiltros>({});
  currentPage = signal(0);
  pageSize = signal(20);
  expandedRowId = signal<number | null>(null);

  // ── Computed: Total de usuarios activos ───────────────────────────────────
  totalUsuariosActivos = computed(() => {
    const stats = this.estadisticas();
    return stats?.usuariosMasActivos?.length ?? 0;
  });

  // ── Listas para los dropdowns de filtros ──────────────────────────────────
  roles = ['ADMINISTRADOR', 'MEDICO', 'ENFERMERA', 'RECEPCIONISTA', 'CAJERO', 'PACIENTE'];

  acciones = [
    'LOGIN', 'CREAR', 'ACTUALIZAR', 'ELIMINAR',
    'CAMBIAR_ESTADO', 'CAMBIAR_CONTRASENA', 'RESET_PASSWORD', 'REGISTRO'
  ];

  entidades = [
    'USUARIO', 'PACIENTE', 'CITA', 'FACTURA', 'MEDICO',
    'MEDICAMENTO', 'HISTORIA_CLINICA', 'CONFERENCIA',
    'LABORATORIO', 'SEGURO_MEDICO', 'TRIAJE'
  ];

  ngOnInit(): void {
    this.cargarLogs();
    this.cargarEstadisticas();
  }

  /**
   * Carga los logs de auditoría con los filtros y paginación actuales.
   */
  cargarLogs(): void {
    this.auditoriaService.getLogs(this.filtros(), this.currentPage(), this.pageSize()).subscribe({
      error: () => {
        this.snackBar.open('Error al cargar los registros de auditoría', 'Cerrar', { duration: 3000 });
      }
    });
  }

  /**
   * Carga las estadísticas de auditoría.
   */
  cargarEstadisticas(): void {
    this.auditoriaService.getEstadisticas().subscribe({
      error: () => {
        this.snackBar.open('Error al cargar estadísticas', 'Cerrar', { duration: 3000 });
      }
    });
  }

  /**
   * Aplica los filtros y recarga los logs desde la primera página.
   */
  aplicarFiltros(): void {
    this.currentPage.set(0);
    this.cargarLogs();
  }

  /**
   * Limpia todos los filtros y recarga los logs.
   */
  limpiarFiltros(): void {
    this.filtros.set({});
    this.currentPage.set(0);
    this.cargarLogs();
  }

  /**
   * Cambia la página actual de la tabla.
   */
  cambiarPagina(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargarLogs();
  }

  /**
   * Expande o colapsa la fila de detalle de un log.
   */
  toggleDetalle(id: number): void {
    this.expandedRowId.update(current => current === id ? null : id);
  }

  /**
   * Formatea una fecha ISO a formato legible.
   */
  formatDate(fecha: string): string {
    if (!fecha) return '—';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-PE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  /**
   * Retorna la clase CSS de color según la acción del log.
   */
  getAccionColor(accion: string): string {
    const colores: Record<string, string> = {
      'LOGIN': 'badge-login',
      'CREAR': 'badge-crear',
      'ACTUALIZAR': 'badge-actualizar',
      'ELIMINAR': 'badge-eliminar',
      'CAMBIAR_ESTADO': 'badge-cambiar-estado',
      'CAMBIAR_CONTRASENA': 'badge-cambiar-contrasena',
      'RESET_PASSWORD': 'badge-reset-password',
      'REGISTRO': 'badge-registro'
    };
    return colores[accion] || 'badge-default';
  }

  /**
   * Retorna el ícono Bootstrap según la acción del log.
   */
  getAccionIcon(accion: string): string {
    const iconos: Record<string, string> = {
      'LOGIN': 'bi-box-arrow-in-right',
      'CREAR': 'bi-plus-circle-fill',
      'ACTUALIZAR': 'bi-pencil-fill',
      'ELIMINAR': 'bi-trash-fill',
      'CAMBIAR_ESTADO': 'bi-toggle-on',
      'CAMBIAR_CONTRASENA': 'bi-key-fill',
      'RESET_PASSWORD': 'bi-arrow-repeat',
      'REGISTRO': 'bi-person-plus-fill'
    };
    return iconos[accion] || 'bi-record-circle';
  }

  /**
   * Retorna la clase CSS de color según el rol del usuario.
   */
  getRolColor(rol: string): string {
    const colores: Record<string, string> = {
      'ADMINISTRADOR': 'badge-rol-admin',
      'MEDICO': 'badge-rol-medico',
      'ENFERMERA': 'badge-rol-enfermera',
      'RECEPCIONISTA': 'badge-rol-recepcionista',
      'CAJERO': 'badge-rol-cajero',
      'PACIENTE': 'badge-rol-paciente'
    };
    return colores[rol] || 'badge-rol-default';
  }

  /**
   * Formatea un string JSON para mostrar de forma legible.
   */
  formatJson(jsonString: string | null): string {
    if (!jsonString) return 'Sin datos';
    try {
      const parsed = JSON.parse(jsonString);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return jsonString;
    }
  }

  /**
   * Actualiza un campo específico del filtro.
   */
  updateFiltro(campo: keyof AuditFiltros, valor: any): void {
    this.filtros.update(current => ({ ...current, [campo]: valor || undefined }));
  }

  /**
   * Actualiza un campo específico del filtro de fecha formateando a cadena ISO local.
   */
  updateFiltroFecha(campo: 'fechaInicio' | 'fechaFin', date: Date | null): void {
    if (!date) {
      this.filtros.update(current => {
        const copy = { ...current };
        delete copy[campo];
        return copy;
      });
      return;
    }
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    const timeStr = campo === 'fechaInicio' ? 'T00:00:00' : 'T23:59:59';
    const isoString = `${year}-${month}-${day}${timeStr}`;
    
    this.filtros.update(current => ({ ...current, [campo]: isoString }));
  }

  /**
   * Convierte un string de fecha (ej. "2026-06-22T00:00:00") a un objeto Date para el Datepicker.
   */
  parseDate(dateStr: string | undefined): Date | null {
    if (!dateStr) return null;
    return new Date(dateStr);
  }

  /**
   * Exporta los registros de auditoría a PDF usando jsPDF y jspdf-autotable.
   */
  exportarPDF(): void {
    this.snackBar.open('Descargando datos y generando reporte PDF...', 'Cerrar', { duration: 3000 });
    
    this.auditoriaService.obtenerLogsSinActualizar(this.filtros(), 0, 1000).subscribe({
      next: (response) => {
        const logsData = response.content;
        if (!logsData || logsData.length === 0) {
          this.snackBar.open('No hay registros de auditoría que exportar.', 'Cerrar', { duration: 3000 });
          return;
        }

        const doc = new jsPDF('l', 'mm', 'a4'); // Paisaje (Landscape) para más espacio horizontal
        const pageW = doc.internal.pageSize.getWidth();
        const pageH = doc.internal.pageSize.getHeight();

        // ── ENCABEZADO PREMIUM ───────────────────────────────────────────────
        doc.setFillColor(13, 110, 253); // Azul ClínicaBienestar
        doc.rect(0, 0, pageW, 40, 'F');

        doc.setFontSize(22);
        doc.setTextColor(255, 255, 255);
        doc.setFont('helvetica', 'bold');
        doc.text('CLÍNICA BIENESTAR', 20, 18);

        doc.setFontSize(14);
        doc.setFont('helvetica', 'normal');
        doc.text('REPORTE DE AUDITORÍA Y SEGURIDAD DEL SISTEMA', 20, 28);

        // Información de generación (a la derecha)
        doc.setFontSize(10);
        doc.setTextColor(230, 240, 255);
        const fechaGen = `Generado el: ${new Date().toLocaleString('es-PE')}`;
        doc.text(fechaGen, pageW - 20, 18, { align: 'right' });
        doc.text('Estado: Registro Inmutable', pageW - 20, 28, { align: 'right' });

        // ── RESUMEN DE FILTROS ───────────────────────────────────────────────
        doc.setFontSize(10);
        doc.setTextColor(50, 50, 50);
        doc.setFont('helvetica', 'bold');
        doc.text('Filtros Activos:', 20, 50);

        doc.setFont('helvetica', 'normal');
        let txtFiltros: string[] = [];
        const f = this.filtros();
        if (f.rol) txtFiltros.push(`Rol: ${f.rol}`);
        if (f.accion) txtFiltros.push(`Acción: ${f.accion}`);
        if (f.entidad) txtFiltros.push(`Módulo: ${f.entidad}`);
        if (f.busqueda) txtFiltros.push(`Búsqueda: "${f.busqueda}"`);
        if (f.fechaInicio) txtFiltros.push(`Desde: ${this.formatDate(f.fechaInicio)}`);
        if (f.fechaFin) txtFiltros.push(`Hasta: ${this.formatDate(f.fechaFin)}`);

        if (txtFiltros.length === 0) {
          doc.text('Ninguno (Mostrando todos los registros)', 48, 50);
        } else {
          doc.text(txtFiltros.join(' | '), 48, 50);
        }

        // Línea divisoria
        doc.setDrawColor(220, 220, 220);
        doc.line(20, 55, pageW - 20, 55);

        // ── TABLA DE LOGS DE AUDITORÍA ───────────────────────────────────────
        const tableData = logsData.map(log => [
          this.formatDate(log.fecha ? log.fecha.toString() : ''),
          `${log.usuarioNombre || 'Anónimo'}\n(${log.usuarioEmail || 'N/A'})`,
          log.rol || 'SISTEMA',
          log.accion || 'N/A',
          `${log.entidad || 'N/A'}\n(ID: ${log.entidadId || '-'})`,
          log.descripcion || 'Sin descripción',
          log.ipAddress || 'N/A'
        ]);

        autoTable(doc, {
          startY: 60,
          margin: { left: 20, right: 20, bottom: 20 },
          head: [['Fecha / Hora', 'Usuario / Email', 'Rol', 'Acción', 'Módulo / ID', 'Descripción de la Acción', 'Dirección IP']],
          body: tableData,
          theme: 'striped',
          styles: {
            fontSize: 9,
            cellPadding: 3,
            valign: 'middle',
            overflow: 'linebreak'
          },
          columnStyles: {
            0: { cellWidth: 35 }, // Fecha
            1: { cellWidth: 45 }, // Usuario
            2: { cellWidth: 28 }, // Rol
            3: { cellWidth: 25 }, // Acción
            4: { cellWidth: 32 }, // Módulo / ID
            5: { cellWidth: 65 }, // Descripción
            6: { cellWidth: 26 }  // IP
          },
          headStyles: {
            fillColor: [13, 110, 253],
            textColor: [255, 255, 255],
            fontStyle: 'bold',
            halign: 'left'
          },
          didDrawPage: (data) => {
            // Pie de página
            doc.setFontSize(8);
            doc.setTextColor(150, 150, 150);
            const strPage = `Página ${data.pageNumber} de ${doc.internal.pages.length - 1}`;
            doc.text(strPage, pageW - 20, pageH - 10, { align: 'right' });
            doc.text('© Clínica Bienestar - Reporte Oficial de Auditoría General', 20, pageH - 10);
          }
        });

        doc.save(`Reporte_Auditoria_${new Date().getTime()}.pdf`);
        this.snackBar.open('Reporte PDF descargado con éxito.', 'Cerrar', { duration: 3000 });
      },
      error: (err) => {
        console.error('Error al generar el reporte PDF:', err);
        this.snackBar.open('Error al descargar los logs para el PDF.', 'Cerrar', { duration: 3000 });
      }
    });
  }
}
