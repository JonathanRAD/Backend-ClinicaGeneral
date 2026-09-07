

import { Medico } from './medico';
import { OrdenLaboratorio } from './orden-laboratorio'; 
import { Receta } from './receta';
import { Cita } from './cita';

export interface Consulta {
  id: number;
  fechaConsulta: Date;
  motivo: string;
  diagnostico: string;
  tratamiento: string;
  medico: Medico;
  cita?: Cita;             // Incluye triaje si fue procesado
  ordenesLaboratorio?: OrdenLaboratorio[]; 
  recetas?: Receta[];
}