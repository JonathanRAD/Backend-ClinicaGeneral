import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../environments/environment';
import { tap, catchError } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { CreateUserPayload } from '../core/models/usuario';

interface AuthResponse {
  token: string;
}

interface RegisterPayload {
  nombres: string;
  apellidos: string;
  email: string;
  password: string;
}

interface PasswordResetPayload {
  email: string;
}

interface ResetPasswordPayload {
  token: string;
  newPassword: string;
}

interface DecodedToken {
  sub: string;
  rol: string;
  permisos: string[];
  iat: number;
  exp: number;
}

interface MensajeRespuesta {
    message: string;
}


@Injectable({
  providedIn: 'root'
})
export class AutenticacionService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private usuariosUrl = `${environment.apiUrl}/usuarios`;

  usuarioLogueado = signal<boolean>(false);
  rolUsuario = signal<string>('');
  permisosUsuario = signal<string[]>([]);

  constructor(
    private router: Router,
    private http: HttpClient,
  ) {
    this.verificarTokenAlCargar();
  }

  private verificarTokenAlCargar(): void {
    const token = this.getToken();
    if (token) {
      if (this.esTokenExpirado(token)) {
        this.logout();
      } else {
        this.usuarioLogueado.set(true);
        this.decodificarToken(token);
      }
    }
  }

   crearUsuarioPorAdmin(payload: CreateUserPayload): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/create-user`, payload).pipe(
      catchError(this.handleError)
    );
  }


  login(email: string, contrasena: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password: contrasena })
      .pipe(
        tap(response => {
          this.manejarRespuestaAutenticacion(response.token);
        }),
        catchError(this.handleError)
      );
  }

  register(payload: RegisterPayload): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, payload)
      .pipe(
        tap(response => {
          // Si el backend devuelve token directamente, autenticar de inmediato
          if (response?.token) {
            this.manejarRespuestaAutenticacion(response.token);
          }
          // Si devuelve { requiresOtp: true, email }, el componente Login
          // cambia a modo 'otp' y espera la verificación del código
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Verifica el código OTP de 6 dígitos enviado por email tras el registro.
   * Backend pendiente: POST /api/auth/verify-otp con body { email, otp }
   * El backend debe almacenar otpCode y otpExpiry en la entidad Usuario
   * (similar al patrón resetPasswordToken/resetPasswordTokenExpiry ya existente).
   */
  verificarOtp(email: string, otp: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/verify-otp`, { email, otp })
      .pipe(
        tap(response => {
          this.manejarRespuestaAutenticacion(response.token);
        }),
        catchError(this.handleError)
      );
  }

  solicitarReseteoContrasena(email: string): Observable<MensajeRespuesta> {
    const payload: PasswordResetPayload = { email };
    return this.http.post<MensajeRespuesta>(`${this.apiUrl}/request-password-reset`, payload).pipe(
      catchError(this.handleError)
    );
  }

  resetearContrasena(token: string, newPassword: string): Observable<MensajeRespuesta> {
    const payload: ResetPasswordPayload = { token, newPassword };
    return this.http.post<MensajeRespuesta>(`${this.apiUrl}/reset-password`, payload).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Solicita el envío de un OTP por email para confirmar la eliminación de cuenta.
   * Backend: POST /api/usuarios/me/request-delete-otp
   */
  solicitarOtpEliminarCuenta(): Observable<MensajeRespuesta> {
    return this.http.post<MensajeRespuesta>(`${this.usuariosUrl}/me/request-delete-otp`, {}).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Elimina la cuenta del usuario autenticado enviando el OTP de confirmación.
   * Backend: DELETE /api/usuarios/me  body: { otp }
   */
  eliminarMiCuenta(otp: string): Observable<void> {
    return this.http.delete<void>(`${this.usuariosUrl}/me`, { body: { otp } }).pipe(
      catchError(this.handleError)
    );
  }

  logout() {
    localStorage.removeItem('jwt_token');
    this.usuarioLogueado.set(false);
    this.rolUsuario.set('');
    this.permisosUsuario.set([]);
    this.router.navigate(['/login']);
  }

  estaLogueado(): boolean {
     const token = this.getToken();
     return !!token && !this.esTokenExpirado(token);
  }


  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  getNombreUsuario(): string | null {
    const token = this.getToken();
    if (token && !this.esTokenExpirado(token)) {
      try {
        const payload: DecodedToken = jwtDecode(token);
        return payload.sub;
      } catch (error) {
        console.error('Error decoding token:', error);
        return null;
      }
    }
    return null;
  }

  tienePermiso(permiso: string): boolean {
    if (!this.estaLogueado()) {
        return false;
    }
    return this.permisosUsuario().includes(permiso);
  }


  private manejarRespuestaAutenticacion(token: string): void {
    if (this.esTokenExpirado(token)) {
        console.error("Received expired token");
        this.logout();
        return;
    }
    localStorage.setItem('jwt_token', token);
    this.usuarioLogueado.set(true);
    this.decodificarToken(token);
  }

  private esTokenExpirado(token: string): boolean {
    try {
      const payload: DecodedToken = jwtDecode(token);
      if (!payload.exp) return true;
      const fechaExpiracion = payload.exp * 1000;
      return fechaExpiracion < Date.now();
    } catch (error) {
      console.error('Error checking token expiration:', error);
      return true;
    }
  }

  private decodificarToken(token: string): void {
    try {
      const payload: DecodedToken = jwtDecode(token);
      this.rolUsuario.set(payload.rol || '');
      this.permisosUsuario.set(payload.permisos || []);
    } catch (error) {
      console.error('Error al decodificar el token JWT:', error);
      this.rolUsuario.set('');
      this.permisosUsuario.set([]);
       this.logout();
    }
  }

  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);
    let errorMessage = 'Ocurrió un error desconocido.';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      if (error.status === 401 && error.error?.message) {
         errorMessage = error.error.message;
      } else if (error.status === 400 && error.error?.message) {
          errorMessage = error.error.message;
          if (error.error.errors && typeof error.error.errors === 'object') {
              const fieldErrors = Object.values(error.error.errors).join(' ');
              errorMessage += ` Detalles: ${fieldErrors}`;
          }
      } else if (error.status === 403) {
          errorMessage = "No tienes permiso para realizar esta acción.";
      } else {
         errorMessage = `Error ${error.status}: ${error.statusText}. ${error.error?.message || ''}`;
      }
    }
    return throwError(() => new Error(errorMessage));
  }
}