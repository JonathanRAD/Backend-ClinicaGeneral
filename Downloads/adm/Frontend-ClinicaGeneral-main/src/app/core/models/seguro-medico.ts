
import { Patient } from './patient';

export interface SeguroMedico {
  id: number;
  nombreAseguradora: string;
  numeroPoliza: string;
  cobertura: string;
  paciente?: Patient;
}