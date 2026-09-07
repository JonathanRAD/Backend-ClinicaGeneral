import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';

export interface PrevisualizacionData {
  titulo: string;
  headers: string[];
  rows: any[][];
}

@Component({
  selector: 'app-dialogo-previsualizacion',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule
  ],
  templateUrl: './dialogo-previsualizacion.html',
  styleUrls: ['./dialogo-previsualizacion.css']
})
export class DialogoPrevisualizacionComponent {
  constructor(
    public dialogRef: MatDialogRef<DialogoPrevisualizacionComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PrevisualizacionData
  ) {}

  cerrar(): void {
    this.dialogRef.close();
  }
}