import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MedicamentoService } from '../../../../services/medicamento.service';
import { FormsModule } from '@angular/forms';

export interface DespachoDialogData {
  pacienteNombre: string;
  pacienteDni: string;
  recetas: any[];
}

@Component({
  selector: 'app-dialogo-despacho',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, 
            MatCheckboxModule, MatDividerModule, FormsModule],
  templateUrl: './dialogo-despacho.html',
  styleUrls: ['./dialogo-despacho.css']
})
export class DialogoDespachoComponent implements OnInit {

  // Mapa: medicamentoId -> { cantidad: number, nombre: string, seleccionado: boolean }
  itemsDespacho = signal<{medicamentoId: number; nombre: string; cantidad: number; seleccionado: boolean}[]>([]);
  despachando = signal(false);

  constructor(
    public dialogRef: MatDialogRef<DialogoDespachoComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DespachoDialogData,
    private medicamentoService: MedicamentoService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.construirListaItems();
  }

  construirListaItems(): void {
    const mapaItems = new Map<number, {medicamentoId: number; nombre: string; cantidad: number; seleccionado: boolean}>();

    for (const receta of this.data.recetas) {
      for (const det of (receta.detalles || [])) {
        const medId = det.medicamento?.id || det.medicamentoId;
        const nombre = det.medicamento?.nombre || ('Medicamento ID: ' + medId);
        const cantidad = det.cantidadSolicitada || 1;

        if (mapaItems.has(medId)) {
          const existing = mapaItems.get(medId)!;
          existing.cantidad += cantidad;
        } else {
          mapaItems.set(medId, { medicamentoId: medId, nombre, cantidad, seleccionado: true });
        }
      }
    }
    this.itemsDespacho.set(Array.from(mapaItems.values()));
  }

  toggleSeleccion(item: any): void {
    item.seleccionado = !item.seleccionado;
    this.itemsDespacho.set([...this.itemsDespacho()]);
  }

  getStockActual(medicamentoId: number): number {
    const med = this.medicamentoService.medicamentos().find(m => m.id === medicamentoId);
    return med?.stockTotal || 0;
  }

  hayItemsSeleccionados(): boolean {
    return this.itemsDespacho().some(i => i.seleccionado);
  }

  confirmarDespacho(): void {
    const itemsSeleccionados = this.itemsDespacho()
      .filter(i => i.seleccionado)
      .map(i => ({ medicamentoId: i.medicamentoId, cantidad: i.cantidad }));

    if (itemsSeleccionados.length === 0) return;

    this.despachando.set(true);
    this.medicamentoService.despacharMedicamentos(itemsSeleccionados).subscribe({
      next: () => {
        this.snackBar.open('✅ Medicamentos despachados y stock actualizado', 'Cerrar', { duration: 4000 });
        this.dialogRef.close({ despachado: true });
      },
      error: (err: any) => {
        const msg = err?.error?.message || err?.message || 'Error al despachar';
        this.snackBar.open(`❌ ${msg}`, 'Cerrar', { duration: 5000 });
        this.despachando.set(false);
      }
    });
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}
