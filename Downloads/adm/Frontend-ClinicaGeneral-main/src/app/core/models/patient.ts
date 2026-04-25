
import { SeguroMedico } from './seguro-medico'; 

export interface Patient {
  id: string;
  dni: string;
  nombres: string;
  apellidos: string;
  fechaNacimiento: Date;
  telefono: string;
  fotoUrl?: string;
  
  peso?: number;
  altura?: number;
  ritmoCardiaco?: number;

  historiaClinica?: any;
  seguroMedico?: SeguroMedico; 
}