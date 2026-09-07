import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { LoteMedicamento, Medicamento } from '../../../../core/models/medicamento';

@Component({
  selector: 'app-formulario-lote',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatButtonModule, MatFormFieldModule, MatInputModule, MatIconModule
  ],
  templateUrl: './formulario-lote.html',
  styleUrls: ['../formulario-medicamento/formulario-medicamento.css'] // Reusaremos los estilos oscuros
})
export class FormularioLote implements OnInit {
  loteForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioLote>,
    @Inject(MAT_DIALOG_DATA) public data: { medicamento: Medicamento }
  ) {}

  ngOnInit(): void {
    this.loteForm = this.fb.group({
      numeroLote: ['', Validators.required],
      stock: [1, [Validators.required, Validators.min(1)]],
      fechaVencimiento: ['', Validators.required]
    });
  }

  guardar() {
    if (this.loteForm.valid) {
      const lote: LoteMedicamento = this.loteForm.value;
      this.dialogRef.close(lote);
    } else {
      this.loteForm.markAllAsTouched();
    }
  }

  cancelar() {
    this.dialogRef.close();
  }
}
