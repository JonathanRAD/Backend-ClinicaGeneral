import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Cita } from '../core/models/cita';
import { environment } from '../environments/environment';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CitaService {
  private citasSignal = signal<Cita[]>([]);
  public citas = this.citasSignal.asReadonly();
  
  private apiUrl = `${environment.apiUrl}/citas`;

  constructor(private http: HttpClient) {
  }

  getMisCitas(): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${this.apiUrl}/mis-citas`);
  }

  obtenerCitasDelDia(): Cita[] {
    const hoy = new Date();
    return this.citas().filter(c => {
      const d = new Date(c.fechaHora);
      return d.getDate() === hoy.getDate() &&
             d.getMonth() === hoy.getMonth() &&
             d.getFullYear() === hoy.getFullYear() &&
             (c.estado === 'programada' || c.estado === 'en_espera');
    });
  }

  agendarCita(payload: { medicoId: number; fechaHora: string; motivo: string }): Observable<Cita> {
    return this.http.post<Cita>(`${this.apiUrl}/agendar`, payload);
  }

  cargarCitasPanel(): void {
    this.http.get<Cita[]>(this.apiUrl).subscribe({
      next: (citasDesdeApi) => this.citasSignal.set(citasDesdeApi),
      error: (err) => {
        this.citasSignal.set([]);
        console.error('Error al cargar citas para el panel:', err);
      }
    });
  }

  cancelarMiCita(id: string): Observable<void> { 
    return this.http.delete<void>(`${this.apiUrl}/mis-citas/${id}`);
  }

  crearCitaPanel(payload: any): Observable<Cita> {
    return this.http.post<Cita>(this.apiUrl, payload).pipe(
      tap(nuevaCita => {
        this.citasSignal.update(citas => [...citas, nuevaCita]);
      })
    );
  }

  actualizarCitaPanel(id: any, payload: any): Observable<Cita> {
    return this.http.put<Cita>(`${this.apiUrl}/${id}`, payload).pipe(
      tap(citaActualizada => {
        this.citasSignal.update(citas =>
          citas.map(c => (c.id === citaActualizada.id ? citaActualizada : c))
        );
      })
    );
  }

  registrarTriaje(id: any, triajeData: any): Observable<Cita> {
    return this.http.post<Cita>(`${this.apiUrl}/${id}/triaje`, triajeData).pipe(
      tap(citaActualizada => {
        this.citasSignal.update(citas =>
          citas.map(c => (c.id === citaActualizada.id ? citaActualizada : c))
        );
      })
    );
  }
  
  eliminarCitaPanel(id: any): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        this.citasSignal.update(citas =>
          citas.filter(c => c.id !== id)
        );
      })
    );
  }
}