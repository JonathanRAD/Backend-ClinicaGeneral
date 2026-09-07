import { Patient } from './patient';
import { Medico } from './medico';

export interface Conferencia {
  id?: number;
  paciente: Patient;
  medico: Medico;
  fechaProgramada: string | Date;
  duracionMinutos: number;
  nombreSala?: string;
  estado?: string;
}
