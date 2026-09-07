import { Component, OnInit, Signal, computed, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MedicamentoService } from '../../../../../services/medicamento.service';
import { Medicamento, LoteMedicamento } from '../../../../../core/models/medicamento';
import { FormularioLote } from '../../../componentes-panel/formulario-lote/formulario-lote';

interface GrupoLote extends Medicamento {
  medicamentoId: any;
}

@Component({
  selector: 'app-gestion-lote',
  standalone: true,
  imports: [
    CommonModule, FormsModule, DatePipe, MatIconModule, MatButtonModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatTooltipModule
  ],
  templateUrl: './gestion-lote.html',
  styleUrls: ['./gestion-lote.css']
})
export class GestionLote implements OnInit {

  busqueda = '';
  filtroEstado = 'todos';
  gruposAbiertos = new Set<any>();

  private readonly DIAS_WARN   = 60;
  private readonly DIAS_DANGER = 15;

  lotesFiltrados: Signal<GrupoLote[]>;

  constructor(
    private medService: MedicamentoService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.lotesFiltrados = computed(() => {
      const meds = this.medService.medicamentos();
      return meds
        .map(m => ({ ...m, medicamentoId: m.id }))
        .filter(m => {
          const txt = this.busqueda.toLowerCase();
          const matchNombre = !txt ||
            m.nombre.toLowerCase().includes(txt) ||
            m.codigo.toLowerCase().includes(txt);

          if (!matchNombre) return false;
          if (this.filtroEstado === 'todos') return true;

          const lotes = m.lotes ?? [];
          if (this.filtroEstado === 'vencido')
            return lotes.some(l => this.getDiasRestantes(l.fechaVencimiento) < 0);
          if (this.filtroEstado === 'por-vencer')
            return lotes.some(l => {
              const d = this.getDiasRestantes(l.fechaVencimiento);
              return d >= 0 && d <= this.DIAS_WARN;
            });
          if (this.filtroEstado === 'vigente')
            return lotes.some(l => this.getDiasRestantes(l.fechaVencimiento) > this.DIAS_WARN);
          return true;
        }) as GrupoLote[];
    });
  }

  ngOnInit(): void {
    this.medService.cargarMedicamentos();
    // Abrir todos los grupos por defecto
    this.medService.medicamentos().forEach(m => this.gruposAbiertos.add(m.id));
  }

  toggleGrupo(id: any): void {
    if (this.gruposAbiertos.has(id)) this.gruposAbiertos.delete(id);
    else this.gruposAbiertos.add(id);
  }

  getDiasRestantes(fecha: string | Date): number {
    const hoy = new Date();
    const fv  = new Date(fecha);
    return Math.floor((fv.getTime() - hoy.getTime()) / 86_400_000);
  }

  getLoteFila(lote: LoteMedicamento): string {
    const d = this.getDiasRestantes(lote.fechaVencimiento);
    if (d < 0 || d <= this.DIAS_DANGER) return 'row-danger';
    if (d <= this.DIAS_WARN)            return 'row-warn';
    return 'row-ok';
  }

  getLoteEstadoTexto(lote: LoteMedicamento): string {
    const d = this.getDiasRestantes(lote.fechaVencimiento);
    if (d < 0)              return 'VENCIDO';
    if (d <= this.DIAS_DANGER) return 'VENCE PRONTO';
    if (d <= this.DIAS_WARN)   return 'PRÓXIMO';
    return 'VIGENTE';
  }

  getLoteEstadoClase(grupo: GrupoLote): string {
    const lotes = grupo.lotes ?? [];
    const minDias = Math.min(...lotes.map(l => this.getDiasRestantes(l.fechaVencimiento)));
    if (isFinite(minDias) && (minDias < 0 || minDias <= this.DIAS_DANGER)) return 'badge-danger';
    if (isFinite(minDias) && minDias <= this.DIAS_WARN) return 'badge-warn';
    return 'badge-ok';
  }

  getLoteEstadoLabel(grupo: GrupoLote): string {
    const lotes = grupo.lotes ?? [];
    const minDias = Math.min(...lotes.map(l => this.getDiasRestantes(l.fechaVencimiento)));
    if (isFinite(minDias) && minDias < 0) return 'Vencido';
    if (isFinite(minDias) && minDias <= this.DIAS_DANGER) return 'Vence pronto';
    if (isFinite(minDias) && minDias <= this.DIAS_WARN)   return 'Próximo a vencer';
    return lotes.length === 0 ? 'Sin lotes' : 'Vigente';
  }

  onIngresarLote(med: GrupoLote | Medicamento): void {
    const dialogRef = this.dialog.open(FormularioLote, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { medicamento: med }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.medService.agregarLote(med.id, resultado).subscribe({
          next: () => {
            this.snackBar.open('Lote ingresado con éxito', 'X', { duration: 3000 });
            this.medService.cargarMedicamentos();
          },
          error: () => this.snackBar.open('Error al ingresar lote', 'X', { duration: 3000 })
        });
      }
    });
  }
}