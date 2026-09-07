
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-dialogo-confirmacion',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './dialogo-confirmacion.html',
  styleUrls: ['./dialogo-confirmacion.css']
})
export class DialogoConfirmacion {
  constructor(
    public dialogRef: MatDialogRef<DialogoConfirmacion>,
    @Inject(MAT_DIALOG_DATA) public data: {
      titulo: string;
      mensaje: string;
      textoConfirmar?: string;
      textoCancelar?: string;
    }
  ) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}