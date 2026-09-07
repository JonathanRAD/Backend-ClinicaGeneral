export interface Triaje {
  id?: string | number;
  citaId: string;
  peso: number;
  altura: number;
  temperatura: number;
  presionArterial: string;
  ritmoCardiaco: number;
  saturacionOxigeno: number;
  nivelAzucar?: number;
  motivoConsulta: string;
  notasOpcionales?: string;
  enfermeraId?: string | number;
  fechaRegistro: Date;
}
