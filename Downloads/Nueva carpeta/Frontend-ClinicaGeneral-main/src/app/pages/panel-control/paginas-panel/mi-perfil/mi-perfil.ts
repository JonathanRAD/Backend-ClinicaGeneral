import { Component, OnInit, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { UsuarioService } from '../../../../services/usuario';
import { AutenticacionService } from '../../../../services/autenticacion';
import { UserProfile } from '../../../../core/models/usuario';
import { DialogoConfirmacion } from '../../componentes-panel/dialogo-confirmacion/dialogo-confirmacion';

@Component({
  selector: 'app-mi-perfil',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './mi-perfil.html',
  styleUrls: ['./mi-perfil.css']
})
export class MiPerfilComponent implements OnInit {
  perfilForm: FormGroup;
  contrasenaForm: FormGroup;
  modoEdicion = false;
  eliminandoCuenta = false;

  usuario: WritableSignal<UserProfile | null> = signal(null);
  permisos: WritableSignal<string[]> = signal([]);

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private authService: AutenticacionService,
    private snackBar: MatSnackBar,
    public dialog: MatDialog
  ) {
    this.perfilForm = this.fb.group({
      nombres: ['', Validators.required],
      apellidos: ['', Validators.required],
      email: [{ value: '', disabled: true }]
    });

    this.contrasenaForm = this.fb.group({
      contrasenaActual: ['', Validators.required],
      nuevaContrasena: ['', [Validators.required, Validators.minLength(8)]],
      confirmarContrasena: ['', Validators.required]
    }, { validator: this.checkPasswords });
  }

  ngOnInit(): void {
    this.usuarioService.getMiPerfil().subscribe(user => {
      this.usuario.set(user);
      this.perfilForm.patchValue(user);
      this.permisos.set(this.mapearRolAPermisos(user.rol));
    });
  }

  private mapearRolAPermisos(rol: string): string[] {
    const permisosPorRol: { [key: string]: string[] } = {
      'ADMINISTRADOR': ['Acceso total al sistema', 'Gestionar usuarios y roles', 'Ver reportes financieros', 'Configuración avanzada'],
      'MEDICO': ['Acceder a historias clínicas', 'Registrar consultas y diagnósticos', 'Generar órdenes de laboratorio'],
      'RECEPCIONISTA': ['Gestionar agenda de citas', 'Registrar y buscar pacientes', 'Ver disponibilidad de médicos']
    };
    return permisosPorRol[rol] || ['Permisos básicos de la plataforma'];
  }

  checkPasswords(group: FormGroup) {
    const pass = group.controls['nuevaContrasena'].value;
    const confirmPass = group.controls['confirmarContrasena'].value;
    return pass === confirmPass ? null : { notSame: true };
  }

  activarEdicion(): void {
    this.modoEdicion = true;
  }

  cancelarEdicion(): void {
    this.modoEdicion = false;
    this.ngOnInit();
  }

  guardarPerfil(): void {
    if (this.perfilForm.invalid || !this.usuario()) return;

    const datosPerfil: UserProfile = {
      ...this.usuario()!,
      ...this.perfilForm.getRawValue()
    };

    this.usuarioService.actualizarPerfil(datosPerfil).subscribe({
      next: (usuarioActualizado) => {
        this.usuario.set(usuarioActualizado);
        this.snackBar.open('Perfil actualizado con éxito', 'Cerrar', { duration: 3000 });
        this.modoEdicion = false;
      },
      error: () => this.snackBar.open('Error al actualizar el perfil', 'Cerrar', { duration: 3000 })
    });
  }

  cambiarContrasena(): void {
    if (this.contrasenaForm.invalid) return;

    this.usuarioService.cambiarContrasena({
      contrasenaActual: this.contrasenaForm.value.contrasenaActual,
      nuevaContrasena: this.contrasenaForm.value.nuevaContrasena
    }).subscribe({
      next: () => {
        this.snackBar.open('Contraseña actualizada correctamente', 'Cerrar', { duration: 3000 });
        this.contrasenaForm.reset();
      },
      error: () => this.snackBar.open('Error al cambiar la contraseña', 'Cerrar', { duration: 3000 })
    });
  }

  /** Elimina la cuenta propia del usuario. Requiere confirmación doble. */
  eliminarMiCuenta(): void {
    const nombreUsuario = `${this.usuario()?.nombres} ${this.usuario()?.apellidos}`;

    const dialogRef = this.dialog.open(DialogoConfirmacion, {
      width: '460px',
      data: {
        titulo: '⚠️ Eliminar mi cuenta',
        mensaje: `Esta acción es permanente e irreversible. Tu cuenta (${nombreUsuario}) será eliminada y perderás acceso inmediatamente. ¿Estás absolutamente seguro?`,
        textoConfirmar: 'Sí, eliminar mi cuenta',
        textoCancelar: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmado => {
      if (confirmado) {
        this.eliminandoCuenta = true;
        this.usuarioService.eliminarMiCuenta().subscribe({
          next: () => {
            this.snackBar.open('Tu cuenta ha sido eliminada.', 'Cerrar', { duration: 3000 });
            this.authService.logout();
          },
          error: (err) => {
            this.eliminandoCuenta = false;
            const msg = err.message?.includes('404')
              ? 'El endpoint de eliminación aún no está implementado en el backend.'
              : (err.message || 'No se pudo eliminar la cuenta. Intenta de nuevo.');
            this.snackBar.open(msg, 'Cerrar', { duration: 5000, panelClass: 'notificacion-error' });
          }
        });
      }
    });
  }
}