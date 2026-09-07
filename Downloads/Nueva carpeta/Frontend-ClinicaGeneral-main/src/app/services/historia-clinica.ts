import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Observable } from 'rxjs';
import { HistoriaClinica } from '../core/models/historia-clinica';
import { Consulta } from '../core/models/consulta';

@Injectable({
  providedIn: 'root'
})
export class HistoriaClinicaService {
  private apiUrl = `${environment.apiUrl}/historias`;

  constructor(private http: HttpClient) { }

  getMiHistorial(): Observable<HistoriaClinica> {
    return this.http.get<HistoriaClinica>(`${this.apiUrl}/mi-historial`);
  }


  getHistoriaPorPacienteId(pacienteId: string): Observable<HistoriaClinica> {
    return this.http.get<HistoriaClinica>(`${this.apiUrl}/paciente/${pacienteId}`);
  }

  /**
   * @param id 
   * @param datos
   */
  actualizarHistoria(id: string | number, datos: Partial<HistoriaClinica>): Observable<HistoriaClinica> {
    return this.http.put<HistoriaClinica>(`${this.apiUrl}/${id}`, datos);
  }

  /**
   * @param historiaId 
   * @param consultaData 
   */
  agregarConsulta(historiaId: string | number, consultaData: any): Observable<Consulta> {
    return this.http.post<Consulta>(`${this.apiUrl}/${historiaId}/consultas`, consultaData);
  }

  agregarReceta(consultaId: number, recetaData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/consultas/${consultaId}/recetas`, recetaData);
  }
}