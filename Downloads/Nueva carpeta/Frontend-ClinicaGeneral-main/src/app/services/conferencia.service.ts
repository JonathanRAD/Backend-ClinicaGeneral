import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Conferencia } from '../core/models/conferencia';
import { environment } from '../environments/environment';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ConferenciaService {
  private apiUrl = `${environment.apiUrl}/conferencias`;

  private conferenciasSignal = signal<Conferencia[]>([]);
  public conferencias = this.conferenciasSignal.asReadonly();

  constructor(private http: HttpClient) {}

  cargarTodas(): void {
    this.http.get<Conferencia[]>(this.apiUrl).subscribe({
      next: (data) => this.conferenciasSignal.set(data),
      error: (err) => console.error('Error al cargar conferencias:', err)
    });
  }

  getPorPaciente(pacienteId: string | number): Observable<Conferencia[]> {
    return this.http.get<Conferencia[]>(`${this.apiUrl}/paciente/${pacienteId}`);
  }

  getPorMedico(medicoId: string | number): Observable<Conferencia[]> {
    return this.http.get<Conferencia[]>(`${this.apiUrl}/medico/${medicoId}`);
  }

  programar(conferencia: Conferencia): Observable<Conferencia> {
    return this.http.post<Conferencia>(this.apiUrl, conferencia).pipe(
      tap((nueva) => {
        this.conferenciasSignal.update(lista => [...lista, nueva]);
      })
    );
  }

  actualizarEstado(id: number, estado: string): Observable<Conferencia> {
    return this.http.put<Conferencia>(`${this.apiUrl}/${id}/estado`, { estado }).pipe(
      tap((actualizada) => {
        this.conferenciasSignal.update(lista =>
          lista.map(c => c.id === actualizada.id ? actualizada : c)
        );
      })
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        this.conferenciasSignal.update(lista => lista.filter(c => c.id !== id));
      })
    );
  }
}
