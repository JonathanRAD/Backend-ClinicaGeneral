import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Observable } from 'rxjs';
import { HistoriaClinica } from '../modelos/historia-clinica';
import { Consulta } from '../modelos/consulta'; 

@Injectable({
  providedIn: 'root'
})
export class HistoriaClinicaService {
  private apiUrl = `${environment.apiUrl}/historias`;

  constructor(private http: HttpClient) { }

  getHistoriaPorPacienteId(pacienteId: string): Observable<HistoriaClinica> {
    return this.http.get<HistoriaClinica>(`${this.apiUrl}/paciente/${pacienteId}`);
  }

  actualizarHistoria(id: number, datos: Partial<HistoriaClinica>): Observable<HistoriaClinica> {
    return this.http.put<HistoriaClinica>(`${this.apiUrl}/${id}`, datos);
  }

  agregarConsulta(historiaId: number, consultaData: any): Observable<Consulta> {
    return this.http.post<Consulta>(`${this.apiUrl}/${historiaId}/consultas`, consultaData);
  }
}