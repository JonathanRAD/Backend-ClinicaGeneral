import { Consulta } from './consulta';
import { Patient } from './patient';

export interface HistoriaClinica {
  id: number;
  fechaCreacion: Date;
  antecedentes: string;
  alergias: string;
  enfermedadesCronicas: string;
  paciente: Patient;
  consultas: Consulta[];
}