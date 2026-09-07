
import { Patient } from './patient';
import { Medico } from './medico';
import { Triaje } from './triaje';

export interface Cita {
  id: string;
  fechaHora: Date;
  paciente: Patient;
  medico: Medico;
  motivo: string;
  estado: 'programada' | 'en_espera' | 'en_triaje' | 'lista_consulta' | 'completada' | 'cancelada';

  consultorio?: string; 
  numeroTurno?: number;  

  tiempoRestante?: string;
  alertaClase?: 'rojo' | 'ambar' | 'verde';
  triaje?: Triaje;
}