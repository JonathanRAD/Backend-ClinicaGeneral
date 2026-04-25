
import { Component, Inject, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-formulario-medico',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatButtonModule,
    MatDatepickerModule, MatNativeDateModule,
    MatSelectModule
  ],
  templateUrl: './formulario-medico.html',
  encapsulation: ViewEncapsulation.None
})
export class FormularioMedico implements OnInit {
  medicoForm: FormGroup;
  esModoEdicion: boolean;

  especialidadesDisponibles: string[] = [
    'Cardiología',
    'Pediatría',
    'Medicina General',
    'Dermatología',
    'Ginecología',
    'Neurología',
    'Traumatología'
  ];
  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioMedico>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.esModoEdicion = this.data.esModoEdicion;
    this.medicoForm = this.fb.group({
      nombres: ['', Validators.required],
      apellidos: ['', Validators.required],
      especialidad: ['', Validators.required],
      fechaNacimiento: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.esModoEdicion && this.data.medico) {
      this.medicoForm.patchValue(this.data.medico);
    }
  }

  guardar(): void {
    if (this.medicoForm.invalid) {
      this.medicoForm.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.medicoForm.value);
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}