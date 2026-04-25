import { Component, Inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Medicamento } from '../../../../core/models/medicamento';

@Component({
  selector: 'app-dialogo-lotes',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, DatePipe],
  templateUrl: './dialogo-lotes.html',
  styleUrls: ['./dialogo-lotes.css']
})
export class DialogoLotesComponent {
  
  constructor(
    public dialogRef: MatDialogRef<DialogoLotesComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { medicamento: Medicamento }
  ) {}

  cerrar() {
    this.dialogRef.close();
  }
}
