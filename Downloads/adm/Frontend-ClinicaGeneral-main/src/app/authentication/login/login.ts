
import { Component, OnInit, signal, ViewChildren, QueryList, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AutenticacionService } from '../../services/autenticacion';


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

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ CommonModule, ReactiveFormsModule, FormsModule, RouterModule ],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login implements OnInit {
  formularioLogin: FormGroup;
  formularioRegistro: FormGroup;
  maxNombreApellidoLength = 20;

  modo = signal<'login' | 'registro' | 'otp'>('login');
  mensajeError = signal<string>('');
  cargando = signal<boolean>(false);
  recordarme = false;
  mostrarContrasena = false;

  /** Email guardado temporalmente para el flujo OTP */
  emailPendienteOtp = signal<string>('');

  /** Los 6 dígitos OTP como array separado para los inputs individuales */
  otpDigits: string[] = ['', '', '', '', '', ''];

  constructor(
    private fb: FormBuilder,
    private authService: AutenticacionService,
    private router: Router
  ) {
    this.formularioLogin = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });

    this.formularioRegistro = this.fb.group({
        nombres: ['', [Validators.required, Validators.maxLength(this.maxNombreApellidoLength), Validators.pattern('^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$')]],
        apellidos: ['', [Validators.required, Validators.maxLength(this.maxNombreApellidoLength), Validators.pattern('^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$')]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [
            Validators.required,
            Validators.minLength(8),
            contrasenaSeguraValidator()
        ]]
    });
  }

  ngOnInit(): void {
    const recordarmeGuardado = localStorage.getItem('recordarme');
    this.recordarme = recordarmeGuardado ? JSON.parse(recordarmeGuardado) : false;
    if (this.recordarme) {
      const emailRecordado = localStorage.getItem('correoRecordado') || '';
      this.formularioLogin.get('email')?.setValue(emailRecordado);
    }
  }

  toggleMostrarContrasena(): void {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

  cambiarModo(): void {
    this.modo.set(this.modo() === 'login' ? 'registro' : 'login');
    this.mensajeError.set('');
    this.formularioLogin.reset();
    this.formularioRegistro.reset();
  }

  // ── OTP helpers ────────────────────────────────────────────────────────────

  /** Maneja la entrada en cada input OTP y avanza al siguiente */
  onOtpInput(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.replace(/\D/g, '');       // solo dígitos
    input.value = value.slice(-1);                        // máximo 1 dígito
    this.otpDigits[index] = input.value;

    // Avanzar al siguiente input
    if (input.value && index < 5) {
      const next = document.getElementById(`otp-${index + 1}`) as HTMLInputElement;
      next?.focus();
    }
  }

  /** Retrocede al input anterior con Backspace */
  onOtpKeydown(event: KeyboardEvent, index: number): void {
    if (event.key === 'Backspace' && !this.otpDigits[index] && index > 0) {
      const prev = document.getElementById(`otp-${index - 1}`) as HTMLInputElement;
      prev?.focus();
    }
  }

  /** Permite pegar un código de 6 dígitos directamente */
  onOtpPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const pasted = event.clipboardData?.getData('text')?.replace(/\D/g, '').slice(0, 6) || '';
    pasted.split('').forEach((digit, i) => {
      this.otpDigits[i] = digit;
      const inp = document.getElementById(`otp-${i}`) as HTMLInputElement;
      if (inp) inp.value = digit;
    });
    // Enfocar el último input rellenado
    const lastIdx = Math.min(pasted.length - 1, 5);
    const last = document.getElementById(`otp-${lastIdx}`) as HTMLInputElement;
    last?.focus();
  }

  get otpCompleto(): boolean {
    return this.otpDigits.every(d => d !== '');
  }

  get otpString(): string {
    return this.otpDigits.join('');
  }

  // ── Verificar OTP ──────────────────────────────────────────────────────────
  verificarOtp(): void {
    if (!this.otpCompleto) return;
    this.mensajeError.set('');
    this.cargando.set(true);

    this.authService.verificarOtp(this.emailPendienteOtp(), this.otpString).subscribe({
      next: () => {
        this.redirigirSegunRol();
      },
      error: (err) => {
        this.mensajeError.set(err.message || 'Código incorrecto o expirado. Intente de nuevo.');
        this.cargando.set(false);
      }
    });
  }

  reenviarOtp(): void {
    // Re-enviar el registro para generar un nuevo OTP
    this.mensajeError.set('');
    this.cargando.set(true);
    this.authService.register(this.formularioRegistro.value).subscribe({
      next: () => {
        this.cargando.set(false);
        this.otpDigits = ['', '', '', '', '', ''];
        this.mensajeError.set('');
        // Mostrar mensaje de éxito usando el campo de error con estilo positivo
      },
      error: (err) => {
        this.mensajeError.set(err.message || 'No se pudo reenviar el código.');
        this.cargando.set(false);
      }
    });
  }

  // ── Enviar formulario (login / registro) ────────────────────────────────────
  enviarFormulario(): void {
    this.mensajeError.set('');
    this.cargando.set(true);

    if (this.modo() === 'login') {
      if (this.formularioLogin.invalid) {
        this.formularioLogin.markAllAsTouched();
        this.cargando.set(false);
        return;
      }
      
      const { email, password } = this.formularioLogin.value;
      this.authService.login(email, password).subscribe({
        next: () => {
          if (this.recordarme) {
            localStorage.setItem('correoRecordado', email);
            localStorage.setItem('recordarme', JSON.stringify(true));
          } else {
            localStorage.removeItem('correoRecordado');
            localStorage.removeItem('recordarme');
          }
          this.redirigirSegunRol();
        },
        error: (err) => {
          this.mensajeError.set(err.message || 'Ocurrió un error inesperado.');
          this.cargando.set(false);
        }
      });
    } else { 
      if (this.formularioRegistro.invalid) {
        this.formularioRegistro.markAllAsTouched();
        this.cargando.set(false);
        return;
      }

      const email = this.formularioRegistro.value.email;

      this.authService.register(this.formularioRegistro.value).subscribe({
        next: (response: any) => {
          this.cargando.set(false);
          if (response?.requiresOtp) {
            // Backend solicita verificación OTP → cambiar a pantalla OTP
            this.emailPendienteOtp.set(response.email || email);
            this.otpDigits = ['', '', '', '', '', ''];
            this.modo.set('otp');
          } else {
            // Backend devolvió token directo → redirigir
            this.redirigirSegunRol();
          }
        },
        error: (err) => {
          this.mensajeError.set(err.message || 'Error en el registro.');
          this.cargando.set(false);
        }
      });
    }
  }

  private redirigirSegunRol(): void {
    const rol = this.authService.rolUsuario();
    if (rol === 'PACIENTE') {
      this.router.navigate(['/portal']); 
    } else {
      this.router.navigate(['/panel']); 
    }
  }

  get loginCtls() {
    return this.formularioLogin.controls;
  }

  get registroCtls() {
    return this.formularioRegistro.controls;
  }
}
