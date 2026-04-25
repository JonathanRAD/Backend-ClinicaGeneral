import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { AutenticacionService } from '../../services/autenticacion';
import { Notificacion } from '../../services/notificacion';

// Reutilizamos la validación de contraseña segura
export function contrasenaSeguraValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;
    const tieneMayuscula = /[A-Z]/.test(value);
    const tieneMinuscula = /[a-z]/.test(value);
    const tieneNumero = /[0-9]/.test(value);
    const tieneCaracterEspecial = /[@$!%*?&_#.,;:<>(){}\[\]\-+=/\|~`^]/.test(value);
    const esValida = tieneMayuscula && tieneMinuscula && tieneNumero && tieneCaracterEspecial;
    return !esValida ? { contrasenaInsegura: true } : null;
  };
}

// Validación para que las contraseñas coincidan
export function contrasenasIgualesValidator(controlName: string, matchingControlName: string): ValidatorFn {
  return (formGroup: AbstractControl): ValidationErrors | null => {
    const control = formGroup.get(controlName);
    const matchingControl = formGroup.get(matchingControlName);

    if (!control || !matchingControl) {
      return null;
    }

    if (matchingControl.errors && !matchingControl.errors['mustMatch']) {
      // return if another validator has already found an error on the matchingControl
      return null;
    }

    // set error on matchingControl if validation fails
    if (control.value !== matchingControl.value) {
      matchingControl.setErrors({ mustMatch: true });
      return { mustMatch: true }; // Devuelve el error a nivel de grupo también
    } else {
      // Si antes había error mustMatch, quítalo
      if (matchingControl.hasError('mustMatch')) {
          const errors = { ...matchingControl.errors }; // Copia los errores existentes
          delete errors['mustMatch']; // Elimina el error mustMatch
          // Si no quedan otros errores, establece los errores a null
          matchingControl.setErrors(Object.keys(errors).length > 0 ? errors : null);
      }
      return null;
    }
  };
}


@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.css'] // Asegúrate de crear este archivo
})
export class ResetPasswordComponent implements OnInit {
  resetForm: FormGroup;
  token: string | null = null;
  cargando = signal(false);
  mensajeError = signal('');
  mensajeExito = signal('');
  mostrarContrasena = signal(false);
  mostrarConfirmar = signal(false);


  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AutenticacionService,
    private notificacion: Notificacion
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8), contrasenaSeguraValidator()]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: contrasenasIgualesValidator('newPassword', 'confirmPassword')
    });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token');
    if (!this.token) {
      this.mensajeError.set('Token inválido o faltante.');
      this.notificacion.mostrar('Token inválido o faltante.', 'error');
      this.router.navigate(['/login']);
    }
  }

  toggleMostrarContrasena(campo: 'nueva' | 'confirmar') {
    if(campo === 'nueva') {
      this.mostrarContrasena.update(v => !v);
    } else {
      this.mostrarConfirmar.update(v => !v);
    }
  }

  onSubmit(): void {
    if (this.resetForm.invalid || !this.token) {
      this.resetForm.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.mensajeError.set('');
    this.mensajeExito.set('');

    const newPassword = this.resetForm.value.newPassword;

    this.authService.resetearContrasena(this.token, newPassword).subscribe({
      next: (respuesta) => {
        this.mensajeExito.set(respuesta.message || 'Contraseña actualizada con éxito. Serás redirigido al login.');
        this.cargando.set(false);
        this.resetForm.disable(); // Deshabilitar formulario en éxito
        setTimeout(() => this.router.navigate(['/login']), 4000); // Redirigir después de 4 seg
      },
      error: (error: Error) => {
        this.mensajeError.set(error.message || 'Ocurrió un error al restablecer la contraseña.');
        this.cargando.set(false);
      }
    });
  }

  get f() { return this.resetForm.controls; }
}