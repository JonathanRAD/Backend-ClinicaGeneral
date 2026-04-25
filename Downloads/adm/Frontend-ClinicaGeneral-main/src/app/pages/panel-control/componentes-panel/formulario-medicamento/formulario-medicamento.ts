import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { Medicamento } from '../../../../core/models/medicamento';

@Component({
  selector: 'app-formulario-medicamento',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule
  ],
  templateUrl: './formulario-medicamento.html',
  styleUrls: ['./formulario-medicamento.css']
})
export class FormularioMedicamento implements OnInit {
  medForm!: FormGroup;
  isEditing = false;

  formasFarmaceuticas = ['Pastilla / Comprimido', 'Cápsula', 'Jarabe', 'Suspensión', 'Inyectable', 'Crema / Pomada', 'Gotas', 'Otro'];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioMedicamento>,
    @Inject(MAT_DIALOG_DATA) public data: { isEditing: boolean, medicamento?: Medicamento }
  ) {
    this.isEditing = data.isEditing;
  }

  ngOnInit(): void {
    const med = this.data.medicamento || {} as Partial<Medicamento>;

    this.medForm = this.fb.group({
      codigo: [{ value: med.codigo || '', disabled: this.isEditing }, Validators.required],
      nombre: [med.nombre || '', Validators.required],
      formaFarmaceutica: [med.formaFarmaceutica || '', Validators.required],
      concentracion: [med.concentracion || '', Validators.required],
      precioUnitario: [med.precioUnitario || 0, [Validators.required, Validators.min(0)]],
      descripcion: [med.descripcion || ''],
      estado: [med.estado || 'ACTIVO', Validators.required]
    });
  }

  guardar() {
    if (this.medForm.valid) {
      const result: Medicamento = this.medForm.getRawValue();
      this.dialogRef.close(result);
    } else {
      this.medForm.markAllAsTouched();
    }
  }

  cancelar() {
    this.dialogRef.close();
  }
}
