import { Component, OnInit, Inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-formulario-paciente',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule
  ],
  templateUrl: './formulario-paciente.html',
  styleUrls: ['./formulario-paciente.css'],
  encapsulation: ViewEncapsulation.None 
})
export class FormularioPaciente implements OnInit {
  pacienteForm: FormGroup;
  esModoEdicion: boolean;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioPaciente>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.esModoEdicion = this.data.esModoEdicion;
    this.pacienteForm = this.fb.group({
      dni: ['', [Validators.required, Validators.pattern('^[0-9]{8}$')]],
      nombres: ['', Validators.required],
      apellidos: ['', Validators.required],
      telefono: ['', [Validators.required, Validators.pattern('^[0-9]{9}$')]],
      fechaNacimiento: ['', Validators.required],
      peso: [null, [Validators.min(1), Validators.max(500)]], 
      altura: [null, [Validators.min(0.5), Validators.max(2.5)]], 
      ritmoCardiaco: [null, [Validators.min(30), Validators.max(220)]] 
    });
  }

  ngOnInit(): void {
    if (this.esModoEdicion && this.data.paciente) {
      this.pacienteForm.patchValue(this.data.paciente);
    }
  }

  guardar() {
    if (this.pacienteForm.invalid) {
      this.pacienteForm.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.pacienteForm.value);
  }

  cancelar() {
    this.dialogRef.close();
  }
}
