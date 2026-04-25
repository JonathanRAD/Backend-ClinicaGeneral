// ── proveedor.service.ts ──────────────────────────────────────────────────
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';

export interface Proveedor {
  id?: number;
  nombre: string;
  ruc?: string;
  telefono?: string;
  email?: string;
  direccion?: string;
  contacto?: string;
}

@Injectable({ providedIn: 'root' })
export class ProveedorService {
  private apiUrl = `${environment.apiUrl}/proveedores`;
  private proveedoresSignal = signal<Proveedor[]>([]);
  public proveedores = this.proveedoresSignal.asReadonly();

  constructor(private http: HttpClient) {}

  cargarProveedores(): void {
    this.http.get<Proveedor[]>(this.apiUrl).subscribe({
      next: data => this.proveedoresSignal.set(data),
      error: err => {
        console.warn('Proveedores API no disponible, usando datos de muestra');
        // Datos de muestra hasta que exista el endpoint
        this.proveedoresSignal.set([
          { id: 1, nombre: 'FARMINDUSTRIA', ruc: '20100823550', telefono: '+51 1 619 8888', email: 'ventas@farmindustria.pe', direccion: 'Av. Universitaria 1650, Lima' },
          { id: 2, nombre: 'IQ FARMA', ruc: '20107034540', telefono: '+51 1 619 9000', email: 'contacto@iqfarma.com.pe', direccion: 'Calle Los Tulipanes 218, Lima' },
          { id: 3, nombre: 'MEDIFARMA', ruc: '20100085284', telefono: '+51 1 613 6000', email: 'ventas@medifarma.com.pe', direccion: 'Av. Argentina 4205, Lima' }
        ]);
      }
    });
  }

  crearProveedor(payload: Proveedor): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.apiUrl, payload).pipe(
      tap(nuevo => this.proveedoresSignal.update(p => [...p, nuevo]))
    );
  }

  actualizarProveedor(id: number, payload: Proveedor): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.apiUrl}/${id}`, payload).pipe(
      tap(actualizado => this.proveedoresSignal.update(p =>
        p.map(x => x.id === id ? actualizado : x)
      ))
    );
  }

  eliminarProveedor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.proveedoresSignal.update(p => p.filter(x => x.id !== id)))
    );
  }
}