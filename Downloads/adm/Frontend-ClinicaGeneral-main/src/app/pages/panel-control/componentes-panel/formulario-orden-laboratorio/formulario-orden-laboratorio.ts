
import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-formulario-orden-laboratorio',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './formulario-orden-laboratorio.html',
})
export class FormularioOrdenLaboratorioComponent {
  ordenForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioOrdenLaboratorioComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { consultaId: number }
  ) {
    this.ordenForm = this.fb.group({
      tipoExamen: ['', [Validators.required, Validators.maxLength(150)]],
      observaciones: ['', [Validators.maxLength(500)]]
    });
  }

  guardar(): void {
    if (this.ordenForm.invalid) {
      this.ordenForm.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.ordenForm.value);
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}