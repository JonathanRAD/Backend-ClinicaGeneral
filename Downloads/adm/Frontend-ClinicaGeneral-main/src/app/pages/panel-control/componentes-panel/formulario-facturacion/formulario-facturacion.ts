
import { Component, Inject, OnInit, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { CitaService } from '../../../../services/cita';
import { Cita } from '../../../../core/models/cita';

@Component({
  selector: 'app-formulario-facturacion',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatSelectModule
  ],
  templateUrl: './formulario-facturacion.html',
  styleUrls: ['./formulario-facturacion.css']
})
export class FormularioFacturacion implements OnInit {
  facturaForm: FormGroup;
  esModoEdicion: boolean;
  citas: Signal<Cita[]>;
  estadosFactura = ['pagada', 'pendiente', 'anulada'];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioFacturacion>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private citaService: CitaService
  ) {
    this.esModoEdicion = this.data.esModoEdicion;
    this.citas = this.citaService.citas;

    this.facturaForm = this.fb.group({
      citaId: ['', Validators.required],
      monto: ['', [Validators.required, Validators.pattern('^[0-9]+(\\.[0-9]{1,2})?$')]],
      montoPagado: [0, [Validators.required, Validators.min(0)]],
      estado: ['pendiente', Validators.required]
      
    });
  }

  ngOnInit(): void {
    if (this.esModoEdicion && this.data.factura) {
      this.facturaForm.patchValue({
        citaId: this.data.factura.cita.id,
        monto: this.data.factura.monto,
        estado: this.data.factura.estado,
        montoPagado: this.data.factura.montoPagado
      });
    }
  }

  guardar(): void {
    if (this.facturaForm.invalid) {
      this.facturaForm.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.facturaForm.value);
  }

  cancelar(): void {
    this.dialogRef.close();
  }
}