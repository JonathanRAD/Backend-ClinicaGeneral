

import { Cita } from './cita';
import { DetalleFactura } from './detalle-factura'; 

export interface Factura {
  id: string;
  cita: Cita;
  monto: number;
  montoPagado: number; 
  fechaEmision: Date;
  estado: 'pagada' | 'pendiente' | 'anulada';
  detalles: DetalleFactura[]; 
}