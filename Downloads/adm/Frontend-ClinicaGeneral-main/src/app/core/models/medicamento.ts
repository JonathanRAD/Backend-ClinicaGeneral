export interface LoteMedicamento {
  id?: number | string;
  numeroLote: string;
  stock: number;
  fechaVencimiento: string | Date;
  fechaIngreso?: string | Date;
}

export interface Medicamento {
  id?: number | string;
  codigo: string;
  nombre: string;
  descripcion?: string;
  formaFarmaceutica: string;
  concentracion: string;
  precioUnitario: number;
  estado: 'ACTIVO' | 'INACTIVO';
  lotes?: LoteMedicamento[];
  stockTotal?: number; // Calculado en el backend usando @Transient
}
