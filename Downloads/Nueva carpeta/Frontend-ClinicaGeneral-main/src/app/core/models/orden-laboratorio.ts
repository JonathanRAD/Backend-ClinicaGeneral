
import { ResultadoLaboratorio } from './resultado-laboratorio';

export interface OrdenLaboratorio {
  id: number;
  fechaOrden: Date;
  tipoExamen: string;
  observaciones: string;
  resultadoLaboratorio?: ResultadoLaboratorio;
}