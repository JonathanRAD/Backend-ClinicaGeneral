// ── formulario-proveedor.ts ──────────────────────────────────────────────
import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { Proveedor } from '../../../../services/proveedor.service';

@Component({
  selector: 'app-formulario-proveedor',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatButtonModule, MatFormFieldModule, MatInputModule, MatIconModule
  ],
  template: `
    <div>
      <h2 mat-dialog-title class="d-flex align-items-center gap-2">
        <mat-icon color="primary">local_shipping</mat-icon>
        {{ data.proveedor ? 'Editar Proveedor' : 'Nuevo Proveedor' }}
      </h2>
      <mat-dialog-content class="px-4 pt-3">
        <form [formGroup]="form" class="row g-3">
          <div class="col-12">
            <mat-form-field appearance="outline" class="w-100">
              <mat-label>Nombre de la empresa *</mat-label>
              <input matInput formControlName="nombre" placeholder="Ej: FARMINDUSTRIA S.A.">
              <mat-error *ngIf="form.get('nombre')?.hasError('required')">Requerido</mat-error>
            </mat-form-field>
          </div>
          <div class="col-md-6">
            <mat-form-field appearance="outline" class="w-100">
              <mat-label>RUC</mat-label>
              <input matInput formControlName="ruc" placeholder="20123456789">
            </mat-form-field>
          </div>
          <div class="col-md-6">
            <mat-form-field appearance="outline" class="w-100">
              <mat-label>Teléfono</mat-label>
              <input matInput formControlName="telefono" placeholder="+51 1 000 0000">
            </mat-form-field>
          </div>
          <div class="col-12">
            <mat-form-field appearance="outline" class="w-100">
              <mat-label>Correo electrónico</mat-label>
              <input matInput formControlName="email" type="email">
            </mat-form-field>
          </div>
          <div class="col-12">
            <mat-form-field appearance="outline" class="w-100">
              <mat-label>Dirección</mat-label>
              <input matInput formControlName="direccion">
            </mat-form-field>
          </div>
          <div class="col-12">
            <mat-form-field appearance="outline" class="w-100">
              <mat-label>Persona de contacto</mat-label>
              <input matInput formControlName="contacto">
            </mat-form-field>
          </div>
        </form>
      </mat-dialog-content>
      <mat-dialog-actions align="end" class="pb-3 px-4">
        <button mat-stroked-button (click)="cancelar()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="guardar()" [disabled]="form.invalid">
          <mat-icon>save</mat-icon>
          {{ data.proveedor ? 'Guardar Cambios' : 'Registrar Proveedor' }}
        </button>
      </mat-dialog-actions>
    </div>
  `
})
export class FormularioProveedorComponent implements OnInit {
  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FormularioProveedorComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { proveedor: Proveedor | null }
  ) {}

  ngOnInit(): void {
    const p = this.data.proveedor;
    this.form = this.fb.group({
      nombre:    [p?.nombre    || '', Validators.required],
      ruc:       [p?.ruc       || ''],
      telefono:  [p?.telefono  || ''],
      email:     [p?.email     || '', Validators.email],
      direccion: [p?.direccion || ''],
      contacto:  [p?.contacto  || '']
    });
  }

  guardar(): void {
    if (this.form.valid) this.dialogRef.close(this.form.value);
    else this.form.markAllAsTouched();
  }

  cancelar(): void { this.dialogRef.close(); }
}