import { Component, Input, Output, EventEmitter, ViewChild, AfterViewInit, OnChanges, SimpleChanges, ContentChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { DatePipe } from '@angular/common';
import { Patient } from '../../core/models/patient';
import { RouterModule } from '@angular/router';
import { AutenticacionService } from '../../services/autenticacion';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';



export interface ColumnConfig {
  name: string; 
  header: string; 
  isDate?: boolean;
}

@Component({
  selector: 'app-tabla-generica',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    RouterModule,
    MatPaginatorModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatFormFieldModule, 
    MatInputModule,
    MatCardModule,
    DatePipe  
  ],
  templateUrl: './tabla-generica.html',
  styleUrls: ['./tabla-generica.css']
})
export class TablaGenerica implements AfterViewInit, OnChanges {
  @Input() data: any[] = [];
  @Input() columnConfig: ColumnConfig[] = [];
  @Input() title: string = 'Lista';
  @Input() addButtonText?: string;
  @Input() nameProperties: [string, string] = ['', ''];
  @Input() idProperty: string = 'id';
  @Input() showClinicalHistoryButton: boolean = false;
  @Input() permissionManage?: string;
  @Input() permissionViewHistory?: string;
  @Input() pdfTitle?: string;


  @Output() onEdit = new EventEmitter<any>();
  @Output() onDelete = new EventEmitter<any>();
  @Output() onAdd = new EventEmitter<void>(); 
  @Output() onRowClicked = new EventEmitter<any>();
  dataSource = new MatTableDataSource<any>();
  displayedColumns: string[] = [];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ContentChild('accionesTemplate') accionesTemplate?: TemplateRef<any>;

  constructor(
    public authService: AutenticacionService // <-- 2. Inyéctalo y hazlo público
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.dataSource.data = this.data;
    }
    if (changes['columnConfig'] || changes['nameProperties']) {
      const hasInitialsAvatar = this.nameProperties && this.nameProperties[0] && this.nameProperties[1];
      let columnsToDisplay = this.columnConfig.map(c => c.name);
      
      if (hasInitialsAvatar) {
        this.displayedColumns = ['avatar', ...columnsToDisplay, 'acciones'];
      } else {
        this.displayedColumns = [...columnsToDisplay, 'acciones'];
      }
    }
  }

  getInitials(element: any): string {
    const firstName = element[this.nameProperties[0]] || '';
    const lastName = element[this.nameProperties[1]] || '';
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }
  getAvatarColor(element: any): string {
    const colors = ['#3498db', '#2ecc71', '#e74c3c', '#f1c40f', '#9b59b6', '#1abc9c'];
    const id = element[this.idProperty] || 'default';
    const index = id.toString().charCodeAt(0) % colors.length;
    return colors[index];
  }
onVerHistoria(paciente: Patient) {
    console.log('Navegando a la historia del paciente:', paciente.id);
  }
  

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  resolveNestedPath(obj: any, path: string): any {
    return path.split('.').reduce((p, c) => (p && p[c]) ? p[c] : null, obj);
  }

  agregarNuevo() {
    this.onAdd.emit();
  }

  editar(element: any) {
    this.onEdit.emit(element);
  }

  eliminar(element: any) {
    this.onDelete.emit(element);
  }

  exportarPDF(): void {
    const doc = new jsPDF({ orientation: 'landscape' });
    const titulo = this.pdfTitle || this.title;
    const fecha = new Date().toLocaleDateString('es-PE', {
      day: '2-digit', month: 'long', year: 'numeric'
    });

    // ── Encabezado ───────────────────────────────────────────────────────────
    doc.setFillColor(13, 110, 253);
    doc.rect(0, 0, doc.internal.pageSize.getWidth(), 28, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    doc.text('Clínica Bienestar', 14, 11);
    doc.setFontSize(11);
    doc.setFont('helvetica', 'normal');
    doc.text(titulo, 14, 20);
    doc.setFontSize(9);
    doc.text(`Generado: ${fecha}`, doc.internal.pageSize.getWidth() - 14, 20, { align: 'right' });

    // ── Filas y columnas ─────────────────────────────────────────────────────
    const datePipe = new DatePipe('en-US');
    const head = [this.columnConfig.map(c => c.header)];
    const body = this.dataSource.filteredData.map(row =>
      this.columnConfig.map(col => {
        const val = this.resolveNestedPath(row, col.name);
        if (col.isDate && val) {
          return datePipe.transform(val, 'dd/MM/yyyy HH:mm') ?? '-';
        }
        return val !== null && val !== undefined ? String(val) : '-';
      })
    );

    autoTable(doc, {
      head,
      body,
      startY: 32,
      styles: { fontSize: 9, cellPadding: 3 },
      headStyles: { fillColor: [13, 110, 253], textColor: 255, fontStyle: 'bold' },
      alternateRowStyles: { fillColor: [245, 247, 250] },
      tableLineColor: [220, 220, 220],
      tableLineWidth: 0.1,
    });

    // ── Pie de página ────────────────────────────────────────────────────────
    const pageCount = (doc as any).internal.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.setTextColor(150);
      doc.text(
        `Página ${i} de ${pageCount}`,
        doc.internal.pageSize.getWidth() / 2,
        doc.internal.pageSize.getHeight() - 8,
        { align: 'center' }
      );
    }

    const nombreArchivo = titulo.toLowerCase().replace(/\s+/g, '_');
    const fecha_archivo = new Date().toISOString().slice(0, 10);

    // Forzar descarga con nombre+extensión correcta (fix Chrome UUID bug)
    const blob = doc.output('blob');
    const url  = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href     = url;
    link.download = `${nombreArchivo}_${fecha_archivo}.pdf`;
    link.type     = 'application/pdf';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}