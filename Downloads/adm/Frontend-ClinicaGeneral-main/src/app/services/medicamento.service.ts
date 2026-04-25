import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Medicamento, LoteMedicamento } from '../core/models/medicamento';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MedicamentoService {
  private apiUrl = `${environment.apiUrl}/medicamentos`;

  // Signal reactivo para la tabla
  private medicamentosSignal = signal<Medicamento[]>([]);
  public medicamentos = this.medicamentosSignal.asReadonly();

  constructor(private http: HttpClient) {}

  cargarMedicamentos(): void {
    this.http.get<Medicamento[]>(this.apiUrl).subscribe({
      next: (data) => this.medicamentosSignal.set(data),
      error: (err) => console.error('Error al cargar inventario', err)
    });
  }

  crearMedicamento(payload: Medicamento): Observable<Medicamento> {
    return this.http.post<Medicamento>(this.apiUrl, payload).pipe(
      tap(nuevoMed => {
        this.medicamentosSignal.update(meds => [...meds, nuevoMed]);
      })
    );
  }

  actualizarMedicamento(id: any, payload: Medicamento): Observable<Medicamento> {
    return this.http.put<Medicamento>(`${this.apiUrl}/${id}`, payload).pipe(
      tap(medActualizado => {
        this.medicamentosSignal.update(meds => 
          meds.map(m => m.id === medActualizado.id ? medActualizado : m)
        );
      })
    );
  }

  agregarLote(medicamentoId: any, lote: LoteMedicamento): Observable<Medicamento> {
    return this.http.post<Medicamento>(`${this.apiUrl}/${medicamentoId}/lotes`, lote).pipe(
      tap(medActualizado => {
        this.medicamentosSignal.update(meds => 
          meds.map(m => m.id === medActualizado.id ? medActualizado : m)
        );
      })
    );
  }

  eliminarMedicamento(id: any): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        this.medicamentosSignal.update(meds => meds.filter(m => m.id !== id));
      })
    );
  }

  despacharMedicamentos(items: { medicamentoId: number; cantidad: number }[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/despachar`, { items }).pipe(
      tap(() => {
        // Recargar inventario completo desde el servidor para reflejar el stock real
        this.cargarMedicamentos();
      })
    );
  }
}
