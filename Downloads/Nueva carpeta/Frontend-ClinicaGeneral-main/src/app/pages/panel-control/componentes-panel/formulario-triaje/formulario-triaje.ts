import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { Cita } from '../../../../core/models/cita';
import { Triaje } from '../../../../core/models/triaje';

@Component({
  selector: 'app-formulario-triaje',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatButtonModule, MatFormFieldModule, MatInputModule, MatIconModule, MatDividerModule
  ],
  templateUrl: './formulario-triaje.html',
  styleUrls: ['./formulario-triaje.css']
})
export class FormularioTriaje implements OnInit {
  triajeForm!: FormGroup;
  pacienteInfo: any;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioTriaje>,
    @Inject(MAT_DIALOG_DATA) public data: { cita: Cita }
  ) { }

  ngOnInit() {
    this.pacienteInfo = this.data.cita.paciente;
    this.triajeForm = this.fb.group({
      peso: [this.pacienteInfo.peso || '', [Validators.required, Validators.min(1)]],
      altura: [this.pacienteInfo.altura || '', [Validators.required, Validators.min(1)]],
      temperatura: ['', [Validators.required, Validators.min(30), Validators.max(45)]],
      presionArterial: ['', [Validators.required, Validators.pattern('^\\d{2,3}/\\d{2,3}$')]],
      ritmoCardiaco: [this.pacienteInfo.ritmoCardiaco || '', [Validators.required, Validators.min(0)]],
      saturacionOxigeno: ['', [Validators.required, Validators.min(0), Validators.max(100)]],
      nivelAzucar: ['', [Validators.min(0)]],
      motivoConsulta: [this.data.cita.motivo, Validators.required],
      notasOpcionales: ['']
    });
  }

  guardar() {
    if (this.triajeForm.valid) {
      const triajeData: Triaje = {
        citaId: this.data.cita.id,
        fechaRegistro: new Date(),
        ...this.triajeForm.value
      };
      this.dialogRef.close(triajeData);
    } else {
      this.triajeForm.markAllAsTouched();
    }
  }

  cancelar() {
    this.dialogRef.close();
  }
}
