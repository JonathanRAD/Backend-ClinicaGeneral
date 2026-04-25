import { DetalleReceta } from './detalle-receta';

export interface Receta {
  id?: number;
  fechaEmision?: string;
  indicacionesGenerales: string;
  detalles: DetalleReceta[];
}
