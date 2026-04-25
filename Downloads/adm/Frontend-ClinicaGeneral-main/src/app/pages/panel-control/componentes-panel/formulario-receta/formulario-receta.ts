import { Component, Inject, OnInit, signal, effect, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MedicamentoService } from '../../../../services/medicamento.service';
import { Medicamento } from '../../../../core/models/medicamento';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { map, startWith } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-formulario-receta',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, 
    MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule, MatAutocompleteModule
  ],
  templateUrl: './formulario-receta.html',
  styleUrls: ['./formulario-receta.css']
})
export class FormularioRecetaComponent implements OnInit {

  recetaForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioRecetaComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public medicamentoService: MedicamentoService
  ) {
    this.recetaForm = this.fb.group({
      indicacionesGenerales: [''],
      detalles: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.medicamentoService.cargarMedicamentos();
    this.agregarMedicamento(); // Agregar al menos una fila vacía por defecto
  }

  get detalles(): FormArray {
    return this.recetaForm.get('detalles') as FormArray;
  }

  agregarMedicamento(): void {
    const detalleForm = this.fb.group({
      medicamentoId: [null, Validators.required],
      cantidadSolicitada: [1, [Validators.required, Validators.min(1)]],
      dosis: ['', Validators.required],
      frecuencia: ['', Validators.required],
      duracion: ['', Validators.required]
    });
    this.detalles.push(detalleForm);
  }

  removerMedicamento(index: number): void {
    this.detalles.removeAt(index);
  }

  getStock(medicamentoId: number | null): number {
    if(!medicamentoId) return 0;
    const med = this.medicamentoService.medicamentos().find(m => m.id === medicamentoId);
    return med ? (med.stockTotal || 0) : 0;
  }
  
  getNombreMedicamento(medicamentoId: number | null): string {
    if(!medicamentoId) return '';
    const med = this.medicamentoService.medicamentos().find(m => m.id === medicamentoId);
    return med ? med.nombre : '';
  }

  guardar(): void {
    if (this.recetaForm.valid) {
      if(this.detalles.length === 0) {
          // El usuario puede crear recetas sin medicamentos si solo da indicaciones, u obligamos a tener uno.
          // dejaremos que mande detalles en null.
      }
      this.dialogRef.close(this.recetaForm.value);
    }
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}
