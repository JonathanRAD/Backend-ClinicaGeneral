export interface DetalleReceta {
  id?: number;
  medicamentoId: number;
  medicamento?: any; // Añadido para reflejar la relación directa del Backend
  nombreMedicamento?: string;
  codigoMedicamento?: string;
  cantidadSolicitada: number;
  dosis: string;
  frecuencia: string;
  duracion: string;
}
