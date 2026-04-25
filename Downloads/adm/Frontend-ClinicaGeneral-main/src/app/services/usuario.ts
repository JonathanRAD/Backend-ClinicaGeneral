import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';
import { Rol } from '../core/models/rol';
import { UserProfile, ChangePasswordPayload, CreateUserPayload } from './../core/models/usuario';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private apiUrl = `${environment.apiUrl}/usuarios`;
  
  private usuariosSignal = signal<UserProfile[]>([]);
  public usuarios = this.usuariosSignal.asReadonly();

  constructor(private http: HttpClient) { }

  getMiPerfil(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`);
  }

  public getAllUsuarios(): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(this.apiUrl).pipe(
      tap(usuarios => this.usuariosSignal.set(usuarios))
    );
  }

  crearUsuario(payload: CreateUserPayload): Observable<any> {
    return this.http.post(`${this.apiUrl}`, payload).pipe(
    );
  }

  actualizarUsuario(id: number, payload: UserProfile): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/${id}`, payload).pipe(
    );
  }

  eliminarUsuario(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`).pipe(
    );
  }

  /** Elimina la propia cuenta del usuario autenticado.
   *  Backend: DELETE /api/usuarios/me */
  eliminarMiCuenta(): Observable<any> {
    return this.http.delete(`${this.apiUrl}/me`);
  }

  actualizarPerfil(perfil: UserProfile): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/me`, perfil);
  }

  cambiarContrasena(payload: ChangePasswordPayload): Observable<any> {
    return this.http.put(`${this.apiUrl}/me/password`, payload);
  }
}