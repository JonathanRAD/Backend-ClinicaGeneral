// RUTA: src/app/services/permiso.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Permiso } from '../core/models/permiso';

@Injectable({
  providedIn: 'root'
})
export class PermisoService {
  private apiUrl = `${environment.apiUrl}/permisos`;

  constructor(private http: HttpClient) { }

  getAllPermisos(): Observable<Permiso[]> {
    return this.http.get<Permiso[]>(this.apiUrl);
  }
}