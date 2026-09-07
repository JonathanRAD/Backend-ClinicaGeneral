import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';
import { AuditLog, AuditPageResponse, AuditEstadisticas, AuditFiltros } from '../core/models/auditoria';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private apiUrl = `${environment.apiUrl}/audit`;
  private http = inject(HttpClient);

  // ── Señales Reactivas ────────────────────────────────────────────────────
  logs = signal<AuditLog[]>([]);
  estadisticas = signal<AuditEstadisticas | null>(null);
  totalElements = signal(0);
  totalPages = signal(0);
  isLoading = signal(false);

  /**
   * Obtiene los logs de auditoría con filtros y paginación.
   */
  getLogs(filtros: AuditFiltros, page: number = 0, size: number = 20): Observable<AuditPageResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filtros.usuarioId) params = params.set('usuarioId', filtros.usuarioId.toString());
    if (filtros.rol) params = params.set('rol', filtros.rol);
    if (filtros.accion) params = params.set('accion', filtros.accion);
    if (filtros.entidad) params = params.set('entidad', filtros.entidad);
    if (filtros.fechaInicio) params = params.set('fechaInicio', filtros.fechaInicio);
    if (filtros.fechaFin) params = params.set('fechaFin', filtros.fechaFin);
    if (filtros.busqueda) params = params.set('busqueda', filtros.busqueda);

    this.isLoading.set(true);
    return this.http.get<AuditPageResponse>(this.apiUrl, { params }).pipe(
      tap(response => {
        this.logs.set(response.content);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.isLoading.set(false);
      })
    );
  }
  
  /**
   * Obtiene los logs de auditoría sin actualizar el estado del servicio (ideal para exportaciones).
   */
  obtenerLogsSinActualizar(filtros: AuditFiltros, page: number = 0, size: number = 1000): Observable<AuditPageResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filtros.usuarioId) params = params.set('usuarioId', filtros.usuarioId.toString());
    if (filtros.rol) params = params.set('rol', filtros.rol);
    if (filtros.accion) params = params.set('accion', filtros.accion);
    if (filtros.entidad) params = params.set('entidad', filtros.entidad);
    if (filtros.fechaInicio) params = params.set('fechaInicio', filtros.fechaInicio);
    if (filtros.fechaFin) params = params.set('fechaFin', filtros.fechaFin);
    if (filtros.busqueda) params = params.set('busqueda', filtros.busqueda);

    return this.http.get<AuditPageResponse>(this.apiUrl, { params });
  }

  /**
   * Obtiene las estadísticas generales de auditoría.
   */
  getEstadisticas(): Observable<AuditEstadisticas> {
    return this.http.get<AuditEstadisticas>(`${this.apiUrl}/estadisticas`).pipe(
      tap(data => this.estadisticas.set(data))
    );
  }

  /**
   * Obtiene la actividad reciente del sistema.
   */
  getActividadReciente(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.apiUrl}/actividad-reciente`);
  }
}
