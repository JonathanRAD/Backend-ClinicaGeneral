
import { Component, OnInit, Inject, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker'; 
import { MatNativeDateModule } from '@angular/material/core'; 
import { MatIconModule } from '@angular/material/icon';

import { PacienteService } from '../../../../services/paciente';
import { MedicoService } from '../../../../services/medico';
import { Patient } from '../../../../core/models/patient';
import { Medico } from '../../../../core/models/medico';

@Component({
  selector: 'app-formulario-cita',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatSelectModule, MatDatepickerModule,
    MatNativeDateModule, MatIconModule 
  ],
  templateUrl: './formulario-cita.html',
  styleUrls: ['./formulario-cita.css']
})
export class FormularioCita implements OnInit {
  citaForm: FormGroup;
  esModoEdicion: boolean;
  pacientes: Signal<Patient[]>;
  medicos: Signal<Medico[]>;
  horasDisponibles: string[] = []; 

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioCita>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private pacienteService: PacienteService,
    private medicoService: MedicoService
  ) {
    this.esModoEdicion = this.data.esModoEdicion;
    this.pacientes = this.pacienteService.pacientes;
    this.medicos = this.medicoService.medicos;
    
    this.generarHoras();

    this.citaForm = this.fb.group({
      pacienteId: ['', Validators.required],
      medicoId: ['', Validators.required],
      fecha: [new Date(), Validators.required], 
      hora: ['', Validators.required],
      motivo: ['', [Validators.required, Validators.maxLength(200)]]
    });
  }

  ngOnInit(): void {
    if (this.esModoEdicion && this.data.cita) {
      const fechaCita = new Date(this.data.cita.fechaHora);
      this.citaForm.patchValue({
        pacienteId: this.data.cita.paciente.id,
        medicoId: this.data.cita.medico.id,
        fecha: fechaCita,
        hora: fechaCita.toTimeString().slice(0, 5),
        motivo: this.data.cita.motivo
      });
    }
  }

  private generarHoras() {
    for (let h = 8; h < 18; h++) { 
      this.horasDisponibles.push(`${h.toString().padStart(2, '0')}:00`);
      this.horasDisponibles.push(`${h.toString().padStart(2, '0')}:30`);
    }
  }

  guardar(): void {
    if (this.citaForm.invalid) {
      this.citaForm.markAllAsTouched();
      return;
    }

    const formValue = this.citaForm.value;
    const fechaSeleccionada: Date = formValue.fecha;
    const [hora, minutos] = formValue.hora.split(':');
    
    fechaSeleccionada.setHours(parseInt(hora), parseInt(minutos), 0, 0);

    const payload = {
      pacienteId: formValue.pacienteId,
      medicoId: formValue.medicoId,
      fechaHora: fechaSeleccionada.toISOString(), 
      motivo: formValue.motivo
    };

    this.dialogRef.close(payload);
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}