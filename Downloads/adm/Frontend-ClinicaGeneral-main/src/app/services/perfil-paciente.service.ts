import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Patient } from '../core/models/patient';

@Injectable({
  providedIn: 'root'
})
export class PerfilPacienteService {
  private apiUrl = `${environment.apiUrl}/pacientes/mi-perfil`;

  constructor(private http: HttpClient) { }

  getMiPerfil(): Observable<Patient> {
    return this.http.get<Patient>(this.apiUrl);
  }

  /**
   * @param perfil 
   */
  actualizarMiPerfil(perfil: Patient): Observable<Patient> {
    return this.http.put<Patient>(this.apiUrl, perfil);
  }
}