// ── gestion-producto.ts ───────────────────────────────────────────────────
import { Component, OnInit, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TablaGenerica, ColumnConfig } from '../../../../../shared/tabla-generica/tabla-generica';
import { Medicamento } from '../../../../../core/models/medicamento';
import { MedicamentoService } from '../../../../../services/medicamento.service';
import { FormularioMedicamento } from '../../../componentes-panel/formulario-medicamento/formulario-medicamento';
import { FormularioLote } from '../../../componentes-panel/formulario-lote/formulario-lote';
import { DialogoLotesComponent } from '../../../componentes-panel/dialogo-lotes/dialogo-lotes';
import { DialogoConfirmacion } from '../../../componentes-panel/dialogo-confirmacion/dialogo-confirmacion';

@Component({
  selector: 'app-gestion-producto',
  standalone: true,
  imports: [CommonModule, TablaGenerica, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './gestion-producto.html',
  styleUrls: ['./gestion-producto.css']
})
export class GestionProducto implements OnInit {

  medicamentos: Signal<Medicamento[]>;

  columnas: ColumnConfig[] = [
    { name: 'codigo', header: 'Código' },
    { name: 'nombre', header: 'Nombre / Principio Activo' },
    { name: 'formaFarmaceutica', header: 'Presentación' },
    { name: 'concentracion', header: 'Concentración' },
    { name: 'stockTotal', header: 'Stock Total' },
    { name: 'precioUnitario', header: 'Prec. Unit. (S/.)' },
    { name: 'estado', header: 'Estado' }
  ];

  constructor(
    private medService: MedicamentoService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.medicamentos = this.medService.medicamentos;
  }

  ngOnInit(): void {
    this.medService.cargarMedicamentos();
  }

  onRegistrarMedicamento() {
    const dialogRef = this.dialog.open(FormularioMedicamento, {
      width: '600px', disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { isEditing: false }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.medService.crearMedicamento(resultado).subscribe({
          next: () => this.snackBar.open('Medicamento registrado', 'X', { duration: 3000 }),
          error: () => this.snackBar.open('Error al registrar', 'X', { duration: 3000 })
        });
      }
    });
  }

  onEditMedicamento(med: Medicamento) {
    const dialogRef = this.dialog.open(FormularioMedicamento, {
      width: '600px', disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { isEditing: true, medicamento: med }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.medService.actualizarMedicamento(med.id, resultado).subscribe({
          next: () => this.snackBar.open('Actualizado con éxito', 'X', { duration: 3000 }),
          error: () => this.snackBar.open('Error al actualizar', 'X', { duration: 3000 })
        });
      }
    });
  }

  onDeleteMedicamento(med: Medicamento) {
    const dialogRef = this.dialog.open(DialogoConfirmacion, {
      width: '400px',
      data: {
        titulo: 'Eliminar Medicamento',
        mensaje: `¿Eliminar ${med.nombre}? Esto podría afectar el historial si tiene lotes.`
      }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.medService.eliminarMedicamento(med.id).subscribe({
          next: () => this.snackBar.open('Eliminado', 'X', { duration: 3000 }),
          error: () => this.snackBar.open('Error al eliminar. Verifique dependencias.', 'X', { duration: 3000 })
        });
      }
    });
  }

  onIngresarLote(med: Medicamento) {
    const dialogRef = this.dialog.open(FormularioLote, {
      width: '500px', disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { medicamento: med }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.medService.agregarLote(med.id, resultado).subscribe({
          next: () => {
            this.snackBar.open('Lote ingresado, stock incrementado', 'X', { duration: 3000 });
            this.medService.cargarMedicamentos();
          },
          error: () => this.snackBar.open('Error al ingresar lote', 'X', { duration: 3000 })
        });
      }
    });
  }

  onVerLotes(med: Medicamento) {
    this.dialog.open(DialogoLotesComponent, {
      width: '600px',
      panelClass: 'custom-dialog-container',
      data: { medicamento: med }
    });
  }
}