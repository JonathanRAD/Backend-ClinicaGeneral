import { Component, OnInit, Inject, ChangeDetectorRef  } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, TitleCasePipe } from '@angular/common';

import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { AutenticacionService } from '../../../../services/autenticacion';
import { Notificacion } from '../../../../services/notificacion';
import { CreateUserPayload, UserProfile } from '../../../../core/models/usuario';
import { Rol } from '../../../../core/models/rol';
import { Permiso } from '../../../../core/models/permiso';
import { PermisoService } from '../../../../services/permiso.service';
import { UsuarioService } from '../../../../services/usuario';

@Component({
  selector: 'app-formulario-usuario',
  standalone: true, 
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule,
    TitleCasePipe
  ],
  templateUrl: './formulario-usuario.html',
})
export class FormularioUsuarioComponent implements OnInit {
  usuarioForm: FormGroup;
  esModoEdicion: boolean;
  rolesDisponibles = Object.values(Rol);
  permisosDisponibles: Permiso[] = [];

  constructor(
    private fb: FormBuilder,
    private authService: AutenticacionService,
    private usuarioService: UsuarioService,
    private notificacion: Notificacion,
    private permisoService: PermisoService,
    public dialogRef: MatDialogRef<FormularioUsuarioComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { usuario: UserProfile },
    private cdRef: ChangeDetectorRef
  ) {
    this.esModoEdicion = !!this.data.usuario; 

    this.usuarioForm = this.fb.group({
      nombres: ['', Validators.required],
      apellidos: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      rol: [Rol.PACIENTE, Validators.required],
      password: ['', this.esModoEdicion ? [] : [Validators.required, Validators.minLength(8)]],
      permisos: this.fb.array([])
    });

    if (this.esModoEdicion && this.data.usuario) {
      this.usuarioForm.patchValue(this.data.usuario);
    }
  }

  
  ngOnInit(): void {
    this.cargarPermisos();
  }

  cargarPermisos(): void {
    this.permisoService.getAllPermisos().subscribe(permisos => {
      this.permisosDisponibles = permisos;
      this.addPermisoCheckboxes();
      this.cdRef.detectChanges(); 

      if (this.esModoEdicion && this.data.usuario?.permisos) {
        this.marcarPermisosActuales(this.data.usuario.permisos);
      }
    });
  }

  private addPermisoCheckboxes() {
    this.permisosFormArray.clear(); 
    this.permisosDisponibles.forEach(() => this.permisosFormArray.push(new FormControl(false)));
  }

  private marcarPermisosActuales(permisosDelUsuario: string[]) {
    const permisosValues = this.permisosDisponibles.map(permiso => 
      permisosDelUsuario.includes(permiso.nombre)
    );
    this.permisosFormArray.setValue(permisosValues);
  }
  get permisosFormArray() {
    return this.usuarioForm.controls['permisos'] as FormArray;
  }

  cancelar(): void {
    this.dialogRef.close();
  }

  guardar(): void {
    if (this.usuarioForm.invalid) {
      return; 
    }

    if (this.esModoEdicion) {
      this.actualizarUsuarioExistente();
    } else {
      this.crearNuevoUsuario();
    }
  }
  private actualizarUsuarioExistente(): void {
    const selectedPermisosNombres = this.usuarioForm.value.permisos
      .map((checked: boolean, i: number) => checked ? this.permisosDisponibles[i].nombre : null)
      .filter((name: string | null) => name !== null);

    // Creamos el payload para actualizar
    const payload: UserProfile = {
      ...this.data.usuario, // Mantenemos ID, etc.
      ...this.usuarioForm.value, // Sobrescribimos con los valores del form
      permisos: selectedPermisosNombres,
    };
    
    if (this.data.usuario.id) {
        this.usuarioService.actualizarUsuario(this.data.usuario.id, payload).subscribe({
          next: () => {
            this.notificacion.mostrar('Usuario actualizado correctamente', 'exito');
            this.dialogRef.close(true);
          },
          error: (err) => {
            const mensajeError = err.error?.message || 'Ocurrió un error desconocido';
            this.notificacion.mostrar('Error al actualizar el usuario: ' + mensajeError, 'error');
          }
      });
    }
  }
  
  private crearNuevoUsuario(): void {
    const selectedPermisosNombres = this.usuarioForm.value.permisos
      .map((checked: boolean, i: number) => checked ? this.permisosDisponibles[i].nombre : null)
      .filter((name: string | null) => name !== null);

    const nuevoUsuario: CreateUserPayload = {
      nombres: this.usuarioForm.value.nombres,
      apellidos: this.usuarioForm.value.apellidos,
      email: this.usuarioForm.value.email,
      password: this.usuarioForm.value.password,
      rol: this.usuarioForm.value.rol,
      permisos: selectedPermisosNombres
    };

    this.authService.crearUsuarioPorAdmin(nuevoUsuario).subscribe({
      next: () => {
        this.notificacion.mostrar('Usuario creado correctamente', 'exito');
        this.dialogRef.close(true); 
      },
      error: (err) => {
        const mensajeError = err.error?.message || 'Ocurrió un error desconocido';
        this.notificacion.mostrar('Error al crear el usuario: ' + mensajeError, 'error');
      }
    });
  }
}