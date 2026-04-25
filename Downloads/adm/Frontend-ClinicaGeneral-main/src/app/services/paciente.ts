
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Patient } from '../core/models/patient';
import { environment } from '../environments/environment'; 

@Injectable({
  providedIn: 'root'
})
export class PacienteService {
  private pacientesSignal = signal<Patient[]>([]);
  public pacientes = this.pacientesSignal.asReadonly();

  private apiUrl = `${environment.apiUrl}/pacientes`;

  constructor(private http: HttpClient) {
    this.cargarPacientes();
  }

  cargarPacientes(): void {
    this.http.get<Patient[]>(this.apiUrl).subscribe({
      next: (pacientesDesdeApi) => {
        this.pacientesSignal.set(pacientesDesdeApi);
        console.log('Pacientes cargados desde la API:', pacientesDesdeApi);
      },
      error: (err) => {
        this.pacientesSignal.set([]);
        console.error('Error al cargar pacientes:', err);
      }
    });
  }
  registrarPaciente(paciente: Omit<Patient, 'id'>) {
    this.http.post<Patient>(this.apiUrl, paciente).subscribe({
      next: (nuevoPaciente) => {
        this.pacientesSignal.update(pacientes => [...pacientes, nuevoPaciente]);
      },
      error: (err) => console.error('Error al registrar paciente:', err)
    });
  }

  actualizarPaciente(pacienteActualizado: Patient) {
    this.http.put<Patient>(`${this.apiUrl}/${pacienteActualizado.id}`, pacienteActualizado).subscribe({
      next: (pacienteRespuesta) => {
        this.pacientesSignal.update(pacientes =>
          pacientes.map(p => (p.id === pacienteRespuesta.id ? pacienteRespuesta : p))
        );
      },
      error: (err) => console.error('Error al actualizar paciente:', err)
    });
  }

  eliminarPaciente(id: string) {
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        this.pacientesSignal.update(pacientes =>
          pacientes.filter(p => p.id !== id)
        );
      },
      error: (err) => console.error('Error al eliminar paciente:', err)
    });
  }
}