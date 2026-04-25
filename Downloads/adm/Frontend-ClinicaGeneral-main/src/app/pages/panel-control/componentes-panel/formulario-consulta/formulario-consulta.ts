import { Component, Inject, OnInit, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { Medico } from '../../../../core/models/medico';
import { MedicoService } from '../../../../services/medico';

@Component({
  selector: 'app-formulario-consulta',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule
  ],
  templateUrl: './formulario-consulta.html',
  styleUrls: ['./formulario-consulta.css']
})
export class FormularioConsultaComponent implements OnInit {
  consultaForm: FormGroup;
  medicos: Signal<Medico[]>;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioConsultaComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private medicoService: MedicoService 
  ) {
    this.medicos = this.medicoService.medicos; 
    this.consultaForm = this.fb.group({
      motivo: ['', [Validators.required, Validators.maxLength(500)]],
      diagnostico: ['', [Validators.required, Validators.maxLength(1000)]],
      tratamiento: ['', [Validators.required, Validators.maxLength(1000)]],
      medicoId: ['', Validators.required]
    });
  }

  ngOnInit(): void {
  }

  guardar(): void {
    if (this.consultaForm.invalid) {
      this.consultaForm.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.consultaForm.value);
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}