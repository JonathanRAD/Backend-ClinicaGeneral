
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Medico } from '../core/models/medico';
import { environment } from '../environments/environment';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MedicoService {
  private medicosSignal = signal<Medico[]>([]);
  public medicos = this.medicosSignal.asReadonly();
  private apiUrl = `${environment.apiUrl}/medicos`;

  constructor(private http: HttpClient) {
    this.cargarMedicos();
  }

  getMedicos(): Observable<Medico[]> {
    return this.http.get<Medico[]>(this.apiUrl);
  }
  cargarMedicos(): void {
    this.http.get<Medico[]>(this.apiUrl).subscribe({
      next: (medicosDesdeApi) => this.medicosSignal.set(medicosDesdeApi),
      error: (err) => {
        console.error('Error al cargar médicos:', err);
        this.medicosSignal.set([]);
      }
    });
  }

  crearMedico(medico: Omit<Medico, 'id'>) {
    this.http.post<Medico>(this.apiUrl, medico).subscribe({
      next: (nuevoMedico) => {
        this.medicosSignal.update(medicos => [...medicos, nuevoMedico]);
      },
      error: (err) => console.error('Error al crear médico:', err)
    });
  }

  actualizarMedico(medico: Medico) {
    this.http.put<Medico>(`${this.apiUrl}/${medico.id}`, medico).subscribe({
      next: (medicoActualizado) => {
        this.medicosSignal.update(medicos =>
          medicos.map(m => m.id === medicoActualizado.id ? medicoActualizado : m)
        );
      },
      error: (err) => console.error('Error al actualizar médico:', err)
    });
  }

  eliminarMedico(id: string) {
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        this.medicosSignal.update(medicos => medicos.filter(m => m.id !== id));
      },
      error: (err) => console.error('Error al eliminar médico:', err)
    });
  }
}