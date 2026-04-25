import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class Notificacion {

  constructor(private snackBar: MatSnackBar) { }

  /**
   * Muestra una notificación (snackbar) en la pantalla.
   * @param mensaje El texto a mostrar.
   * @param tipo El tipo de notificación para darle un estilo visual.
   */
  mostrar(mensaje: string, tipo: 'exito' | 'error' = 'exito'): void {
    this.snackBar.open(mensaje, 'Cerrar', {
      duration: 3000,
      panelClass: tipo === 'exito' ? 'notificacion-exito' : 'notificacion-error',
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
  }
}