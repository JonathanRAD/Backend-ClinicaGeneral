import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { SeguroMedicoService } from '../../../../services/seguro-medico';

@Component({
  selector: 'app-formulario-seguro',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './formulario-seguro.html',
  styleUrls: ['./formulario-seguro.css']
})
export class FormularioSeguroComponent implements OnInit {
  seguroForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioSeguroComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { seguro: SeguroMedicoService | null }
  ) {
    this.seguroForm = this.fb.group({
      nombreAseguradora: ['', Validators.required],
      numeroPoliza: ['', Validators.required],
      cobertura: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.data.seguro) {
      this.seguroForm.patchValue(this.data.seguro);
    }
  }

  guardar(): void {
    if (this.seguroForm.invalid) {
      this.seguroForm.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.seguroForm.value);
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}