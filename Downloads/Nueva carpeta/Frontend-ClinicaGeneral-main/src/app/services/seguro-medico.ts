
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Observable } from 'rxjs';
import { SeguroMedico } from '../core/models/seguro-medico';

@Injectable({
  providedIn: 'root'
})
export class SeguroMedicoService {
  private apiUrl = `${environment.apiUrl}/seguros`;

  constructor(private http: HttpClient) { }

  guardarSeguro(pacienteId: string, seguroData: any, seguroId?: number): Observable<SeguroMedico> {
    if (seguroId) {
      return this.http.put<SeguroMedico>(`${this.apiUrl}/${seguroId}`, seguroData);
    } else {
      return this.http.post<SeguroMedico>(`${this.apiUrl}/paciente/${pacienteId}`, seguroData);
    }
  }

  validarSeguro(pacienteId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/validar/paciente/${pacienteId}`);
  }
}