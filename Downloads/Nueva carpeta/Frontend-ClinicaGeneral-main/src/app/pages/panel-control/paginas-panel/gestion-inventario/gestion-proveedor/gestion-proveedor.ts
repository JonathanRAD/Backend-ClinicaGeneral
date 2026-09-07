// ── gestion-proveedor.ts ──────────────────────────────────────────────────
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProveedorService, Proveedor } from '../../../../../services/proveedor.service';
import { FormularioProveedorComponent } from '../../../componentes-panel/formulario-proveedor/formulario-proveedor';
import { DialogoConfirmacion } from '../../../componentes-panel/dialogo-confirmacion/dialogo-confirmacion';

@Component({
  selector: 'app-gestion-proveedor',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './gestion-proveedor.html',
  styleUrls: ['./gestion-proveedor.css']
})
export class GestionProveedor implements OnInit {

  proveedores: any;

  constructor(
    private proveedorService: ProveedorService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.proveedores = this.proveedorService.proveedores;
  }

  ngOnInit(): void {
    this.proveedorService.cargarProveedores();
  }

  getColor(nombre: string): string {
    const colors = [
      'linear-gradient(135deg,#1565c0,#1e88e5)',
      'linear-gradient(135deg,#2e7d32,#43a047)',
      'linear-gradient(135deg,#4527a0,#7b1fa2)',
      'linear-gradient(135deg,#e65100,#fb8c00)',
      'linear-gradient(135deg,#00695c,#00897b)',
      'linear-gradient(135deg,#c62828,#e53935)'
    ];
    let hash = 0;
    for (let i = 0; i < nombre.length; i++) hash += nombre.charCodeAt(i);
    return colors[hash % colors.length];
  }

  onAgregar(): void {
    const dialogRef = this.dialog.open(FormularioProveedorComponent, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { proveedor: null }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.proveedorService.crearProveedor(resultado).subscribe({
          next: () => this.snackBar.open('Proveedor registrado', 'X', { duration: 3000 }),
          error: () => this.snackBar.open('Error al registrar', 'X', { duration: 3000 })
        });
      }
    });
  }

  onEditar(proveedor: Proveedor): void {
    const dialogRef = this.dialog.open(FormularioProveedorComponent, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { proveedor }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.proveedorService.actualizarProveedor(proveedor.id!, resultado).subscribe({
          next: () => this.snackBar.open('Proveedor actualizado', 'X', { duration: 3000 }),
          error: () => this.snackBar.open('Error al actualizar', 'X', { duration: 3000 })
        });
      }
    });
  }

  onEliminar(proveedor: Proveedor): void {
    const dialogRef = this.dialog.open(DialogoConfirmacion, {
      width: '450px',
      data: {
        titulo: 'Eliminar Proveedor',
        mensaje: `¿Eliminar al proveedor "${proveedor.nombre}"?`
      }
    });
    dialogRef.afterClosed().subscribe(confirmado => {
      if (confirmado) {
        this.proveedorService.eliminarProveedor(proveedor.id!).subscribe({
          next: () => this.snackBar.open('Proveedor eliminado', 'X', { duration: 3000 }),
          error: () => this.snackBar.open('Error al eliminar', 'X', { duration: 3000 })
        });
      }
    });
  }
}