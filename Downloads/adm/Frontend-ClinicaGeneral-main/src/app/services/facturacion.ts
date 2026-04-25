
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Factura } from '../core/models/factura';
import { environment } from '../environments/environment';

export interface FacturaPayload {
  citaId: number;
  monto: number;
  estado: 'pagada' | 'pendiente' | 'anulada';
  montoPagado: number;
}

@Injectable({
  providedIn: 'root'
})
export class FacturacionService {
  private facturasSignal = signal<Factura[]>([]);
  public facturas = this.facturasSignal.asReadonly();
  private apiUrl = `${environment.apiUrl}/facturas`;

  constructor(private http: HttpClient) {
    this.cargarFacturas();
  }

  cargarFacturas(): void {
    this.http.get<Factura[]>(this.apiUrl).subscribe({
      next: (data) => this.facturasSignal.set(data),
      error: (err) => {
        console.error('Error al cargar facturas:', err);
        this.facturasSignal.set([]);
      }
    });
  }

  registrarFactura(payload: FacturaPayload) {
    this.http.post<Factura>(this.apiUrl, payload).subscribe({
      next: (nuevaFactura) => {
        this.facturasSignal.update(existentes => [...existentes, nuevaFactura]);
      },
      error: (err) => console.error('Error al registrar factura:', err)
    });
  }

  actualizarFactura(id: string, payload: FacturaPayload) {
    this.http.put<Factura>(`${this.apiUrl}/${id}`, payload).subscribe({
      next: (facturaActualizada) => {
        this.facturasSignal.update(facturas =>
          facturas.map(f => (f.id === facturaActualizada.id ? facturaActualizada : f))
        );
      },
      error: (err) => console.error('Error al actualizar factura:', err)
    });
  }

  eliminarFactura(id: string) {
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        this.facturasSignal.update(facturas => facturas.filter(f => f.id !== id));
      },
      error: (err) => console.error('Error al anular factura:', err)
    });
  }

  /**
   * Solicita al backend que envíe la factura por correo al paciente.
   * Backend: POST /api/facturas/{id}/enviar-correo
   * Implementar en FacturaController usando el EmailService existente.
   */
  enviarFacturaPorCorreo(id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/${id}/enviar-correo`, {});
  }
}