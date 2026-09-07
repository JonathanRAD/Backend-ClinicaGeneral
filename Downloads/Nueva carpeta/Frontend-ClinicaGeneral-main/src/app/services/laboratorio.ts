
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { OrdenLaboratorio } from '../core/models/orden-laboratorio';

export interface OrdenLaboratorioPayload {
  tipoExamen: string;
  observaciones: string;
}

@Injectable({
  providedIn: 'root'
})
export class LaboratorioService {
  private apiUrl = `${environment.apiUrl}/laboratorio`;

  constructor(private http: HttpClient) { }

  crearOrden(consultaId: number, payload: OrdenLaboratorioPayload): Observable<OrdenLaboratorio> {
    return this.http.post<OrdenLaboratorio>(`${this.apiUrl}/ordenes/consulta/${consultaId}`, payload);
  }
}